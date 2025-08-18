package com.beartrail.marketdata.event.publisher;

import com.beartrail.marketdata.model.dto.PriceUpdateDto;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class PriceUpdateEvent {
    private String symbol;
    private String instrumentToken;
    private Double lastPrice;
    private PriceUpdateDto prevOhlc;
    private PriceUpdateDto liveOhlc;
    // private String timeInterval;         // to be extended later, for now we use only 1m candles and use timescaledb aggregation to get other intervals
}
