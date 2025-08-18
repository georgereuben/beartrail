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

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("instrument_keys.json");

            if (inputStream == null) {
                log.error("instrument_keys.json file not found in resources");
                instrumentKeysToSymbolMap = new HashMap<>();
                return;
            }

            JsonNode rootNode = objectMapper.readTree(inputStream);
            instrumentKeysToSymbolMap = new HashMap<>();

            int totalCount = 0;
            int equityCount = 0;

            if (rootNode.isArray()) {
                for (JsonNode instrumentNode : rootNode) {
                    totalCount++;

                    String segment = instrumentNode.path("segment").asText();
                    String instrumentKey = instrumentNode.path("instrument_key").asText();
                    String name = instrumentNode.path("name").asText();

                    //only processing equity segments for now
                    if (EQUITY_SEGMENTS.contains(segment) && !instrumentKey.isEmpty() && !name.isEmpty()) {
                        instrumentKeysToSymbolMap.put(instrumentKey, name);
                        equityCount++;
                    }
                }
            }

            log.info("Successfully loaded {} equity instruments out of {} total instruments",
                    equityCount, totalCount);
            log.info("Instrument keys map size: {}", instrumentKeysToSymbolMap.size());

        } catch (IOException e) {
            log.error("Error loading instrument keys from JSON file", e);
            instrumentKeysToSymbolMap = new HashMap<>();
        }
    }
}
