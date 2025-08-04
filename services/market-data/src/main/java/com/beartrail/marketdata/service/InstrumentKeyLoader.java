package com.beartrail.marketdata.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Component
public class InstrumentKeyLoader {

    private List<String> instrumentKeys;

    @PostConstruct
    public void loadInstrumentKeys() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            instrumentKeys = mapper.readValue(
                    Paths.get("src/main/resources/instrument_keys.json").toFile(),
                    mapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
            log.info("Instrument keys loaded: {}", instrumentKeys);
        } catch (Exception e) {
            log.error("Failed to load instrument keys", e);
        }
    }

    public List<String> getInstrumentKeys() {
        return instrumentKeys;
    }
}
