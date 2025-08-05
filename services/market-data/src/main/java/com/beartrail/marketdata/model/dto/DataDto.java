package com.beartrail.marketdata.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataDto {
    @JsonProperty("last_price")
    private Double lastPrice;
    @JsonProperty("instrument_token")
    private String instrumentToken;
    @JsonProperty("prev_ohlc")
    private PriceUpdateDto prevOhlc;
    @JsonProperty("live_ohlc")
    private PriceUpdateDto liveOhlc;
}
