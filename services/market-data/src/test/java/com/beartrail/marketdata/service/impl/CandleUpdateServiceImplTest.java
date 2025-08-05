package com.beartrail.marketdata.service.impl;

import com.beartrail.marketdata.model.entity.TimeInterval;
import com.beartrail.marketdata.model.entity.MarketData;
import com.beartrail.marketdata.repository.MarketDataRepository;
import com.beartrail.marketdata.service.InstrumentKeyLoader;
import com.beartrail.marketdata.client.upstox.UpstoxApiClient;
import com.beartrail.marketdata.service.MarketDataKafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CandleUpdateServiceImplTest {
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
    private CandleUpdateServiceImpl candleUpdateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        candleUpdateService = new CandleUpdateServiceImpl();
        injectField("instrumentKeyLoader", instrumentKeyLoader);
        injectField("upstoxApiClient", upstoxApiClient);
        injectField("marketDataRepository", marketDataRepository);
        injectField("marketDataCacheService", marketDataCacheService);
        injectField("marketDataService", marketDataService);
        injectField("marketDataKafkaProducer", marketDataKafkaProducer);
    }

    private void injectField(String fieldName, Object mock) {
        try {
            java.lang.reflect.Field field = CandleUpdateServiceImpl.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(candleUpdateService, mock);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void updateCandlesForInterval_validInterval_processesBatchesAndSavesData() {
        TimeInterval interval = TimeInterval.ONE_MINUTE;
        List<String> symbols = Arrays.asList("RELIANCE", "TCS");
        when(instrumentKeyLoader.getInstrumentKeys()).thenReturn(symbols);
        MarketData marketData1 = mock(MarketData.class);
        MarketData marketData2 = mock(MarketData.class);
        when(marketData1.getSymbol()).thenReturn("RELIANCE");
        when(marketData2.getSymbol()).thenReturn("TCS");
        List<MarketData> marketDataList = Arrays.asList(marketData1, marketData2);
        when(upstoxApiClient.getMarketData(symbols, "I1")).thenReturn(marketDataList);
        candleUpdateService.updateCandlesForInterval(interval);
        verify(upstoxApiClient).getMarketData(symbols, "I1");
        verify(marketDataRepository, times(2)).save(any(MarketData.class));
        verify(marketDataCacheService).cacheLatestMarketData(eq("RELIANCE"), eq("I1"), anyString());
        verify(marketDataCacheService).cacheLatestMarketData(eq("TCS"), eq("I1"), anyString());
    }

    @Test
    void updateCandlesForInterval_nullInterval_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> candleUpdateService.updateCandlesForInterval(null));
    }

    @Test
    void updateCandlesForInterval_emptyInstrumentKeys_throwsException() {
        TimeInterval interval = TimeInterval.ONE_MINUTE;
        when(instrumentKeyLoader.getInstrumentKeys()).thenReturn(Collections.emptyList());
        assertThrows(RuntimeException.class, () -> candleUpdateService.updateCandlesForInterval(interval));
    }

    @Test
    void updateCandlesForInterval_marketDataListEmpty_noException() {
        TimeInterval interval = TimeInterval.ONE_MINUTE;
        List<String> symbols = Arrays.asList("RELIANCE");
        when(instrumentKeyLoader.getInstrumentKeys()).thenReturn(symbols);
        when(upstoxApiClient.getMarketData(symbols, "I1")).thenReturn(Collections.emptyList());
        assertDoesNotThrow(() -> candleUpdateService.updateCandlesForInterval(interval));
        verify(upstoxApiClient).getMarketData(symbols, "I1");
        verifyNoInteractions(marketDataRepository);
        verifyNoInteractions(marketDataCacheService);
    }

    @Test
    void updateCandlesForInterval_batchProcessing_worksForLargeSymbolList() {
        TimeInterval interval = TimeInterval.ONE_MINUTE;
        List<String> symbols = Mockito.mock(List.class);
        when(symbols.size()).thenReturn(1001);
        when(instrumentKeyLoader.getInstrumentKeys()).thenReturn(symbols);
        when(symbols.subList(anyInt(), anyInt())).thenReturn(Arrays.asList("RELIANCE"));
        when(upstoxApiClient.getMarketData(anyList(), eq("I1"))).thenReturn(Arrays.asList(mock(MarketData.class)));
        candleUpdateService.updateCandlesForInterval(interval);
        verify(upstoxApiClient, atLeastOnce()).getMarketData(anyList(), eq("I1"));
    }

    @Test
    void updateCandlesForInterval_marketDataRepositoryThrows_exceptionPropagates() {
        TimeInterval interval = TimeInterval.ONE_MINUTE;
        List<String> symbols = Arrays.asList("RELIANCE");
        when(instrumentKeyLoader.getInstrumentKeys()).thenReturn(symbols);
        MarketData marketData = mock(MarketData.class);
        when(upstoxApiClient.getMarketData(symbols, "I1")).thenReturn(Arrays.asList(marketData));
        doThrow(new RuntimeException("DB error")).when(marketDataRepository).save(any(MarketData.class));
        assertThrows(RuntimeException.class, () -> candleUpdateService.updateCandlesForInterval(interval));
    }

    @Test
    void updateCandlesForSymbol_noLogic_noException() {
        assertDoesNotThrow(() -> candleUpdateService.updateCandlesForSymbol("RELIANCE", TimeInterval.ONE_MINUTE));
    }

    @Test
    void calculateCompletedIntervalTimestamp_returnsZero() {
        long result = candleUpdateService.calculateCompletedIntervalTimestamp(TimeInterval.ONE_MINUTE);
        assertEquals(0, result);
    }
}
