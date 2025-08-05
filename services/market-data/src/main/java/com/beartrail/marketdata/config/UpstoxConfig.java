package com.beartrail.marketdata.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "beartrail.upstox")
public class UpstoxConfig {
    private String authToken;
    private String baseUrl;                      // TODO: add redirect workflow for fetching authToken from frontend (with redirect URL)
}
