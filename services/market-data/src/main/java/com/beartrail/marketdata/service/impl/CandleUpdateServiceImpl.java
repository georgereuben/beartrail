package com.beartrail.marketdata.service.impl;

import com.beartrail.marketdata.client.upstox.UpstoxApiClient;
import com.beartrail.marketdata.model.entity.Candle;
import com.beartrail.marketdata.model.entity.TimeFrameValue;
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
    public void updateCandlesForInterval(TimeFrameValue interval) {
        if (interval == null) {
            throw new IllegalArgumentException("Time interval cannot be null");
        }

        List<String> symbols = instrumentKeyLoader.getInstrumentKeys();

        if (symbols.isEmpty()) {
            throw new RuntimeException("No instrument keys found for the given interval: " + interval);
        }
        //process symbols in batches of 500
        for (int i = 0; i < symbols.size(); i += 500) {
            int end = Math.min(i + 500, symbols.size());
            List<String> batchSymbols = symbols.subList(i, end);

            List<Candle> candleList = upstoxApiClient.getCandles(batchSymbols, interval.getValue());

            for (Candle candle : candleList) {
                marketDataKafkaProducer.sendPriceUpdate(candle.toPriceUpdateEvent());
                marketDataRepository.save(candle);                                                  // TODO: decouple this and make it consume from market_data kafka topic
                marketDataCacheService.cacheLatestCandles(candle.getStock().getSymbol(), interval.getValue(), candle.toString());
            }
        }
    }

    @Override
    public void updateCandlesForSymbol(String symbol, TimeFrameValue interval) {

    }

    @Override
    public long calculateCompletedIntervalTimestamp(TimeFrameValue interval) {
        return 0;
    }
}
