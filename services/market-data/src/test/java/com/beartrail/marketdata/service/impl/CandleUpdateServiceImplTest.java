package com.beartrail.marketdata.service.impl;

import com.beartrail.marketdata.model.entity.Candle;
import com.beartrail.marketdata.model.entity.TimeFrameValue;
import com.beartrail.marketdata.model.entity.TimeFrameValue;
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
        TimeFrameValue interval = TimeFrameValue.ONE_MINUTE;
        List<String> symbols = Arrays.asList(TEST_SYMBOL, "TCS");
        when(instrumentKeyLoader.getInstrumentKeys()).thenReturn(symbols);
        Candle candle1 = mock(Candle.class);
        Candle candle2 = mock(Candle.class);
        when(candle1.getStock().getSymbol()).thenReturn(TEST_SYMBOL);
        when(candle2.getStock().getSymbol()).thenReturn("TCS");
        List<Candle> candleList = Arrays.asList(candle1, candle2);
        when(upstoxApiClient.getCandles(symbols, "I1")).thenReturn(candleList);
        candleUpdateService.updateCandlesForInterval(interval);
        verify(upstoxApiClient).getCandles(symbols, "I1");
        verify(marketDataRepository, times(2)).save(any(Candle.class));
        verify(marketDataCacheService).cacheLatestCandles(eq(TEST_SYMBOL), eq("I1"), anyString());
        verify(marketDataCacheService).cacheLatestCandles(eq("TCS"), eq("I1"), anyString());
    }

    @Test
    void updateCandlesForInterval_nullInterval_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> candleUpdateService.updateCandlesForInterval(null));
    }

    @Test
    void updateCandlesForInterval_emptyInstrumentKeys_throwsException() {
        when(instrumentKeyLoader.getInstrumentKeys()).thenReturn(Collections.emptyList());
        assertThrows(RuntimeException.class, () -> candleUpdateService.updateCandlesForInterval(TimeFrameValue.ONE_MINUTE));
    }

    @Test
    void updateCandlesForInterval_marketDataListEmpty_noException() {
        List<String> symbols = Arrays.asList(TEST_SYMBOL);
        when(instrumentKeyLoader.getInstrumentKeys()).thenReturn(symbols);
        when(upstoxApiClient.getCandles(symbols, "I1")).thenReturn(Collections.emptyList());
        assertDoesNotThrow(() -> candleUpdateService.updateCandlesForInterval(TimeFrameValue.ONE_MINUTE));
        verify(upstoxApiClient).getCandles(symbols, "I1");
        verifyNoInteractions(marketDataRepository);
        verifyNoInteractions(marketDataCacheService);
    }

    @Test
    void updateCandlesForInterval_batchProcessing_worksForLargeSymbolList() {
        List<String> symbols = Mockito.mock(List.class);
        when(symbols.size()).thenReturn(1001);
        when(instrumentKeyLoader.getInstrumentKeys()).thenReturn(symbols);
        when(symbols.subList(anyInt(), anyInt())).thenReturn(Arrays.asList(TEST_SYMBOL));
        when(upstoxApiClient.getCandles(anyList(), eq("I1"))).thenReturn(Arrays.asList(mock(Candle.class)));
        candleUpdateService.updateCandlesForInterval(TimeFrameValue.ONE_MINUTE);
        verify(upstoxApiClient, atLeastOnce()).getCandles(anyList(), eq("I1"));
    }

    @Test
    void updateCandlesForInterval_marketDataRepositoryThrows_exceptionPropagates() {
        List<String> symbols = Arrays.asList(TEST_SYMBOL);
        when(instrumentKeyLoader.getInstrumentKeys()).thenReturn(symbols);
        Candle candle = mock(Candle.class);
        when(upstoxApiClient.getCandles(symbols, "I1")).thenReturn(Arrays.asList(candle));
        doThrow(new RuntimeException("DB error")).when(marketDataRepository).save(any(Candle.class));
        assertThrows(RuntimeException.class, () -> candleUpdateService.updateCandlesForInterval(TimeFrameValue.ONE_MINUTE));
    }

    @Test
    void updateCandlesForSymbol_noLogic_noException() {
        assertDoesNotThrow(() -> candleUpdateService.updateCandlesForSymbol(TEST_SYMBOL, TimeFrameValue.ONE_MINUTE));
    }

    @Test
    void calculateCompletedIntervalTimestamp_returnsZero() {
        long result = candleUpdateService.calculateCompletedIntervalTimestamp(TimeFrameValue.ONE_MINUTE);
        assertEquals(0, result);
    }
}
