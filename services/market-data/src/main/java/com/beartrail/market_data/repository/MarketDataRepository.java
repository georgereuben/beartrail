package com.beartrail.market_data.repository;

import com.beartrail.market_data.model.entity.MarketData;
import com.beartrail.market_data.model.entity.TimeInterval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarketDataRepository extends JpaRepository<MarketData, UUID> {

    List<MarketData> findBySymbolAndTimeInterval(String symbol, String timeInterval);

    Optional<MarketData> findBySymbolAndTimeIntervalAndTimestamp(
            String symbol,
            TimeInterval interval,
            Long timestamp
    );
}
