package com.beartrail.marketdata.service;

import com.beartrail.marketdata.model.entity.TimeFrameValue;

public interface CandleUpdateService {

    void updateCandles();

    void updateCandlesForSymbol(String symbol, TimeFrameValue interval);

    long calculateCompletedIntervalTimestamp(TimeFrameValue interval);
}