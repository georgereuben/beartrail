package com.beartrail.marketdataclient.client.upstox;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.beartrail.marketdataclient.config.UpstoxConfig;
import com.beartrail.marketdataclient.event.publisher.PriceUpdateEvent;
import com.beartrail.marketdataclient.model.dto.DataDto;
import com.beartrail.marketdataclient.model.dto.LatestMarketDataResponseDto;
import com.beartrail.marketdataclient.model.dto.PriceUpdateDto;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class UpstoxApiClientTest {

    @Mock
    private UpstoxConfig upstoxConfig;

    @Mock
    private RestTemplate restTemplate;

    private UpstoxApiClient upstoxApiClient;

    private static final String BASE_URL = "https://api.upstox.com/v2";
    private static final String AUTH_TOKEN = "test-auth-token";

    @BeforeEach
    void setUp() {
        when(upstoxConfig.getBaseUrl()).thenReturn(BASE_URL);
        when(upstoxConfig.getAuthToken()).thenReturn(AUTH_TOKEN);
        upstoxApiClient = new UpstoxApiClient(upstoxConfig, restTemplate);
    }

    @Test
    void testGetPriceUpdateEvents_Success() {
        // Given
        List<String> symbolList = List.of("NSE_EQ|INE002A01018", "NSE_EQ|INE009A01021");
        String interval = "1minute";

        PriceUpdateDto prevOhlc = PriceUpdateDto.builder()
                .openPrice(new BigDecimal("100.00"))
                .highPrice(new BigDecimal("105.00"))
                .lowPrice(new BigDecimal("98.00"))
                .closePrice(new BigDecimal("102.00"))
                .volume(1000L)
                .build();

        PriceUpdateDto liveOhlc = PriceUpdateDto.builder()
                .openPrice(new BigDecimal("102.00"))
                .highPrice(new BigDecimal("107.00"))
                .lowPrice(new BigDecimal("101.00"))
                .closePrice(new BigDecimal("105.00"))
                .volume(1500L)
                .build();

        DataDto dataDto = new DataDto(105.50, "12345", prevOhlc, liveOhlc);
        Map<String, DataDto> dataMap = Map.of("NSE_EQ|INE002A01018", dataDto);

        LatestMarketDataResponseDto mockResponse = new LatestMarketDataResponseDto("success", dataMap);
        ResponseEntity<LatestMarketDataResponseDto> responseEntity =
                new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(LatestMarketDataResponseDto.class)
        )).thenReturn(responseEntity);

        // When
        List<PriceUpdateEvent> result = upstoxApiClient.getPriceUpdateEvents(symbolList, interval);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        PriceUpdateEvent event = result.get(0);
        assertEquals("NSE_EQ|INE002A01018", event.getSymbol());
        assertEquals("12345", event.getInstrumentToken());
        assertEquals(105.50, event.getLastPrice());
        assertEquals(prevOhlc, event.getPrevOhlc());
        assertEquals(liveOhlc, event.getLiveOhlc());

        // Verify REST call was made with correct parameters
        verify(restTemplate).exchange(
                contains("market-quote/ohlc"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(LatestMarketDataResponseDto.class)
        );
    }

    @Test
    void testGetPriceUpdateEvents_NullResponse() {
        // Given
        List<String> symbolList = List.of("NSE_EQ|INE002A01018");
        String interval = "1minute";

        ResponseEntity<LatestMarketDataResponseDto> responseEntity =
                new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(LatestMarketDataResponseDto.class)
        )).thenReturn(responseEntity);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> upstoxApiClient.getPriceUpdateEvents(symbolList, interval));

        assertEquals("No market data found for the given symbol and interval", exception.getMessage());
    }

    @Test
    void testGetPriceUpdateEvents_EmptyData() {
        // Given
        List<String> symbolList = List.of("NSE_EQ|INE002A01018");
        String interval = "1minute";

        LatestMarketDataResponseDto mockResponse = new LatestMarketDataResponseDto("success", Map.of());
        ResponseEntity<LatestMarketDataResponseDto> responseEntity =
                new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(LatestMarketDataResponseDto.class)
        )).thenReturn(responseEntity);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> upstoxApiClient.getPriceUpdateEvents(symbolList, interval));

        assertEquals("No market data found for the given symbol and interval", exception.getMessage());
    }

    @Test
    void testGetPriceUpdateEvents_RestTemplateException() {
        // Given
        List<String> symbolList = List.of("NSE_EQ|INE002A01018");
        String interval = "1minute";

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(LatestMarketDataResponseDto.class)
        )).thenThrow(new RuntimeException("Connection timeout"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> upstoxApiClient.getPriceUpdateEvents(symbolList, interval));

        assertEquals("Connection timeout", exception.getMessage());
    }

    @Test
    void testGetPriceUpdateEvents_MultipleSymbols() {
        // Given
        List<String> symbolList = List.of("NSE_EQ|INE002A01018", "NSE_EQ|INE009A01021");
        String interval = "1minute";

        PriceUpdateDto ohlc1 = PriceUpdateDto.builder()
                .openPrice(new BigDecimal("100.00"))
                .closePrice(new BigDecimal("102.00"))
                .build();

        PriceUpdateDto ohlc2 = PriceUpdateDto.builder()
                .openPrice(new BigDecimal("200.00"))
                .closePrice(new BigDecimal("205.00"))
                .build();

        DataDto dataDto1 = new DataDto(102.50, "12345", ohlc1, ohlc1);
        DataDto dataDto2 = new DataDto(205.75, "67890", ohlc2, ohlc2);

        Map<String, DataDto> dataMap = Map.of(
                "NSE_EQ|INE002A01018", dataDto1,
                "NSE_EQ|INE009A01021", dataDto2
        );

        LatestMarketDataResponseDto mockResponse = new LatestMarketDataResponseDto("success", dataMap);
        ResponseEntity<LatestMarketDataResponseDto> responseEntity =
                new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(LatestMarketDataResponseDto.class)
        )).thenReturn(responseEntity);

        // When
        List<PriceUpdateEvent> result = upstoxApiClient.getPriceUpdateEvents(symbolList, interval);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify both events are present
        assertTrue(result.stream().anyMatch(event -> event.getSymbol().equals("NSE_EQ|INE002A01018")));
        assertTrue(result.stream().anyMatch(event -> event.getSymbol().equals("NSE_EQ|INE009A01021")));
    }
}
