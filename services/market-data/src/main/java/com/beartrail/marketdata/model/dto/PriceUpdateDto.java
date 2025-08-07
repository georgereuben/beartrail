package com.beartrail.marketdata.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceUpdateDto {       // represents a ohlc candle for a specific symbol at a specific time interval
    @JsonProperty("open")
    private Double openPrice;
    @JsonProperty("high")
    private Double highPrice;
    @JsonProperty("low")
    private Double lowPrice;
    @JsonProperty("close")
    private Double closePrice;
    @JsonProperty("volume")
    private Long volume;
    @JsonProperty("ts")
    private Long timestamp; // in ms from epoch
}
