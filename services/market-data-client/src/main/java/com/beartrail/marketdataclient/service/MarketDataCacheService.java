package com.beartrail.marketdataclient.service;

import com.beartrail.marketdataclient.model.entity.Candle;

import java.util.Optional;

public interface MarketDataCacheService {

    Optional<Candle> get(String cacheKey);

    void cacheLatestCandles(String symbol, String timeInterval, String data);

    void invalidateCache(String symbol, String timeInterval);

    void invalidateAllCache();
}