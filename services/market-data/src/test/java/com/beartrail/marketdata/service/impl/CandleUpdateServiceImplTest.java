package com.beartrail.marketdata.service.impl;

import com.beartrail.marketdata.model.entity.TimeInterval;
import com.beartrail.marketdata.model.entity.MarketData;
import com.beartrail.marketdata.repository.MarketDataRepository;
import com.beartrail.marketdata.service.InstrumentKeyLoader;
import com.beartrail.marketdata.client.upstox.UpstoxApiClient;
import com.beartrail.marketdata.service.MarketDataKafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CandleUpdateServiceImplTest {

    private static final String TEST_SYMBOL = "RELIANCE";

    @Mock
    private MarketDataServiceImpl marketDataService;
    @Mock
    private MarketDataCacheServiceImpl marketDataCacheService;
    @Mock
    private MarketDataRepository marketDataRepository;
    @Mock
    private UpstoxApiClient upstoxApiClient;
    @Mock
    private InstrumentKeyLoader instrumentKeyLoader;
    @Mock
    private MarketDataKafkaProducer marketDataKafkaProducer;

    @InjectMocks
    private CandleUpdateServiceImpl candleUpdateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void updateCandlesForInterval_validInterval_processesBatchesAndSavesData() {
        TimeInterval interval = TimeInterval.ONE_MINUTE;
        List<String> symbols = Arrays.asList(TEST_SYMBOL, "TCS");
        when(instrumentKeyLoader.getInstrumentKeys()).thenReturn(symbols);
        MarketData marketData1 = mock(MarketData.class);
        MarketData marketData2 = mock(MarketData.class);
        when(marketData1.getSymbol()).thenReturn(TEST_SYMBOL);
        when(marketData2.getSymbol()).thenReturn("TCS");
        List<MarketData> marketDataList = Arrays.asList(marketData1, marketData2);
        when(upstoxApiClient.getMarketData(symbols, "I1")).thenReturn(marketDataList);
        candleUpdateService.updateCandlesForInterval(interval);
        verify(upstoxApiClient).getMarketData(symbols, "I1");
        verify(marketDataRepository, times(2)).save(any(MarketData.class));
        verify(marketDataCacheService).cacheLatestMarketData(eq(TEST_SYMBOL), eq("I1"), anyString());
        verify(marketDataCacheService).cacheLatestMarketData(eq("TCS"), eq("I1"), anyString());
    }

    @Test
    void updateCandlesForInterval_nullInterval_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> candleUpdateService.updateCandlesForInterval(null));
    }

    @Test
    void updateCandlesForInterval_emptyInstrumentKeys_throwsException() {
        when(instrumentKeyLoader.getInstrumentKeys()).thenReturn(Collections.emptyList());
        assertThrows(RuntimeException.class, () -> candleUpdateService.updateCandlesForInterval(TimeInterval.ONE_MINUTE));
    }

    @Test
    void updateCandlesForInterval_marketDataListEmpty_noException() {
        List<String> symbols = Arrays.asList(TEST_SYMBOL);
        when(instrumentKeyLoader.getInstrumentKeys()).thenReturn(symbols);
        when(upstoxApiClient.getMarketData(symbols, "I1")).thenReturn(Collections.emptyList());
        assertDoesNotThrow(() -> candleUpdateService.updateCandlesForInterval(TimeInterval.ONE_MINUTE));
        verify(upstoxApiClient).getMarketData(symbols, "I1");
        verifyNoInteractions(marketDataRepository);
        verifyNoInteractions(marketDataCacheService);
    }

    @Test
    void updateCandlesForInterval_batchProcessing_worksForLargeSymbolList() {
        List<String> symbols = Mockito.mock(List.class);
        when(symbols.size()).thenReturn(1001);
        when(instrumentKeyLoader.getInstrumentKeys()).thenReturn(symbols);
        when(symbols.subList(anyInt(), anyInt())).thenReturn(Arrays.asList(TEST_SYMBOL));
        when(upstoxApiClient.getMarketData(anyList(), eq("I1"))).thenReturn(Arrays.asList(mock(MarketData.class)));
        candleUpdateService.updateCandlesForInterval(TimeInterval.ONE_MINUTE);
        verify(upstoxApiClient, atLeastOnce()).getMarketData(anyList(), eq("I1"));
    }

    @Test
    void updateCandlesForInterval_marketDataRepositoryThrows_exceptionPropagates() {
        List<String> symbols = Arrays.asList(TEST_SYMBOL);
        when(instrumentKeyLoader.getInstrumentKeys()).thenReturn(symbols);
        MarketData marketData = mock(MarketData.class);
        when(upstoxApiClient.getMarketData(symbols, "I1")).thenReturn(Arrays.asList(marketData));
        doThrow(new RuntimeException("DB error")).when(marketDataRepository).save(any(MarketData.class));
        assertThrows(RuntimeException.class, () -> candleUpdateService.updateCandlesForInterval(TimeInterval.ONE_MINUTE));
    }

    @Test
    void updateCandlesForSymbol_noLogic_noException() {
        assertDoesNotThrow(() -> candleUpdateService.updateCandlesForSymbol(TEST_SYMBOL, TimeInterval.ONE_MINUTE));
    }

    @Test
    void calculateCompletedIntervalTimestamp_returnsZero() {
        long result = candleUpdateService.calculateCompletedIntervalTimestamp(TimeInterval.ONE_MINUTE);
        assertEquals(0, result);
    }
}
