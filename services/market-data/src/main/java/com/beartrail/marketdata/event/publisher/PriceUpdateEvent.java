package com.beartrail.marketdata.event.publisher;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PriceUpdateEvent {
    private String symbol;
    private Double lastPrice;
    private Double openPrice;
    private Double highPrice;
    private Double lowPrice;
    private Double closePrice;
    private Long volume;
    private Long timestamp;
    private String timeInterval; // using string to represent TimeInterval enum
}
