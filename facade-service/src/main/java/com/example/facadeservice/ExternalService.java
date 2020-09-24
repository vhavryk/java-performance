package com.example.facadeservice;

import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

@Service
public class ExternalService {

  @Autowired
  private ExternalServiceClient externalServiceClient;

  public ResultData load(List<Long> times) {
    Long start = System.currentTimeMillis();
    LongSummaryStatistics statistics = times
        .parallelStream()
        .map(time -> externalServiceClient.load(time).getTime())
        .collect(Collectors.summarizingLong(Long::longValue));
    Long end = System.currentTimeMillis();
    return new ResultData(statistics, (end - start));
  }
}
