package com.beartrail.marketdata.service;

import com.beartrail.marketdata.model.entity.TimeFrameValue;

public interface CandleUpdateService {

    void updateCandlesForInterval(TimeFrameValue interval);

    void updateCandlesForSymbol(String symbol, TimeFrameValue interval);

    long calculateCompletedIntervalTimestamp(TimeFrameValue interval);
}