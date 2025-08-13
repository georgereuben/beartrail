package com.beartrail.marketdataclient.service;

import com.beartrail.marketdataclient.model.entity.TimeFrameValue;

public interface CandleUpdateService {

    void updateCandlesForInterval(TimeFrameValue interval);

    void updateCandlesForSymbol(String symbol, TimeFrameValue interval);

    long calculateCompletedIntervalTimestamp(TimeFrameValue interval);
}