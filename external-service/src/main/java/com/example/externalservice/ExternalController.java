package com.example.externalservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExternalController {


  @GetMapping("/external-data/{time}")
  public ExternalData getData(@PathVariable Long time){
    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {
      // do nothing
    }
    return new ExternalData(time);
  }
}
