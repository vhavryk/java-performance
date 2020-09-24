package com.example.facadeservice;

import feign.okhttp.OkHttpClient;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.ConnectionPool;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ServiceConfiguration {

  @Value("${trust.store}")
  private Resource trustStore;

  @Value("${trust.store.password}")
  private String trustStorePassword;

  @Bean
  public OkHttpClient client()
      throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, NoSuchProviderException {
    SSLContext sslContext = new SSLContextBuilder()
        .loadTrustMaterial(trustStore.getURL(), trustStorePassword.toCharArray())
        .build();

    X509TrustManager trustManager = trustManager();

    okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
        .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
        .hostnameVerifier((s, sslSession) -> true)
        .connectionPool(new ConnectionPool(2000, 10, TimeUnit.SECONDS))
        .build();

    OkHttpClient okHttpClient = new OkHttpClient(client);

    return okHttpClient;
  }

  private X509TrustManager trustManager() throws NoSuchAlgorithmException, KeyStoreException {
    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
        TrustManagerFactory.getDefaultAlgorithm());
    trustManagerFactory.init((KeyStore) null);
    TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
    if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
      throw new IllegalStateException("Unexpected default trust managers:"
          + Arrays.toString(trustManagers));
    }
    return (X509TrustManager) trustManagers[0];
  }


}
