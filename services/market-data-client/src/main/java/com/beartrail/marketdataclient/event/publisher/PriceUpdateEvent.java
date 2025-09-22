package com.beartrail.marketdataclient.event.publisher;

import com.beartrail.marketdataclient.model.dto.PriceUpdateDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PriceUpdateEvent {
  private String symbol;
  private String instrumentToken;
  private Double lastPrice;
  private PriceUpdateDto prevOhlc;
  private PriceUpdateDto liveOhlc;
  // private String timeInterval;         // to be extended later, for now we use only 1m candles
  // and use timescaledb aggregation to get other intervals
}
