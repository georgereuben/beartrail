package com.beartrail.market_data.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataDto {
    private String symbol;
    private Map<String, Double> lastPrice;
    private Map<String, String> instrumentToken;
    private Map<String, PriceUpdateDto> prevOhlc;
    private Map<String, PriceUpdateDto> liveOhlc;
    private String timeInterval;                    // using string to represent TimeInterval enum
}
