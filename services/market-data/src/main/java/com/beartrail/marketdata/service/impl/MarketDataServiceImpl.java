package com.beartrail.marketdata.service.impl;

import com.beartrail.marketdata.event.publisher.PriceUpdateEvent;
import com.beartrail.marketdata.model.dto.PriceUpdateDto;
import com.beartrail.marketdata.model.entity.Candle;
import com.beartrail.marketdata.model.entity.Instrument;
import com.beartrail.marketdata.model.entity.Stock;
import com.beartrail.marketdata.model.entity.TimeFrame;
import com.beartrail.marketdata.repository.MarketDataRepository;
import com.beartrail.marketdata.repository.StockRepository;
import com.beartrail.marketdata.repository.TimeFrameRepository;
import com.beartrail.marketdata.service.InstrumentKeyLoader;
import com.beartrail.marketdata.service.MarketDataCacheService;
import com.beartrail.marketdata.service.MarketDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@CacheConfig(cacheNames = "marketData")
public class MarketDataServiceImpl implements MarketDataService {

    private final MarketDataRepository marketDataRepository;
    private final StockRepository stockRepository;
    private final TimeFrameRepository timeFrameRepository;
    private final MarketDataCacheService marketDataCacheService;
    private final ObjectMapper objectMapper;
    private final InstrumentKeyLoader instrumentKeyLoader;

    public MarketDataServiceImpl(MarketDataRepository marketDataRepository, StockRepository stockRepository, TimeFrameRepository timeFrameRepository, MarketDataCacheService marketDataCacheService, ObjectMapper objectMapper, InstrumentKeyLoader instrumentKeyLoader) {
        this.marketDataRepository = marketDataRepository;
        this.stockRepository = stockRepository;
        this.timeFrameRepository = timeFrameRepository;
        this.marketDataCacheService = marketDataCacheService;
        this.objectMapper = objectMapper;
        this.instrumentKeyLoader = instrumentKeyLoader;
    }

    @Cacheable(key = "'timeframe_' + #value")
    public TimeFrame getTimeFrame(String value) {
        return timeFrameRepository.findByValue(value);
    }

    @Cacheable(key = "'stock_' + #symbol")
    public Stock getStockBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol);
    }

    @KafkaListener(topics = "market-data-updates", containerFactory = "batchFactory")
    @Transactional
    public void consumeMarketDataBatch(List<String> messages) throws JsonProcessingException {
        List<Candle> candles = new ArrayList<>();
        Set<String> symbolsToUpdate = new HashSet<>();

        for (String message : messages) {
            PriceUpdateEvent event = parseMessage(message);
            Stock stock = findOrCreateStock(event);
            candles.add(createCandle(stock, event));
            symbolsToUpdate.add(event.getSymbol());
        }

        marketDataRepository.saveAll(candles);

        stockRepository.updateLastPricesInBatch(symbolsToUpdate);
    }

    private PriceUpdateEvent parseMessage(String message) throws JsonProcessingException {
        try {
            PriceUpdateEvent priceUpdateEvent = objectMapper.readValue(message, PriceUpdateEvent.class);
            log.info("Received market data update for symbol: {}", priceUpdateEvent.getSymbol());
            return priceUpdateEvent;
        } catch (JsonProcessingException e) {
            log.error("Failed to parse market data update message: {}", message, e);
            throw e; // rethrowing, will handle it in the listener later
        }
    }

    private Stock findOrCreateStock(PriceUpdateEvent event) {
        stockRepository.upsertStock(
                event.getSymbol(),
                event.getInstrumentToken(),
                instrumentKeyLoader.getInstrumentKeysToSymbolMap().get(event.getInstrumentToken()),
                BigDecimal.valueOf(event.getLastPrice())
        );
        return getStockBySymbol(event.getSymbol());
    }

    private Candle createCandle(Stock stock, PriceUpdateEvent priceUpdateEvent) {
        TimeFrame timeFrame = getTimeFrame("I1");
        if (timeFrame == null) {
            log.error("TimeFrame with value 'I1' not found in database");
            throw new RuntimeException("Required TimeFrame not found");
        }

        // if prevOhlc is null, it means this is the first update for this stock or that the third party API did not provide previous OHLC data
        if (priceUpdateEvent.getPrevOhlc() == null) {
            log.warn("Previous OHLC data is null for symbol: {}, creating a new candle with last price as open, high, low, and close", stock.getSymbol());
            PriceUpdateDto priceUpdateDto = PriceUpdateDto.builder()
                    .timestamp(Instant.now().toEpochMilli())
                    .openPrice(BigDecimal.valueOf(priceUpdateEvent.getLastPrice()))
                    .highPrice(BigDecimal.valueOf(priceUpdateEvent.getLastPrice()))
                    .lowPrice(BigDecimal.valueOf(priceUpdateEvent.getLastPrice()))
                    .closePrice(BigDecimal.valueOf(priceUpdateEvent.getLastPrice()))
                    .volume(0L)
                    .build();
            priceUpdateEvent.setPrevOhlc(priceUpdateDto);
        } else {
            log.info("Using provided previous OHLC data for symbol: {}", stock.getSymbol());
        }

        return Candle.builder()
                .candleId(UUID.randomUUID())
                .stock(stock)
                .timeFrame(timeFrame)
                .timestamp(Instant.now()
                    .truncatedTo(ChronoUnit.MINUTES)) // truncated to that minute
                .openPrice(priceUpdateEvent.getPrevOhlc().getOpenPrice())
                .highPrice(priceUpdateEvent.getPrevOhlc().getHighPrice())
                .lowPrice(priceUpdateEvent.getPrevOhlc().getLowPrice())
                .closePrice(priceUpdateEvent.getPrevOhlc().getClosePrice())
                .volume(priceUpdateEvent.getPrevOhlc().getVolume())
                .build();
    }

    @Override
    public Optional<Candle> getLatestMarketData(String symbol, String timeInterval, Instant timestamp) {
        if (symbol == null || symbol.isEmpty()) {
            log.error("Invalid stock symbol provided: {}", symbol);
            return Optional.empty();
        }

        try {
            String cacheKey = String.format("%s_%s", symbol, timeInterval);
            Optional<Candle> cachedData = marketDataCacheService.get(cacheKey);

            if (cachedData.isPresent()) {
                log.info("Cache hit for symbol: {}, time interval: {}", symbol, timeInterval);
                return cachedData;
            }

            log.info("Cache miss for symbol: {}, time interval: {}", symbol, timeInterval);
            Optional<Candle> marketData = marketDataRepository.findByStock_SymbolAndTimestamp(symbol, timestamp);
            if (marketData.isPresent()) {
                log.info("Latest market data found for symbol: {}, time interval: {}", symbol, timeInterval);
                marketDataCacheService.cacheLatestCandles(cacheKey, marketData.get());
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
    public List<Candle> getHistoricalMarketData(String symbol, String timeInterval) {

        if (symbol == null || symbol.isEmpty()) {
            log.error("Invalid stock symbol provided: {}", symbol);
            return List.of();
        }
        try {
            List<Candle> historicalData = marketDataRepository.findByStock_Symbol(symbol);
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
