package com.beartrail.marketdata.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Slf4j
@Component
public class InstrumentKeyLoader {

    private List<String> instrumentKeys;

    @PostConstruct
    public void loadInstrumentKeys() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            try (var is = getClass().getClassLoader().getResourceAsStream("instrument_keys.json")) {
                if (is == null) {
                    throw new IllegalStateException("instrument_keys.json not found in resources");
                }

                JsonNode rootNode = mapper.readTree(is);

                if (rootNode.isArray()) {
                    // Handle array structure: [{"instrument_key": "key1", ...}, {"instrument_key": "key2", ...}, ...]
                    instrumentKeys = new ArrayList<>();
                    for (JsonNode instrumentNode : rootNode) {
                        if (instrumentNode.isObject() && instrumentNode.has("instrument_key")) {
                            String instrumentKey = instrumentNode.get("instrument_key").asText();
                            if (!instrumentKey.isEmpty()) {
                                instrumentKeys.add(instrumentKey);
                            }
                        }
                    }
                } else if (rootNode.isObject()) {
                    // Handle object structure, look for common property names
                    instrumentKeys = new ArrayList<>();
                    if (rootNode.has("instrumentKeys")) {
                        JsonNode keysNode = rootNode.get("instrumentKeys");
                        if (keysNode.isArray()) {
                            for (JsonNode keyNode : keysNode) {
                                instrumentKeys.add(keyNode.asText());
                            }
                        }
                    } else if (rootNode.has("keys")) {
                        JsonNode keysNode = rootNode.get("keys");
                        if (keysNode.isArray()) {
                            for (JsonNode keyNode : keysNode) {
                                instrumentKeys.add(keyNode.asText());
                            }
                        }
                    } else {
                        // If it's an object but doesn't have expected properties,
                        // try to extract all string values
                        rootNode.fieldNames().forEachRemaining(fieldName -> {
                            JsonNode fieldNode = rootNode.get(fieldName);
                            if (fieldNode.isArray()) {
                                for (JsonNode item : fieldNode) {
                                    if (item.isTextual()) {
                                        instrumentKeys.add(item.asText());
                                    }
                                }
                            }
                        });
                    }
                } else {
                    throw new IllegalStateException("Unexpected JSON structure in instrument_keys.json");
                }
            }
            log.info("Instrument keys loaded: {}", instrumentKeys);
        } catch (Exception e) {
            log.error("Failed to load instrument keys", e);
        }
    }

}
