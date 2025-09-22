package com.beartrail.marketdata.model.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OhlcCandleId implements Serializable {
  private static final long serialVersionUID = 1L;
  private UUID candleId;
  private Instant timestamp;
}
