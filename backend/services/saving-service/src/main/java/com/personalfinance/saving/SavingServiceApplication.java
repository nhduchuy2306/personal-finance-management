package com.personalfinance.saving;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {
  "com.personalfinance.saving",
  "com.personalfinance.common"
})
@EnableDiscoveryClient
public class SavingServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(SavingServiceApplication.class, args);
  }
}
