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

    @Query("SELECT s.id FROM Stock s WHERE s.instrumentToken = :instrumentToken")
    Long findIdByInstrumentToken(@Param("instrumentToken") String instrumentToken);

    @Query("SELECT s FROM Stock s WHERE s.instrumentToken = :instrumentToken")
    Stock findByInstrumentToken(@Param("instrumentToken") String instrumentToken);

    @Modifying
    @Query(value = """
        INSERT INTO stocks (symbol, instrument_token, trading_name, last_price)
        VALUES (:symbol, :instrumentToken, :tradingName, :lastPrice)
        ON CONFLICT (symbol)
        DO UPDATE SET last_price = EXCLUDED.last_price
        """, nativeQuery = true)
    void upsertStock(@Param("symbol") String symbol,
                     @Param("instrumentToken") String instrumentToken,
                     @Param("tradingName") String tradingName,
                     @Param("lastPrice") BigDecimal lastPrice);

    @Modifying
    @Query("""
        UPDATE Stock s
        SET s.lastPrice = s.lastPrice
        WHERE s.symbol IN :symbols
        """)
    void updateLastPricesInBatch(@Param("symbols") Set<String> symbolsToUpdate);
}
