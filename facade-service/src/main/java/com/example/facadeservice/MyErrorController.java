package com.example.facadeservice;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/error")
public class MyErrorController extends BasicErrorController {


  public MyErrorController(ErrorAttributes errorAttributes) {
    super(errorAttributes, new ErrorProperties());
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> error(final HttpServletRequest request) {
    final Map<String, Object> body = this.getErrorAttributes(request, false);
    final HttpStatus status = this.getStatus(request);
    return new ResponseEntity<>(body, status);
  }

  @Override
  public String getErrorPath() {
    return "/error";
  }


}
