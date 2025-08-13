package com.beartrail.marketdata.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceUpdateDto {       // represents a ohlc candle for a specific symbol at a specific time interval
    @JsonProperty("open")
    private BigDecimal openPrice;
    @JsonProperty("high")
    private BigDecimal highPrice;
    @JsonProperty("low")
    private BigDecimal lowPrice;
    @JsonProperty("close")
    private BigDecimal closePrice;
    @JsonProperty("volume")
    private Long volume;
    @JsonProperty("ts")
    private Instant timestamp; // in ms from epoch
}
