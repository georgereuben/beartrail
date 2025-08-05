package com.beartrail.marketdata.service;

import com.beartrail.marketdata.event.publisher.PriceUpdateEvent;
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

    private final KafkaTemplate<String, PriceUpdateEvent> kafkaTemplate;

    public void sendPriceUpdate(PriceUpdateEvent priceUpdateEvent) {
        log.info("Sending price update event: {}", priceUpdateEvent);
        CompletableFuture<SendResult<String, PriceUpdateEvent>> future = kafkaTemplate.send("market-data-updates", priceUpdateEvent.getSymbol(), priceUpdateEvent);
        future.whenComplete(
                (result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send price update event: {}", priceUpdateEvent, ex);
                    } else {
                        log.info("Price update event sent successfully: {}", result.getProducerRecord());
                    }
                }
        );
    }
}
