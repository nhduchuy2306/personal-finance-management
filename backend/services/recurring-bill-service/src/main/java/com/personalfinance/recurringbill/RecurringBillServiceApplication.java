package com.personalfinance.recurringbill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {
  "com.personalfinance.recurringbill",
  "com.personalfinance.common"
})
@EnableDiscoveryClient
public class RecurringBillServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(RecurringBillServiceApplication.class, args);
  }
}
