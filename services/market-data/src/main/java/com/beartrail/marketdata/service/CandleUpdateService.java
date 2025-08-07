package com.beartrail.marketdata.service;

import com.beartrail.marketdata.model.entity.TimeInterval;

import java.util.List;

public interface CandleUpdateService {

    void updateCandlesForInterval(TimeInterval interval);

    void updateCandlesForSymbol(String symbol, TimeInterval interval);

    long calculateCompletedIntervalTimestamp(TimeInterval interval);
}