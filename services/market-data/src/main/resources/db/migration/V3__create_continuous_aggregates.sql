-- Continuous aggregate for 5-minute candles from 1-minute data
CREATE MATERIALIZED VIEW ohlc_5m_candles
WITH (timescaledb.continuous) AS
SELECT
    time_bucket('5 minutes', timestamp) AS bucket,
    stock_id,
    FIRST(open_price, timestamp) AS open_price,
    MAX(high_price) AS high_price,
    MIN(low_price) AS low_price,
    LAST(close_price, timestamp) AS close_price,
    SUM(volume) AS volume,
    COUNT(*) AS candle_count
FROM ohlc_candles
GROUP BY bucket, stock_id
WITH NO DATA;

-- Continuous aggregate for 30-minute candles
CREATE MATERIALIZED VIEW ohlc_30m_candles
WITH (timescaledb.continuous) AS
SELECT
    time_bucket('30 minutes', timestamp) AS bucket,
    stock_id,
    FIRST(open_price, timestamp) AS open_price,
    MAX(high_price) AS high_price,
    MIN(low_price) AS low_price,
    LAST(close_price, timestamp) AS close_price,
    SUM(volume) AS volume,
    COUNT(*) AS candle_count
FROM ohlc_candles
GROUP BY bucket, stock_id
WITH NO DATA;

-- Continuous aggregate for 1-hour candles
CREATE MATERIALIZED VIEW ohlc_1h_candles
WITH (timescaledb.continuous) AS
SELECT
    time_bucket('1 hour', timestamp) AS bucket,
    stock_id,
    FIRST(open_price, timestamp) AS open_price,
    MAX(high_price) AS high_price,
    MIN(low_price) AS low_price,
    LAST(close_price, timestamp) AS close_price,
    SUM(volume) AS volume,
    COUNT(*) AS candle_count
FROM ohlc_candles
GROUP BY bucket, stock_id
WITH NO DATA;

-- Daily candles continuous aggregate
CREATE MATERIALIZED VIEW ohlc_1d_candles
WITH (timescaledb.continuous) AS
SELECT
    time_bucket('1 day', timestamp) AS bucket,
    stock_id,
    FIRST(open_price, timestamp) AS open_price,
    MAX(high_price) AS high_price,
    MIN(low_price) AS low_price,
    LAST(close_price, timestamp) AS close_price,
    SUM(volume) AS volume,
    COUNT(*) AS candle_count
FROM ohlc_candles
GROUP BY bucket, stock_id
WITH NO DATA;
