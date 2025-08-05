package com.beartrail.marketdata.service.impl;

import com.beartrail.marketdata.client.upstox.UpstoxApiClient;
import com.beartrail.marketdata.model.entity.MarketData;
import com.beartrail.marketdata.model.entity.TimeInterval;
import com.beartrail.marketdata.repository.MarketDataRepository;
import com.beartrail.marketdata.service.CandleUpdateService;
import com.beartrail.marketdata.service.InstrumentKeyLoader;
import com.beartrail.marketdata.service.MarketDataKafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CandleUpdateServiceImpl implements CandleUpdateService {

    @Autowired
    private MarketDataServiceImpl marketDataService;
    @Autowired
    private MarketDataCacheServiceImpl marketDataCacheService;
    @Autowired
    private MarketDataRepository marketDataRepository;
    @Autowired
    private UpstoxApiClient upstoxApiClient;
    @Autowired
    private InstrumentKeyLoader instrumentKeyLoader;
    @Autowired
    private MarketDataKafkaProducer marketDataKafkaProducer;

    @Override
    public void updateCandlesForInterval(TimeInterval interval) {
        if (interval == null) {
            throw new IllegalArgumentException("Time interval cannot be null");
        }

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
                marketDataKafkaProducer.sendPriceUpdate(marketData.toPriceUpdateEvent(interval));
                marketDataRepository.save(marketData);
                marketDataCacheService.cacheLatestMarketData(marketData.getSymbol(), interval.name(), marketData.toString());
            }
        }
    }

    @Override
    public void updateCandlesForSymbol(String symbol, TimeInterval interval) {

    }

    @Override
    public long calculateCompletedIntervalTimestamp(TimeInterval interval) {
        return 0;
    }
}
