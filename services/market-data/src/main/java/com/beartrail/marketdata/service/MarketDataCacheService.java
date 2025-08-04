package com.beartrail.marketdata.service;

import com.beartrail.marketdata.model.entity.MarketData;

import java.util.Optional;

public interface MarketDataCacheService {

    Optional<MarketData> get(String cacheKey);

    void cacheLatestMarketData(String symbol, String timeInterval, String data);

    void invalidateCache(String symbol, String timeInterval);

    void invalidateAllCache();
}