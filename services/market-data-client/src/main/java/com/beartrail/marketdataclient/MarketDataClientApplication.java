package com.beartrail.marketdataclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MarketDataClientApplication {

  public static void main(String[] args) {
    SpringApplication.run(MarketDataClientApplication.class, args);
  }
}
