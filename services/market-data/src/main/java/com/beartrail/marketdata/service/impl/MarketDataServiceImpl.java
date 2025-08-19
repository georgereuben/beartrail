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
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
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

    @KafkaListener(topics = "market-data-updates", groupId = "market-data-group")
    @Transactional
    public void consumeMarketData(String message) throws JsonProcessingException {
        try {
            log.info("Received market data update: {}", message);

            PriceUpdateEvent priceUpdateEvent = objectMapper.readValue(message, PriceUpdateEvent.class);

            // finding existing stock or create and save new one
            Stock stock = stockRepository.findBySymbol(priceUpdateEvent.getSymbol());
            if (stock == null) {
                String instrumentToken = priceUpdateEvent.getInstrumentToken();
                stock = Stock.builder()
                        .symbol(priceUpdateEvent.getSymbol())
                        .instrumentToken(instrumentToken)
                        .tradingName(instrumentKeyLoader.getInstrumentKeysToSymbolMap().get(instrumentToken))
                        .lastPrice(priceUpdateEvent.getLastPrice())
                        .build();
                stock = stockRepository.save(stock);
                log.info("Created new stock entity for symbol: {}", stock.getSymbol());
            } else {
                stock.setLastPrice(priceUpdateEvent.getLastPrice());
                stock = stockRepository.save(stock);
                log.info("Updated existing stock entity for symbol: {}", stock.getSymbol());
            }

            // finding 1 minute by default for now
            TimeFrame timeFrame = timeFrameRepository.findByValue("I1");
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

            // candle from price update event
            Candle candle = Candle.builder()
                    .candleId(System.currentTimeMillis()) // generate unique ID using timestamp since timescaledb was having issues with auto-generation
                    .stock(stock)
                    .timeFrame(timeFrame)
                    .timestamp(Instant.ofEpochMilli(priceUpdateEvent.getPrevOhlc().getTimestamp()))
                    .openPrice(priceUpdateEvent.getPrevOhlc().getOpenPrice())
                    .highPrice(priceUpdateEvent.getPrevOhlc().getHighPrice())
                    .lowPrice(priceUpdateEvent.getPrevOhlc().getLowPrice())
                    .closePrice(priceUpdateEvent.getPrevOhlc().getClosePrice())
                    .volume(priceUpdateEvent.getPrevOhlc().getVolume())
                    .build();

            marketDataRepository.save(candle);                  // TODO: batch save for performace opti

            log.info("Market data saved for symbol: {} ({})", stock.getSymbol(),
                    instrumentKeyLoader.getInstrumentKeysToSymbolMap().get(priceUpdateEvent.getInstrumentToken()));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse market data message: {}", message, e);
            throw e; // rethrowing here is to ensure the message is not acknowledged if parsing fails
        } catch (Exception e) {
            log.error("Error processing market data message: {}", message, e);
        }
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
                marketDataCacheService.cacheLatestCandles(symbol, timeInterval, marketData.get().toString());
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
