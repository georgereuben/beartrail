package com.beartrail.marketdata.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "stocks")
public class Stock extends Instrument {

    @Id
    @Column(name = "stock_id", nullable = false)
    private String id;

    @Column(name = "symbol", nullable = false, unique = true)
    private String symbol;

    @Column(name="instrument_token", nullable = false)
    private String instrumentToken;

    @Column(name="last_price", nullable = false)
    private Double lastPrice;
}
