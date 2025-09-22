package com.beartrail.marketdataclient.event.publisher;

import static org.junit.jupiter.api.Assertions.*;

import com.beartrail.marketdataclient.model.dto.PriceUpdateDto;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PriceUpdateEventTest {

    @Test
    void testBuilderPattern() {
        // Given
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

        // When
        PriceUpdateEvent event = PriceUpdateEvent.builder()
                .symbol("NSE_EQ|INE002A01018")
                .instrumentToken("12345")
                .lastPrice(105.50)
                .prevOhlc(prevOhlc)
                .liveOhlc(liveOhlc)
                .build();

        // Then
        assertNotNull(event);
        assertEquals("NSE_EQ|INE002A01018", event.getSymbol());
        assertEquals("12345", event.getInstrumentToken());
        assertEquals(105.50, event.getLastPrice());
        assertEquals(prevOhlc, event.getPrevOhlc());
        assertEquals(liveOhlc, event.getLiveOhlc());
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        PriceUpdateDto ohlc = PriceUpdateDto.builder()
                .openPrice(new BigDecimal("100.00"))
                .closePrice(new BigDecimal("102.00"))
                .build();

        PriceUpdateEvent event1 = PriceUpdateEvent.builder()
                .symbol("NSE_EQ|INE002A01018")
                .instrumentToken("12345")
                .lastPrice(105.50)
                .prevOhlc(ohlc)
                .liveOhlc(ohlc)
                .build();

        PriceUpdateEvent event2 = PriceUpdateEvent.builder()
                .symbol("NSE_EQ|INE002A01018")
                .instrumentToken("12345")
                .lastPrice(105.50)
                .prevOhlc(ohlc)
                .liveOhlc(ohlc)
                .build();

        PriceUpdateEvent event3 = PriceUpdateEvent.builder()
                .symbol("NSE_EQ|INE009A01021")
                .instrumentToken("67890")
                .lastPrice(200.75)
                .prevOhlc(ohlc)
                .liveOhlc(ohlc)
                .build();

        // Then
        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
        assertNotEquals(event1, event3);
        assertNotEquals(event1.hashCode(), event3.hashCode());
    }

    @Test
    void testToString() {
        // Given
        PriceUpdateEvent event = PriceUpdateEvent.builder()
                .symbol("NSE_EQ|INE002A01018")
                .instrumentToken("12345")
                .lastPrice(105.50)
                .build();

        // When
        String toString = event.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("NSE_EQ|INE002A01018"));
        assertTrue(toString.contains("12345"));
        assertTrue(toString.contains("105.5"));
    }

    @Test
    void testSettersAndGetters() {
        // Given
        PriceUpdateEvent event = PriceUpdateEvent.builder().build(); // Use builder instead of no-args constructor
        PriceUpdateDto ohlc = PriceUpdateDto.builder()
                .openPrice(new BigDecimal("100.00"))
                .build();

        // When
        event.setSymbol("TEST_SYMBOL");
        event.setInstrumentToken("54321");
        event.setLastPrice(150.25);
        event.setPrevOhlc(ohlc);
        event.setLiveOhlc(ohlc);

        // Then
        assertEquals("TEST_SYMBOL", event.getSymbol());
        assertEquals("54321", event.getInstrumentToken());
        assertEquals(150.25, event.getLastPrice());
        assertEquals(ohlc, event.getPrevOhlc());
        assertEquals(ohlc, event.getLiveOhlc());
    }
}
