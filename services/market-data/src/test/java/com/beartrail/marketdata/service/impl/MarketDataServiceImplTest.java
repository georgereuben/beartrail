package com.beartrail.marketdata.service.impl;

import com.beartrail.marketdata.model.entity.Candle;
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

    private static final String TEST_SYMBOL = "RELIANCE";
    private static final String TEST_INTERVAL = "ONE_MINUTE";

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
        Long timestamp = 123L;
        Candle mockData = mock(Candle.class);
        when(marketDataCacheService.get(TEST_SYMBOL + "_" + TEST_INTERVAL)).thenReturn(Optional.of(mockData));
        Optional<Candle> result = marketDataService.getLatestMarketData(TEST_SYMBOL, TEST_INTERVAL, timestamp);
        assertTrue(result.isPresent());
        verify(marketDataCacheService, times(1)).get(TEST_SYMBOL + "_" + TEST_INTERVAL);
        verifyNoInteractions(marketDataRepository);
    }

    @Test
    void getLatestMarketData_validCacheMiss_repositoryHit_returnsDataAndCaches() {
        Long timestamp = 123L;
        Candle mockData = mock(Candle.class);
        when(marketDataCacheService.get(TEST_SYMBOL + "_" + TEST_INTERVAL)).thenReturn(Optional.empty());
        when(marketDataRepository.findBySymbolAndTimestamp(TEST_SYMBOL, timestamp)).thenReturn(Optional.of(mockData));
        Optional<Candle> result = marketDataService.getLatestMarketData(TEST_SYMBOL, TEST_INTERVAL, timestamp);
        assertTrue(result.isPresent());
        verify(marketDataCacheService).cacheLatestCandles(eq(TEST_SYMBOL), eq(TEST_INTERVAL), anyString());
    }

    @Test
    void getLatestMarketData_validCacheMiss_repositoryMiss_returnsEmpty() {
        Long timestamp = 123L;
        when(marketDataCacheService.get(TEST_SYMBOL + "_" + TEST_INTERVAL)).thenReturn(Optional.empty());
        when(marketDataRepository.findBySymbolAndTimestamp(TEST_SYMBOL, timestamp)).thenReturn(Optional.empty());
        Optional<Candle> result = marketDataService.getLatestMarketData(TEST_SYMBOL, TEST_INTERVAL, timestamp);
        assertTrue(result.isEmpty());
    }

    @Test
    void getLatestMarketData_invalidSymbol_returnsEmpty() {
        Optional<Candle> result = marketDataService.getLatestMarketData("", TEST_INTERVAL, 123L);
        assertTrue(result.isEmpty());
    }

    @Test
    void getLatestMarketData_invalidInterval_returnsEmpty() {
        Optional<Candle> result = marketDataService.getLatestMarketData(TEST_SYMBOL, "INVALID", 123L);
        assertTrue(result.isEmpty());
    }

    @Test
    void getHistoricalMarketData_valid_returnsData() {
        List<Candle> mockList = List.of(mock(Candle.class));
        when(marketDataRepository.findBySymbol(TEST_SYMBOL)).thenReturn(mockList);
        List<Candle> result = marketDataService.getHistoricalMarketData(TEST_SYMBOL, TEST_INTERVAL);
        assertEquals(1, result.size());
    }

    @Test
    void getHistoricalMarketData_invalidSymbol_returnsEmptyList() {
        List<Candle> result = marketDataService.getHistoricalMarketData("", TEST_INTERVAL);
        assertTrue(result.isEmpty());
    }

    @Test
    void getHistoricalMarketData_invalidInterval_returnsEmptyList() {
        List<Candle> result = marketDataService.getHistoricalMarketData(TEST_SYMBOL, "INVALID");
        assertTrue(result.isEmpty());
    }

    @Test
    void getHistoricalMarketData_noData_returnsEmptyList() {
        when(marketDataRepository.findBySymbol(TEST_SYMBOL)).thenReturn(List.of());
        List<Candle> result = marketDataService.getHistoricalMarketData(TEST_SYMBOL, TEST_INTERVAL);
        assertTrue(result.isEmpty());
    }
}
