package com.beartrail.marketdata.repository;

import com.beartrail.marketdata.model.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Set;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Stock findBySymbol(String symbol);

    @Modifying
    @Query("""
        INSERT INTO stocks (symbol, instrument_token, trading_name, last_price)\s
        VALUES (:symbol, :instrumentToken, :tradingName, :lastPrice)
        ON DUPLICATE KEY UPDATE\s
        last_price = VALUES(last_price),
        updated_at = CURRENT_TIMESTAMP
       \s""")
    void upsertStock(@Param("symbol") String symbol,
                     @Param("instrumentToken") String instrumentToken,
                     @Param("tradingName") String tradingName,
                     @Param("lastPrice") BigDecimal lastPrice);

    void updateLastPricesInBatch(Set<String> symbolsToUpdate);
}

