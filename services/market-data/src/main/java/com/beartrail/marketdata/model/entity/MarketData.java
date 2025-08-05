package com.beartrail.marketdata.model.entity;

import com.beartrail.marketdata.event.publisher.PriceUpdateEvent;
import com.beartrail.marketdata.model.dto.PriceUpdateDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Represents an OHLCV candle for a specific symbol at a given time interval.
 */
@Entity
@Table(name = "marketdata")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketData {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "symbol", nullable = true)
    private String symbol;

    @Column(name = "instrument_token", nullable = true)
    private String instrumentToken;

    @Column(name = "last_price", nullable = true)
    private Double lastPrice;

    @Column(name = "open_price", nullable = true)
    private Double openPrice;

    @Column(name = "high_price", nullable = true)
    private Double highPrice;

    @Column(name = "low_price", nullable = true)
    private Double lowPrice;

    @Column(name = "close_price", nullable = true)
    private Double closePrice;

    @Column(name = "volume", nullable = true)
    private Long volume;

    @Column(name = "timestamp", nullable = true)
    private Long timestamp;     // in ms from epoch

    @Enumerated(EnumType.STRING)
    @Column(name = "time_interval", nullable = true)           // TODO: make separate tables for each time interval
    private TimeInterval timeInterval;

    public MarketData(String symbol, Map<String, Double> lastPrice, Map<String, String> instrumentToken, Map<String, PriceUpdateDto> prevOhlc, Map<String, PriceUpdateDto> liveOhlc, String timeInterval) {
        this.symbol = symbol;
        this.instrumentToken = instrumentToken != null ? instrumentToken.get("instrument_token") : null;
        this.lastPrice = lastPrice != null ? lastPrice.get(symbol) : null;

        if (prevOhlc != null && prevOhlc.get(symbol) != null) {
            PriceUpdateDto prevData = prevOhlc.get(symbol);
            this.openPrice = prevData.getOpenPrice();
            this.highPrice = prevData.getHighPrice();
            this.lowPrice = prevData.getLowPrice();
            this.closePrice = prevData.getClosePrice();
            this.volume = prevData.getVolume();
            this.timestamp = prevData.getTimestamp();
        }

        this.timeInterval = timeInterval != null ? TimeInterval.fromValue(TimeInterval.valueOf(timeInterval)) : null;
    }

    public PriceUpdateEvent toPriceUpdateEvent(TimeInterval interval) {
        return PriceUpdateEvent.builder()
                .symbol(this.symbol)
                .lastPrice(this.lastPrice)
                .openPrice(this.openPrice)
                .highPrice(this.highPrice)
                .lowPrice(this.lowPrice)
                .closePrice(this.closePrice)
                .volume(this.volume)
                .timestamp(this.timestamp)
                .timeInterval(interval.name())
                .build();
    }
}
