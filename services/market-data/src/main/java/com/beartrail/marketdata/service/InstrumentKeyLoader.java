package com.beartrail.marketdata.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
@Slf4j
@Component
public class InstrumentKeyLoader {

    private Map<String, String> instrumentKeysToSymbolMap;
    private static final Set<String> EQUITY_SEGMENTS = Set.of("NSE_EQ", "BSE_EQ");

    @PostConstruct
    public void loadInstrumentKeys() {
        log.info("Loading instrument keys from JSON file...");

        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("instrument_keys.json")) {
            if (inputStream == null) {
                log.error("instrument_keys.json file not found in resources");
                instrumentKeysToSymbolMap = new HashMap<>();
                return;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(inputStream);
            instrumentKeysToSymbolMap = new HashMap<>();

            if (rootNode.isArray()) {
                int totalCount = 0; // NOPMD - DD anomaly is acceptable here
                int equityCount = 0; // NOPMD - DD anomaly is acceptable here

                for (JsonNode instrumentNode : rootNode) {
                    totalCount++; // NOPMD - Counter increment is intentional

                    String segment = instrumentNode.path("segment").asText();
                    String instrumentKey = instrumentNode.path("instrument_key").asText();
                    String name = instrumentNode.path("name").asText();

                    //only processing equity segments for now
                    if (EQUITY_SEGMENTS.contains(segment) && !instrumentKey.isEmpty() && !name.isEmpty()) {
                        instrumentKeysToSymbolMap.put(instrumentKey, name);
                        equityCount++; // NOPMD - Counter increment is intentional
                    }
                }

                log.info("Successfully loaded {} equity instruments out of {} total instruments",
                        equityCount, totalCount);
            } else {
                log.warn("Root node is not an array, no instruments loaded");
            }

            log.info("Instrument keys map size: {}", instrumentKeysToSymbolMap.size());

        } catch (IOException e) {
            log.error("Error loading instrument keys from JSON file", e);
            instrumentKeysToSymbolMap = new HashMap<>();
        }
    }
}
