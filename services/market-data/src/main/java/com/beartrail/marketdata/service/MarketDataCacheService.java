package com.beartrail.marketdata.service;

import com.beartrail.marketdata.model.entity.Candle;

import java.util.Optional;

public interface MarketDataCacheService {

    Optional<Candle> get(String cacheKey);

    void cacheLatestCandles(String symbol, String timeInterval, String data);

    void invalidateCache(String symbol, String timeInterval);

    void invalidateAllCache();
}