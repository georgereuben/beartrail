package com.beartrail.market_data.service;

public interface MarketDataService {
    /**
     * Fetches the latest market data for a given symbol and time interval.
     *
     * @param symbol       The stock symbol to fetch data for.
     * @param timeInterval The time interval for the market data (e.g., "1m", "5m", "1h").
     * @return A string representing the latest market data in JSON format.
     */
    String getLatestMarketData(String symbol, String timeInterval);

    /**
     * Fetches historical market data for a given symbol and time interval.
     *
     * @param symbol       The stock symbol to fetch historical data for.
     * @param timeInterval The time interval for the historical data (e.g., "1d", "1w").
     * @return A string representing the historical market data in JSON format.
     */
    String getHistoricalMarketData(String symbol, String timeInterval);
}
