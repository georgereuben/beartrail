package com.beartrail.marketdata.service.impl;

import com.beartrail.marketdata.model.entity.MarketData;
import com.beartrail.marketdata.service.MarketDataCacheService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MarketDataCacheServiceImpl implements MarketDataCacheService {         // TODO: Implement caching logic

    @Override
    public Optional<MarketData> get(String cacheKey) {
        return Optional.empty();
    }

    @Override
    public void cacheLatestMarketData(String symbol, String timeInterval, String data) {

    }

    @Override
    public void invalidateCache(String symbol, String timeInterval) {

    }

    @Override
    public void invalidateAllCache() {

    }
}
