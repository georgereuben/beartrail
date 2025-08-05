package com.beartrail.marketdata.service.impl;

import com.beartrail.marketdata.model.entity.MarketData;
import com.beartrail.marketdata.model.entity.TimeInterval;
import com.beartrail.marketdata.repository.MarketDataRepository;
import com.beartrail.marketdata.service.MarketDataCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MarketDataServiceImplTest {
    @Mock
    private MarketDataRepository marketDataRepository;
    @Mock
    private MarketDataCacheService marketDataCacheService;
    @InjectMocks
    private MarketDataServiceImpl marketDataService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getLatestMarketData_validCacheHit_returnsCachedData() {
        String symbol = "AAPL";
        String interval = "ONE_MINUTE";
        Long timestamp = 123L;
        MarketData mockData = mock(MarketData.class);
        when(marketDataCacheService.get(symbol + "_" + interval)).thenReturn(Optional.of(mockData));
        Optional<MarketData> result = marketDataService.getLatestMarketData(symbol, interval, timestamp);
        assertTrue(result.isPresent());
        verify(marketDataCacheService, times(1)).get(symbol + "_" + interval);
        verifyNoInteractions(marketDataRepository);
    }

    @Test
    void getLatestMarketData_validCacheMiss_repositoryHit_returnsDataAndCaches() {
        String symbol = "AAPL";
        String interval = "ONE_MINUTE";
        Long timestamp = 123L;
        MarketData mockData = mock(MarketData.class);
        when(marketDataCacheService.get(symbol + "_" + interval)).thenReturn(Optional.empty());
        when(marketDataRepository.findBySymbolAndTimeIntervalAndTimestamp(symbol, TimeInterval.ONE_MINUTE, timestamp)).thenReturn(Optional.of(mockData));
        Optional<MarketData> result = marketDataService.getLatestMarketData(symbol, interval, timestamp);
        assertTrue(result.isPresent());
        verify(marketDataCacheService).cacheLatestMarketData(eq(symbol), eq(interval), anyString());
    }

    @Test
    void getLatestMarketData_validCacheMiss_repositoryMiss_returnsEmpty() {
        String symbol = "AAPL";
        String interval = "ONE_MINUTE";
        Long timestamp = 123L;
        when(marketDataCacheService.get(symbol + "_" + interval)).thenReturn(Optional.empty());
        when(marketDataRepository.findBySymbolAndTimeIntervalAndTimestamp(symbol, TimeInterval.ONE_MINUTE, timestamp)).thenReturn(Optional.empty());
        Optional<MarketData> result = marketDataService.getLatestMarketData(symbol, interval, timestamp);
        assertTrue(result.isEmpty());
    }

    @Test
    void getLatestMarketData_invalidSymbol_returnsEmpty() {
        Optional<MarketData> result = marketDataService.getLatestMarketData("", "ONE_MINUTE", 123L);
        assertTrue(result.isEmpty());
    }

    @Test
    void getLatestMarketData_invalidInterval_returnsEmpty() {
        Optional<MarketData> result = marketDataService.getLatestMarketData("AAPL", "INVALID", 123L);
        assertTrue(result.isEmpty());
    }

    @Test
    void getHistoricalMarketData_valid_returnsData() {
        String symbol = "AAPL";
        String interval = "ONE_MINUTE";
        List<MarketData> mockList = List.of(mock(MarketData.class));
        when(marketDataRepository.findBySymbolAndTimeInterval(symbol, TimeInterval.ONE_MINUTE.toString())).thenReturn(mockList);
        List<MarketData> result = marketDataService.getHistoricalMarketData(symbol, interval);
        assertEquals(1, result.size());
    }

    @Test
    void getHistoricalMarketData_invalidSymbol_returnsEmptyList() {
        List<MarketData> result = marketDataService.getHistoricalMarketData("", "ONE_MINUTE");
        assertTrue(result.isEmpty());
    }

    @Test
    void getHistoricalMarketData_invalidInterval_returnsEmptyList() {
        List<MarketData> result = marketDataService.getHistoricalMarketData("AAPL", "INVALID");
        assertTrue(result.isEmpty());
    }

    @Test
    void getHistoricalMarketData_noData_returnsEmptyList() {
        String symbol = "AAPL";
        String interval = "ONE_MINUTE";
        when(marketDataRepository.findBySymbolAndTimeInterval(symbol, TimeInterval.ONE_MINUTE.toString())).thenReturn(List.of());
        List<MarketData> result = marketDataService.getHistoricalMarketData(symbol, interval);
        assertTrue(result.isEmpty());
    }
}

