package com.beartrail.marketdataclient.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public RestTemplate testRestTemplate() {
        return mock(RestTemplate.class);
    }

    @Bean
    @Primary
    public UpstoxConfig testUpstoxConfig() {
        UpstoxConfig config = new UpstoxConfig();
        config.setBaseUrl("https://api.upstox.com/v2");
        config.setAuthToken("test-token");
        return config;
    }
}
