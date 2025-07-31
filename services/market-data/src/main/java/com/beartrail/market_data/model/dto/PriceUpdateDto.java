package com.beartrail.market_data.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceUpdateDto {
    private String symbol;
    private Double lastPrice;
    private Double openPrice;
    private Double highPrice;
    private Double lowPrice;
    private Double closePrice;
    private Long volume;
    private Long timestamp;
    private String timeInterval;        // using string to represent TimeInterval enum
}
