package com.beartrail.market_data.service.impl;

import com.beartrail.market_data.model.entity.MarketData;
import com.beartrail.market_data.service.MarketDataCacheService;
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
