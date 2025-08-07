package com.beartrail.marketdata.service.impl;

import com.beartrail.marketdata.model.entity.MarketData;
import com.beartrail.marketdata.model.entity.TimeInterval;
import com.beartrail.marketdata.repository.MarketDataRepository;
import com.beartrail.marketdata.service.MarketDataCacheService;
import com.beartrail.marketdata.service.MarketDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
public class MarketDataServiceImpl implements MarketDataService {

    private final MarketDataRepository marketDataRepository;
    private final MarketDataCacheService marketDataCacheService;

    public MarketDataServiceImpl(MarketDataRepository marketDataRepository, MarketDataCacheService marketDataCacheService) {
        this.marketDataRepository = marketDataRepository;
        this.marketDataCacheService = marketDataCacheService;
    }

    @Override
    public Optional<MarketData> getLatestMarketData(String symbol, String timeInterval, Long timestamp) {
        if (symbol == null || symbol.isEmpty()) {
            log.error("Invalid stock symbol provided: {}", symbol);
            return Optional.empty();
        }

        try {
            String cacheKey = String.format("%s_%s", symbol, timeInterval);
            Optional<MarketData> cachedData = marketDataCacheService.get(cacheKey);

            if (cachedData.isPresent()) {
                log.info("Cache hit for symbol: {}, time interval: {}", symbol, timeInterval);
                return cachedData;
            }

            log.info("Cache miss for symbol: {}, time interval: {}", symbol, timeInterval);
            Optional<MarketData> marketData = marketDataRepository.findBySymbolAndTimestamp(symbol, timestamp);
            if (marketData.isPresent()) {
                log.info("Latest market data found for symbol: {}, time interval: {}", symbol, timeInterval);
                marketDataCacheService.cacheLatestMarketData(symbol, timeInterval, marketData.get().toString());
                return marketData;
            } else {
                log.warn("No market data found for symbol: {}, time interval: {}", symbol, timeInterval);
                return Optional.empty();
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid time interval: {}", timeInterval, e);
            return Optional.empty();
        }
    }

    @Override
    public List<MarketData> getHistoricalMarketData(String symbol, String timeInterval) {

        if (symbol == null || symbol.isEmpty()) {
            log.error("Invalid stock symbol provided: {}", symbol);
            return List.of();
        }
        try {
            List<MarketData> historicalData = marketDataRepository.findBySymbol(symbol);
            if (historicalData.isEmpty()) {
                log.warn("No historical market data found for symbol: {}, time interval: {}", symbol, timeInterval);
            } else {
                log.info("Historical market data retrieved for symbol: {}, time interval: {}", symbol, timeInterval);
            }
            return historicalData;
        } catch (IllegalArgumentException e) {
            log.error("Invalid time interval: {}", timeInterval, e);
            return List.of();
        }
    }
}
