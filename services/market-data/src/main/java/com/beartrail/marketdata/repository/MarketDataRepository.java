package com.beartrail.marketdata.repository;

import com.beartrail.marketdata.model.entity.Candle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarketDataRepository extends JpaRepository<Candle, UUID> {

    List<Candle> findByStock_Symbol(String symbol);

    Optional<Candle> findByStock_SymbolAndTimestamp(
            String symbol,
            Instant timestamp
    );
}
