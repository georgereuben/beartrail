package com.beartrail.marketdataclient.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest(classes = UpstoxConfig.class)
@EnableConfigurationProperties(UpstoxConfig.class)
@TestPropertySource(properties = {
    "beartrail.upstox.auth-token=test-token-123",
    "beartrail.upstox.base-url=https://api.upstox.com/v2"
})
class UpstoxConfigTest {

    @Autowired
    private UpstoxConfig upstoxConfig;

    @Test
    void testConfigurationPropertiesBinding() {
        // Then
        assertNotNull(upstoxConfig);
        assertEquals("test-token-123", upstoxConfig.getAuthToken());
        assertEquals("https://api.upstox.com/v2", upstoxConfig.getBaseUrl());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        UpstoxConfig config = new UpstoxConfig();

        // When
        config.setAuthToken("new-token");
        config.setBaseUrl("https://new-api.upstox.com");

        // Then
        assertEquals("new-token", config.getAuthToken());
        assertEquals("https://new-api.upstox.com", config.getBaseUrl());
    }
}
