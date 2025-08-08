package com.beartrail.marketdata.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Stock extends Instrument {

    @Id
    @Column(name = "stock_id", nullable = false)
    private String id;

    @Column(name="instrument_token", nullable = false)
    private String instrumentToken;
}
