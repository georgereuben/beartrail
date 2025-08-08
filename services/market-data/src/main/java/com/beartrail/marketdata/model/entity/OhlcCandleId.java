package com.beartrail.marketdata.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OhlcCandleId implements Serializable {
    private Long candleId;
    private Instant timestamp;
}