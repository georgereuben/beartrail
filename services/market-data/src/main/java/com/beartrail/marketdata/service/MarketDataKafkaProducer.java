package com.beartrail.marketdata.service;

import com.beartrail.marketdata.event.publisher.PriceUpdateEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class MarketDataKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());        // instant was not being registered, thus added this module

    public void sendPriceUpdate(PriceUpdateEvent priceUpdateEvent) {
        log.info("Sending price update event: {}", priceUpdateEvent);
        try {
            String eventJson = objectMapper.writeValueAsString(priceUpdateEvent);
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send("market-data-updates", priceUpdateEvent.getSymbol(), eventJson);
            future.whenComplete(
                    (result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send price update event: {}", eventJson, ex);
                        } else {
                            log.info("Price update event sent successfully: {}", result.getProducerRecord());
                        }
                    }
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize price update event: {}", priceUpdateEvent, e);
        }
    }
}
