package com.example.facadeservice;

import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FacadeController {

  @Autowired
  private ExternalService externalService;

  @GetMapping("/data/{times}")
  public ResponseEntity<ResultData> load(@NotEmpty @PathVariable List<Long> times)
      throws InterruptedException, ExecutionException {
    ResponseEntity<ResultData> responseEntity;
    CompletableFuture<ResultData> future = CompletableFuture.supplyAsync(() -> externalService.load(times));
    try {
      ResultData resultData = future.get(100, TimeUnit.SECONDS);
      responseEntity = new ResponseEntity<>(resultData, HttpStatus.OK);
    } catch (TimeoutException e) {
      ResultData resultData = new ResultData(new LongSummaryStatistics(), 5);
      responseEntity = new ResponseEntity<>(resultData, HttpStatus.GATEWAY_TIMEOUT);
    }
    return responseEntity;
  }

}
