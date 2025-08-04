package com.beartrail.marketdata.service;

import com.beartrail.marketdata.model.entity.MarketData;

import java.util.List;
import java.util.Optional;

public interface MarketDataService {
    /**
     * Fetches the latest market data (candle) for a given symbol and time interval and timestamp.
     *
     * @param symbol       The stock symbol to fetch data for.
     * @param timeInterval The time interval for the market data (e.g., "1m", "5m", "1h").
     * @param timestamp   The timestamp to fetch the latest data after.
     * @return A string representing the latest market data in JSON format.
     */
    Optional<MarketData> getLatestMarketData(String symbol, String timeInterval, Long timestamp);

    /**
     * Fetches all the historical market data for a given symbol and time interval.
     *
     * @param symbol       The stock symbol to fetch historical data for.
     * @param timeInterval The time interval for the historical data (e.g., "1d", "1w").
     * @return A string representing the historical market data in JSON format.
     */
    List<MarketData> getHistoricalMarketData(String symbol, String timeInterval);
}
