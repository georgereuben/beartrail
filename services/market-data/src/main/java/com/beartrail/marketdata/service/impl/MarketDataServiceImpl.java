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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

    private final Map<String, Long> instrumentTokenToStockIdCache = new ConcurrentHashMap<>();

    @Cacheable(key = "'stock_id_' + #instrumentToken")
    public Long getStockIdByInstrumentToken(String instrumentToken) {
        return stockRepository.findIdByInstrumentToken(instrumentToken);
    }

    @Cacheable(key = "'stock_entity_' + #stockId")
    public Stock getStockById(Long stockId) {
        return stockRepository.findById(stockId).orElse(null);
    }

    @Cacheable(key = "'timeframe_' + #value")
    public TimeFrame getTimeFrame(String value) {
        return timeFrameRepository.findByValue(value);
    }

    @KafkaListener(topics = "market-data-updates", containerFactory = "batchFactory")
    @Transactional
    public void consumeMarketDataBatch(List<String> messages) throws JsonProcessingException {
        List<Candle> candles = new ArrayList<>();
        Set<String> newInstrumentTokens = new HashSet<>();

        List<PriceUpdateEvent> events = new ArrayList<>();
        for (String message : messages) {
            events.add(parseMessage(message));
        }

        // check which stocks need creation, some might be new
        for (PriceUpdateEvent event : events) {
            Long stockId = getStockIdByInstrumentToken(event.getInstrumentToken());
            if (stockId == null) {
                newInstrumentTokens.add(event.getInstrumentToken());
            }
        }

        // batch create only the new stocks
        if (!newInstrumentTokens.isEmpty()) {
            createNewStocks(events, newInstrumentTokens);
            evictStockCaches(newInstrumentTokens);
        }

        //batch fetch all required stocks in a single query
        Set<String> allInstrumentTokens = events.stream()
            .map(PriceUpdateEvent::getInstrumentToken)
            .collect(Collectors.toSet());

        List<Stock> stocks = stockRepository.findByInstrumentTokenIn(allInstrumentTokens);
        Map<String, Stock> instrumentTokenToStockMap = stocks.stream()
            .collect(Collectors.toMap(Stock::getInstrumentToken, stock -> stock));

        TimeFrame timeFrame = getTimeFrame("I1");
        for (PriceUpdateEvent event : events) {
            Stock stock = instrumentTokenToStockMap.get(event.getInstrumentToken());
            if (stock == null) {
                log.error("Stock not found for instrument token: {}", event.getInstrumentToken());
                continue;
            }
            candles.add(createCandle(stock, event, timeFrame));
        }

        marketDataRepository.saveAll(candles);
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

    @CacheEvict(allEntries = true, cacheNames = {"stocks"})
    private void evictStockCaches(Set<String> instrumentTokens) {

    }

    private void createNewStocks(List<PriceUpdateEvent> events, Set<String> newInstrumentTokens) {
        Map<String, String> instrumentToNameMap = instrumentKeyLoader.getInstrumentKeysToSymbolMap();
        Set<String> processedSymbols = new HashSet<>();

        for (PriceUpdateEvent event : events) {
            if (newInstrumentTokens.contains(event.getInstrumentToken()) &&
                !processedSymbols.contains(event.getSymbol())) {

                stockRepository.upsertStock(
                    event.getSymbol(),
                    event.getInstrumentToken(),
                    instrumentToNameMap.get(event.getInstrumentToken()),
                    BigDecimal.valueOf(event.getLastPrice())
                );
                processedSymbols.add(event.getSymbol());
            }
        }

        log.info("Upserted {} stocks", processedSymbols.size());
    }

    private Candle createCandle(Stock stock, PriceUpdateEvent event, TimeFrame timeFrame) {
        if (event.getPrevOhlc() == null) {
            PriceUpdateDto priceUpdateDto = PriceUpdateDto.builder()
                    .timestamp(Instant.now().toEpochMilli())
                    .openPrice(BigDecimal.valueOf(event.getLastPrice()))
                    .highPrice(BigDecimal.valueOf(event.getLastPrice()))
                    .lowPrice(BigDecimal.valueOf(event.getLastPrice()))
                    .closePrice(BigDecimal.valueOf(event.getLastPrice()))
                    .volume(0L)
                    .build();
            event.setPrevOhlc(priceUpdateDto);
        }

        return Candle.builder()
                .candleId(UUID.randomUUID())
                .stock(stock)
                .timeFrame(timeFrame)
                .timestamp(Instant.now().truncatedTo(ChronoUnit.MINUTES))
                .openPrice(event.getPrevOhlc().getOpenPrice())
                .highPrice(event.getPrevOhlc().getHighPrice())
                .lowPrice(event.getPrevOhlc().getLowPrice())
                .closePrice(event.getPrevOhlc().getClosePrice())
                .volume(event.getPrevOhlc().getVolume())
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
