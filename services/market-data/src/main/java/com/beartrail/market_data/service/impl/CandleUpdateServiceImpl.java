package com.beartrail.market_data.service.impl;

import com.beartrail.market_data.client.upstox.UpstoxApiClient;
import com.beartrail.market_data.model.entity.MarketData;
import com.beartrail.market_data.model.entity.TimeInterval;
import com.beartrail.market_data.repository.MarketDataRepository;
import com.beartrail.market_data.service.CandleUpdateService;
import com.beartrail.market_data.service.InstrumentKeyLoader;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class CandleUpdateServiceImpl implements CandleUpdateService {

    @Autowired
    private MarketDataServiceImpl marketDataService;
    @Autowired
    private MarketDataCacheServiceImpl marketDataCacheService;
    @Autowired
    private MarketDataRepository marketDataRepository;
    @Autowired
    private UpstoxApiClient upstoxApiClient;

    @Override
    public void updateCandlesForInterval(TimeInterval interval) {
        if (interval == null) {
            throw new IllegalArgumentException("Time interval cannot be null");
        }

        InstrumentKeyLoader instrumentKeyLoader = new InstrumentKeyLoader();
        List<String> symbols = instrumentKeyLoader.getInstrumentKeys();

        if (symbols.isEmpty()) {
            throw new RuntimeException("No instrument keys found for the given interval: " + interval);
        }
        //process symbols in batches of 500
        int batchSize = 500;
        for (int i = 0; i < symbols.size(); i += batchSize) {
            int end = Math.min(i + batchSize, symbols.size());
            List<String> batchSymbols = symbols.subList(i, end);

            List<MarketData> marketDataList = upstoxApiClient.getMarketData(batchSymbols, interval.name());

            for (MarketData marketData : marketDataList) {
                marketDataRepository.save(marketData);
                marketDataCacheService.cacheLatestMarketData(marketData.getSymbol(), interval.name(), marketData.toString());
            }
        }
    }

    @Override
    public void updateCandlesForSymbol(String symbol, TimeInterval interval) {

    }

    @Override
    public List<String> getActiveSymbols() {
        return List.of();
    }

    @Override
    public long calculateCompletedIntervalTimestamp(TimeInterval interval) {
        return 0;
    }
}
