package com.beartrail.market_data.service;

public interface CacheService {

    String getLatestMarketData(String symbol, String timeInterval);

    void cacheLatestMarketData(String symbol, String timeInterval, String data);

    void invalidateCache(String symbol, String timeInterval);

    void invalidateAllCache();
}