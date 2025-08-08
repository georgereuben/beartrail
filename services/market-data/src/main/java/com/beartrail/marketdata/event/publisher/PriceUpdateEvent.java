package com.beartrail.marketdata.event.publisher;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class PriceUpdateEvent {
    private String symbol;
    private Double lastPrice;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal closePrice;
    private Long volume;
    private Instant timestamp;
    private String timeInterval; // using string to represent TimeInterval enum
}
