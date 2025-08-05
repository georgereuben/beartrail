package com.beartrail.marketdata.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Slf4j
@Component
public class InstrumentKeyLoader {

    private List<String> instrumentKeys;
    private static final Set<String> EQUITY_SEGMENTS = Set.of("NSE_EQ", "BSE_EQ");

    @PostConstruct
    public void loadInstrumentKeys() {
        try {
            try (var is = Thread.currentThread().getContextClassLoader().getResourceAsStream("instrument_keys.json")) {
                if (is == null) {
                    throw new IllegalStateException("instrument_keys.json not found in resources");
                }

                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(is);

                instrumentKeys = new ArrayList<>();

                if (rootNode.isArray()) {
                    // Process array of instrument objects
                    for (JsonNode instrumentNode : rootNode) {
                        if (instrumentNode.isObject()) {
                            processInstrumentNode(instrumentNode);
                        }
                    }
                } else {
                    throw new IllegalStateException("Expected JSON array structure in instrument_keys.json");
                }

                log.info("Loaded {} equity instrument keys", instrumentKeys.size());
                log.debug("Equity instrument keys: {}", instrumentKeys);
            }
        } catch (Exception e) {
            log.error("Failed to load instrument keys", e);
            instrumentKeys = new ArrayList<>(); // Initialize empty list on failure
        }
    }

    private void processInstrumentNode(JsonNode instrumentNode) {
        // Check if this is an equity instrument
        if (instrumentNode.has("segment") && instrumentNode.has("instrument_key")) {
            String segment = instrumentNode.get("segment").asText();

            if (EQUITY_SEGMENTS.contains(segment)) {
                String instrumentKey = instrumentNode.get("instrument_key").asText();

                if (!instrumentKey.isEmpty()) {
                    instrumentKeys.add(instrumentKey);

                    // Log details for debugging
                    String tradingSymbol = instrumentNode.has("trading_symbol")
                        ? instrumentNode.get("trading_symbol").asText() : "N/A";
                    String name = instrumentNode.has("name")
                        ? instrumentNode.get("name").asText() : "N/A";

                    log.debug("Added equity instrument: {} - {} ({})", instrumentKey, tradingSymbol, name);
                }
            }
        }
    }
}
