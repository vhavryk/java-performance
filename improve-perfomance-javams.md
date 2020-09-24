# Improve performance of the java microservice

This article shows how to improve performance of the 
spring boot microservice

## What You Will use

We will use 2 java microserveces :

**external-service** :  "Real life" microservice accessible by https

**facade-service** : This microservice will reads data 
from external-service & sends result to clients. We are going to improve performance of this service.

## What You need
- Java 8
- Jmeter 5.3
- Java IDE
- Gradle 6.6.1

## Download source

First of all need to download source from https://github.com/vhavryk/java-performance

## External service

This service was created by [spring initializer](https://start.spring.io/)
and have one controller simulates load :

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

Just run **ExternalServiceApplication** & access this one by

https://localhost:8543/external-data/300


## Facade service

This one also created by [Spring Initializer](https://start.spring.io/)
And have 2 major classes :

#### ExternalService 

Reads data from external service using externalServiceClient & calculate sum of the times.

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
    
#### ExternalServiceClient

We will use [openfeign](https://github.com/OpenFeign/feign) library to read data from external service.
Current implementation of the http client based on [OKHttp library](https://square.github.io/okhttp/)

    @FeignClient(name = "external-service", url = "${external-service.url}", configuration = ServiceConfiguration.class)
    public interface ExternalServiceClient {
    
      @RequestMapping(method = RequestMethod.GET, value = "/external-data/{time}", consumes = "application/json")
      Data load(@PathVariable("time") Long time);
    }
    
  
Run **FacadeServiceApplication** class   
Call http://localhost:8080/data/1,500,920,20000  
Response will look like :

    {
        "statistics": {
            "count": 4,
            "sum": 1621,
            "min": 1,
            "max": 920,
            "average": 405.25
        },
        "spentTime": 1183
    }

  
## Preparing to performance run

Run Jmeter 5.3.1 & open file **perfomance-testing.jmx** in root of the source folder.    

##### Performance Test Configuration : 

| Name | Value |
| ----------- | ----------- |
| Number of threads (users) | 250 |
| Rump-up period in seconds | 5 |
| Loop count | 20 | 
| Processor | Intel® Core™ i5-3210M Processor 2.5GHz |
| Number of processors | 4 |  

We are going to load test the following url : 
http://localhost:8080/data/1,500,920,200  
So go to Jmeter and press start button.

## Jmeter first run results/analyses

| Name | Value |
| ----------- | ----------- |
| Average time (ms) | 29874 |
| Maximim time (ms) | 30304 |
| Throughput (sec) | 6 |
| Error % | 98 |
| Main error | Jmeter throws client socket timeout exception |
 
Server become unavailable.   
This is because we use parallelStream() call in ExternalService.
Java streams uses [fork join pool](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinPool.html) to handles data in parallel mode.
And by default parallelism of fork join pool calculates
using number of available processors. In my case this is 3.
For IO operations this is bottleneck.
So lets increase parallelism of the fork join pool to value 1000.

    -Djava.util.concurrent.ForkJoinPool.common.parallelism=1000
    
And run Jmeter again.
    
 
## Jmeter second run results/analyses

| Name | Value |
| ----------- | ----------- |
| Average time (ms) | 9078 |
| Maximim time (ms) | 21804 |
| Throughput (sec) | 26 |
| Error % | 0 |

So we improved performance from 6 rps to 26 rps.
This is huge. Also service is stable with zero errors.
But still average time is high 9 sec.
I have assumption this is bc of app spents a lot of time for
creating https connection. Lets add connection pool :

    @Configuration
    public class ServiceConfiguration {
    
      .....
    
      @Bean
      public OkHttpClient client()
          throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, NoSuchProviderException {
        
        .......
        
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
            .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
            .hostnameVerifier((s, sslSession) -> true)
            .connectionPool(new ConnectionPool(2000, 10, TimeUnit.SECONDS))
            .build();
    
        OkHttpClient okHttpClient = new OkHttpClient(client);
    
        return okHttpClient;
      }
      
      
So app can keep up to 2000 https connection in pool for 10 seconds.

## Jmeter third run results/analyses  

| Name | Value |
| ----------- | ----------- |
| Average time (ms) | 3208 |
| Maximim time (ms) | 7600 |
| Throughput (sec) | 71 |
| Error % | 0 |
    
 So we improved performance almost in 3 times from 26 to 71 rps.  
 Overall performance improvement is 10 times from 6 to 71 rps.
 But We see that maximum time is high 7 seconds. 
 It's a lot bc affects overall performance & UI client's won't wait so long.  
 So we need to limit number of requests to handle.  
 We can do it using specified tomcat properties :
 
     server.tomcat.accept-count=80
     server.tomcat.max-connections=80
     server.tomcat.max-threads=160 
     
App will reject(send Connection refused) to all clients ones 160 connection is reached.

## Jmeter forth run results/analyses  

| Name | Value |
| ----------- | ----------- |
| Average time (ms) | 2270 |
| Maximim time (ms) | 4690 |
| Throughput (sec) | 94 |
| Error % | 29 |

Maximum time now up to 5 sec.  
Increased rps to 94 from 71.  
Expectedly increased percent of the errors to 29%.  
All errors are connection refused errors.

## Final words

In this article I showed how to improve performance at 15 times from 6 to 94 rps without complex changes of the code.

