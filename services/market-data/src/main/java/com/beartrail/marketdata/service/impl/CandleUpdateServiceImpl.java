package com.beartrail.marketdata.service.impl;

import com.beartrail.marketdata.client.upstox.UpstoxApiClient;
import com.beartrail.marketdata.model.entity.TimeFrameValue;
import com.beartrail.marketdata.repository.MarketDataRepository;
import com.beartrail.marketdata.service.CandleUpdateService;
import com.beartrail.marketdata.service.InstrumentKeyLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public void updateCandles() {



    }

    @Override
    public void updateCandlesForSymbol(String symbol, TimeFrameValue interval) {

    }

    @Override
    public long calculateCompletedIntervalTimestamp(TimeFrameValue interval) {
        return 0;
    }
}
