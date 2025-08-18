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
    private Long candleId;

    @Id
    @Column(nullable = true)
    private Instant timestamp = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES); // TODO: check starting of all minutes

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)  // TODO: this is to make sure stock is persisted when candle is saved, check once
    @JoinColumn(name = "stock_id", referencedColumnName = "stock_id", nullable = true)
    private Stock stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timeframe_id", referencedColumnName = "timeframe_id", nullable = true)
    private TimeFrame timeFrame;

    @Column(name = "open_price", precision = 15, scale = 4, nullable = true)
    private BigDecimal openPrice;

    @Column(name = "high_price", precision = 15, scale = 4, nullable = true)
    private BigDecimal highPrice;

    @Column(name = "low_price", precision = 15, scale = 4, nullable = true)
    private BigDecimal lowPrice;

    @Column(name = "close_price", precision = 15, scale = 4, nullable = true)
    private BigDecimal closePrice;

    @Builder.Default
    @Column(name = "volume", nullable = true)
    private Long volume = 0L;

    @Column(name = "created_at", nullable = true)
    @CreationTimestamp
    private Instant createdAt;
}