package com.example.facadeservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "external-service", url = "${external-service.url}", configuration = ServiceConfiguration.class)
public interface ExternalServiceClient {

  @RequestMapping(method = RequestMethod.GET, value = "/external-data/{time}", consumes = "application/json")
  Data load(@PathVariable("time") Long time);
}
