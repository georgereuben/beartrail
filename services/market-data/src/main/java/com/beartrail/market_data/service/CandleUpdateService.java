package com.beartrail.market_data.service;

import com.beartrail.market_data.model.entity.TimeInterval;

import java.util.List;

public interface CandleUpdateService {

    void updateCandlesForInterval(TimeInterval interval);

    void updateCandlesForSymbol(String symbol, TimeInterval interval);

    List<String> getActiveSymbols();

    long calculateCompletedIntervalTimestamp(TimeInterval interval);
}