package com.beartrail.marketdata.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ohlc_candles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Candle {

    @Id
    @Column(name="candle_id", nullable = false)
    private String id;




}
