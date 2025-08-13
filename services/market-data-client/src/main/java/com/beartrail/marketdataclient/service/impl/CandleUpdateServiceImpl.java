package com.beartrail.marketdataclient.service.impl;

import com.beartrail.marketdataclient.client.upstox.UpstoxApiClient;
import com.beartrail.marketdataclient.model.entity.Candle;
import com.beartrail.marketdataclient.model.entity.TimeFrameValue;
import com.beartrail.marketdataclient.service.CandleUpdateService;
import com.beartrail.marketdataclient.service.InstrumentKeyLoader;
import com.beartrail.marketdataclient.service.MarketDataKafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CandleUpdateServiceImpl implements CandleUpdateService {

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
