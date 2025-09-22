package com.beartrail.marketdata.service.impl;

import com.beartrail.marketdata.model.entity.Candle;
import com.beartrail.marketdata.service.MarketDataCacheService;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class MarketDataCacheServiceImpl
    implements MarketDataCacheService { // TODO: Implement caching logic

  @Override
  public Optional<Candle> get(String cacheKey) {
    return Optional.empty();
  }

  @Override
  public void cacheLatestCandles(String symbol, String timeInterval, String data) {}

  @Override
  public void invalidateCache(String symbol, String timeInterval) {}

  @Override
  public void invalidateAllCache() {}

  @Override
  public void cacheLatestCandles(String cacheKey, Candle candle) {}
}
