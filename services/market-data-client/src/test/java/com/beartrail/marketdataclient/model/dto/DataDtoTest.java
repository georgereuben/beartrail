package com.beartrail.marketdataclient.model.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class DataDtoTest {

    @Test
    void testNoArgsConstructor() {
        // When
        DataDto dto = new DataDto();

        // Then
        assertNotNull(dto);
        assertNull(dto.getLastPrice());
        assertNull(dto.getInstrumentToken());
        assertNull(dto.getPrevOhlc());
        assertNull(dto.getLiveOhlc());
    }

    @Test
    void testAllArgsConstructor() {
        // Given
        Double lastPrice = 102.50;
        String instrumentToken = "12345";
        PriceUpdateDto prevOhlc = PriceUpdateDto.builder()
                .openPrice(new BigDecimal("100.00"))
                .closePrice(new BigDecimal("101.00"))
                .build();
        PriceUpdateDto liveOhlc = PriceUpdateDto.builder()
                .openPrice(new BigDecimal("101.00"))
                .closePrice(new BigDecimal("102.50"))
                .build();

        // When
        DataDto dto = new DataDto(lastPrice, instrumentToken, prevOhlc, liveOhlc);

        // Then
        assertEquals(lastPrice, dto.getLastPrice());
        assertEquals(instrumentToken, dto.getInstrumentToken());
        assertEquals(prevOhlc, dto.getPrevOhlc());
        assertEquals(liveOhlc, dto.getLiveOhlc());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        DataDto dto = new DataDto();
        Double lastPrice = 250.75;
        String instrumentToken = "67890";
        PriceUpdateDto ohlc = PriceUpdateDto.builder()
                .openPrice(new BigDecimal("250.00"))
                .closePrice(new BigDecimal("250.75"))
                .build();

        // When
        dto.setLastPrice(lastPrice);
        dto.setInstrumentToken(instrumentToken);
        dto.setPrevOhlc(ohlc);
        dto.setLiveOhlc(ohlc);

        // Then
        assertEquals(lastPrice, dto.getLastPrice());
        assertEquals(instrumentToken, dto.getInstrumentToken());
        assertEquals(ohlc, dto.getPrevOhlc());
        assertEquals(ohlc, dto.getLiveOhlc());
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        PriceUpdateDto ohlc = PriceUpdateDto.builder()
                .openPrice(new BigDecimal("100.00"))
                .closePrice(new BigDecimal("102.00"))
                .build();

        DataDto dto1 = new DataDto(102.50, "12345", ohlc, ohlc);
        DataDto dto2 = new DataDto(102.50, "12345", ohlc, ohlc);
        DataDto dto3 = new DataDto(200.75, "67890", ohlc, ohlc);

        // Then
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testWithNullValues() {
        // Given & When
        DataDto dto = new DataDto(null, null, null, null);

        // Then
        assertNull(dto.getLastPrice());
        assertNull(dto.getInstrumentToken());
        assertNull(dto.getPrevOhlc());
        assertNull(dto.getLiveOhlc());
    }

    @Test
    void testToString() {
        // Given
        PriceUpdateDto ohlc = PriceUpdateDto.builder()
                .openPrice(new BigDecimal("100.00"))
                .build();
        DataDto dto = new DataDto(102.50, "12345", ohlc, ohlc);

        // When
        String toString = dto.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("102.5"));
        assertTrue(toString.contains("12345"));
    }
}
