package com.beartrail.marketdata.model.entity;

import com.beartrail.marketdata.event.publisher.PriceUpdateEvent;
import com.beartrail.marketdata.model.dto.PriceUpdateDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @Column(name = "symbol", nullable = true)
    private String symbol;

    @Column(name = "instrument_token", nullable = true)         // TODO: change nullables back to false
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

//    @Enumerated(EnumType.STRING)
//    @Column(name = "time_interval", nullable = true)           // TODO: make separate tables for each time interval
//    private TimeInterval timeInterval;

    public MarketData(String symbol, Double lastPrice, String instrumentToken, PriceUpdateDto prevOhlc, PriceUpdateDto liveOhlc) {
        this.symbol = symbol;
        this.instrumentToken = instrumentToken;
        this.lastPrice = lastPrice;

        if (prevOhlc != null) {
            this.openPrice = prevOhlc.getOpenPrice();
            this.highPrice = prevOhlc.getHighPrice();
            this.lowPrice = prevOhlc.getLowPrice();
            this.closePrice = prevOhlc.getClosePrice();
            this.volume = prevOhlc.getVolume();
            this.timestamp = prevOhlc.getTimestamp();
        }
    }

    public PriceUpdateEvent toPriceUpdateEvent() {
        return PriceUpdateEvent.builder()
                .symbol(this.symbol)
                .lastPrice(this.lastPrice)
                .openPrice(this.openPrice)
                .highPrice(this.highPrice)
                .lowPrice(this.lowPrice)
                .closePrice(this.closePrice)
                .volume(this.volume)
                .timestamp(this.timestamp)
                .build();
    }
}
