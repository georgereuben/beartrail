package com.beartrail.marketdataclient.model.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class PriceUpdateDtoTest {

    @Test
    void testBuilderPattern() {
        // When
        PriceUpdateDto dto = PriceUpdateDto.builder()
                .openPrice(new BigDecimal("100.50"))
                .highPrice(new BigDecimal("105.75"))
                .lowPrice(new BigDecimal("98.25"))
                .closePrice(new BigDecimal("103.00"))
                .volume(15000L)
                .build();

        // Then
        assertNotNull(dto);
        assertEquals(new BigDecimal("100.50"), dto.getOpenPrice());
        assertEquals(new BigDecimal("105.75"), dto.getHighPrice());
        assertEquals(new BigDecimal("98.25"), dto.getLowPrice());
        assertEquals(new BigDecimal("103.00"), dto.getClosePrice());
        assertEquals(15000L, dto.getVolume());
    }

    @Test
    void testNoArgsConstructor() {
        // When
        PriceUpdateDto dto = new PriceUpdateDto();

        // Then
        assertNotNull(dto);
        assertNull(dto.getOpenPrice());
        assertNull(dto.getHighPrice());
        assertNull(dto.getLowPrice());
        assertNull(dto.getClosePrice());
        assertNull(dto.getVolume());
    }

    @Test
    void testAllArgsConstructor() {
        // Given
        BigDecimal open = new BigDecimal("100.00");
        BigDecimal high = new BigDecimal("110.00");
        BigDecimal low = new BigDecimal("95.00");
        BigDecimal close = new BigDecimal("105.00");
        Long volume = 20000L;
        Instant timestamp = Instant.now();

        // When
        PriceUpdateDto dto = new PriceUpdateDto(open, high, low, close, volume, timestamp);

        // Then
        assertEquals(open, dto.getOpenPrice());
        assertEquals(high, dto.getHighPrice());
        assertEquals(low, dto.getLowPrice());
        assertEquals(close, dto.getClosePrice());
        assertEquals(volume, dto.getVolume());
        assertEquals(timestamp, dto.getTimestamp());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        PriceUpdateDto dto = new PriceUpdateDto();
        BigDecimal open = new BigDecimal("50.25");
        BigDecimal high = new BigDecimal("55.75");
        BigDecimal low = new BigDecimal("48.50");
        BigDecimal close = new BigDecimal("52.00");
        Long volume = 8500L;

        // When
        dto.setOpenPrice(open);
        dto.setHighPrice(high);
        dto.setLowPrice(low);
        dto.setClosePrice(close);
        dto.setVolume(volume);

        // Then
        assertEquals(open, dto.getOpenPrice());
        assertEquals(high, dto.getHighPrice());
        assertEquals(low, dto.getLowPrice());
        assertEquals(close, dto.getClosePrice());
        assertEquals(volume, dto.getVolume());
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        PriceUpdateDto dto1 = PriceUpdateDto.builder()
                .openPrice(new BigDecimal("100.00"))
                .highPrice(new BigDecimal("105.00"))
                .lowPrice(new BigDecimal("98.00"))
                .closePrice(new BigDecimal("102.00"))
                .volume(1000L)
                .build();

        PriceUpdateDto dto2 = PriceUpdateDto.builder()
                .openPrice(new BigDecimal("100.00"))
                .highPrice(new BigDecimal("105.00"))
                .lowPrice(new BigDecimal("98.00"))
                .closePrice(new BigDecimal("102.00"))
                .volume(1000L)
                .build();

        PriceUpdateDto dto3 = PriceUpdateDto.builder()
                .openPrice(new BigDecimal("200.00"))
                .highPrice(new BigDecimal("205.00"))
                .lowPrice(new BigDecimal("198.00"))
                .closePrice(new BigDecimal("202.00"))
                .volume(2000L)
                .build();

        // Then
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }
}
