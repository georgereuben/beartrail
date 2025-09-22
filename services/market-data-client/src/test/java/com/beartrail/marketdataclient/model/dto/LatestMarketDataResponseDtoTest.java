package com.beartrail.marketdataclient.model.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LatestMarketDataResponseDtoTest {

    @Test
    void testNoArgsConstructor() {
        // When
        LatestMarketDataResponseDto dto = new LatestMarketDataResponseDto();

        // Then
        assertNotNull(dto);
        assertNull(dto.getStatus());
        assertNull(dto.getData());
    }

    @Test
    void testAllArgsConstructor() {
        // Given
        String status = "success";
        PriceUpdateDto ohlc = PriceUpdateDto.builder()
                .openPrice(new BigDecimal("100.00"))
                .closePrice(new BigDecimal("102.00"))
                .build();
        DataDto dataDto = new DataDto(102.50, "12345", ohlc, ohlc);
        Map<String, DataDto> data = Map.of("NSE_EQ|INE002A01018", dataDto);

        // When
        LatestMarketDataResponseDto dto = new LatestMarketDataResponseDto(status, data);

        // Then
        assertEquals(status, dto.getStatus());
        assertEquals(data, dto.getData());
        assertEquals(1, dto.getData().size());
        assertTrue(dto.getData().containsKey("NSE_EQ|INE002A01018"));
    }

    @Test
    void testSettersAndGetters() {
        // Given
        LatestMarketDataResponseDto dto = new LatestMarketDataResponseDto();
        String status = "error";
        Map<String, DataDto> data = Map.of();

        // When
        dto.setStatus(status);
        dto.setData(data);

        // Then
        assertEquals(status, dto.getStatus());
        assertEquals(data, dto.getData());
        assertTrue(dto.getData().isEmpty());
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        PriceUpdateDto ohlc = PriceUpdateDto.builder()
                .openPrice(new BigDecimal("100.00"))
                .build();
        DataDto dataDto = new DataDto(102.50, "12345", ohlc, ohlc);
        Map<String, DataDto> data = Map.of("TEST", dataDto);

        LatestMarketDataResponseDto dto1 = new LatestMarketDataResponseDto("success", data);
        LatestMarketDataResponseDto dto2 = new LatestMarketDataResponseDto("success", data);
        LatestMarketDataResponseDto dto3 = new LatestMarketDataResponseDto("error", data);

        // Then
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }

    @Test
    void testWithMultipleDataEntries() {
        // Given
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

        Map<String, DataDto> data = Map.of(
                "NSE_EQ|INE002A01018", dataDto1,
                "NSE_EQ|INE009A01021", dataDto2
        );

        // When
        LatestMarketDataResponseDto dto = new LatestMarketDataResponseDto("success", data);

        // Then
        assertEquals("success", dto.getStatus());
        assertEquals(2, dto.getData().size());
        assertTrue(dto.getData().containsKey("NSE_EQ|INE002A01018"));
        assertTrue(dto.getData().containsKey("NSE_EQ|INE009A01021"));
    }
}
