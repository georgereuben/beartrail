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

    @Column(name = "symbol", nullable = false)
    private String symbol;

    @Column(name = "instrument_token", nullable = false)
    private String instrumentToken;

    @Column(name = "last_price", nullable = false)
    private Double lastPrice;

    @Column(name = "open_price", nullable = false)
    private Double openPrice;

    @Column(name = "high_price", nullable = false)
    private Double highPrice;

    @Column(name = "low_price", nullable = false)
    private Double lowPrice;

    @Column(name = "close_price", nullable = false)
    private Double closePrice;

    @Column(name = "volume", nullable = false)
    private Long volume;

    @Column(name = "timestamp", nullable = false)
    private Long timestamp;     // in ms from epoch

    @Enumerated(EnumType.STRING)
    @Column(name = "time_interval", nullable = false)
    private TimeInterval timeInterval;

    public MarketData(String symbol, Map<String, Double> lastPrice, Map<String, String> instrumentToken, Map<String, PriceUpdateDto> prevOhlc, Map<String, PriceUpdateDto> liveOhlc, String timeInterval) {
        this.symbol = symbol;
        this.instrumentToken = instrumentToken.get("instrument_token");
        this.lastPrice = lastPrice.get(symbol);
        this.openPrice = prevOhlc.get(symbol).getOpenPrice();
        this.highPrice = prevOhlc.get(symbol).getHighPrice();
        this.lowPrice = prevOhlc.get(symbol).getLowPrice();
        this.closePrice = prevOhlc.get(symbol).getClosePrice();
        this.volume = prevOhlc.get(symbol).getVolume();
        this.timestamp = prevOhlc.get(symbol).getTimestamp();
        this.timeInterval = TimeInterval.fromValue(TimeInterval.valueOf(timeInterval));
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
