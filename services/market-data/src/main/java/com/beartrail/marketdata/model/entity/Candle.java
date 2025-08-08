package com.beartrail.marketdata.model.entity;

import com.beartrail.marketdata.event.publisher.PriceUpdateEvent;
import com.beartrail.marketdata.model.dto.PriceUpdateDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ohlc_candles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(OhlcCandleId.class)
public class Candle {

    @Id
    @Column(name="candle_id", nullable = false)
    private String id;

    @Id
    @Column(nullable = false)
    private Instant timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", referencedColumnName = "stock_id", nullable = false)
    private Stock stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timeframe_id", referencedColumnName = "timeframe_id", nullable = false)
    private TimeFrame timeFrame;

    @Column(name = "open_price", precision = 15, scale = 4, nullable = false)
    private BigDecimal openPrice;

    @Column(name = "high_price", precision = 15, scale = 4, nullable = false)
    private BigDecimal highPrice;

    @Column(name = "low_price", precision = 15, scale = 4, nullable = false)
    private BigDecimal lowPrice;

    @Column(name = "close_price", precision = 15, scale = 4, nullable = false)
    private BigDecimal closePrice;

    @Builder.Default
    @Column(name = "volume", nullable = false)
    private Long volume = 0L;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private Instant createdAt;

    public Candle(String symbol, Double lastPrice, String instrumentToken, PriceUpdateDto prevOhlc, PriceUpdateDto liveOhlc) {
        this.stock.setSymbol(symbol);
        this.stock.setInstrumentToken(instrumentToken);
        this.stock.setLastPrice(lastPrice);

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
                .symbol(this.stock.getSymbol())
                .lastPrice(this.stock.getLastPrice())
                .openPrice(this.openPrice)
                .highPrice(this.highPrice)
                .lowPrice(this.lowPrice)
                .closePrice(this.closePrice)
                .volume(this.volume)
                .timestamp(this.timestamp)
                .build();
    }
}