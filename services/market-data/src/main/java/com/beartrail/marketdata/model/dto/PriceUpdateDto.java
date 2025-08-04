package com.beartrail.marketdata.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceUpdateDto {       // represents a ohlc candle for a specific symbol at a specific time interval
    private String symbol;
    private Double lastPrice;
    private Double openPrice;
    private Double highPrice;
    private Double lowPrice;
    private Double closePrice;
    private Long volume;
    private Long timestamp;
    private String timeInterval = "1d";        // using string to represent TimeInterval enum
}
