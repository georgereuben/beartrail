-- to refresh 5m aggregates every 5 minutes with 1-hour lag
SELECT add_continuous_aggregate_policy('ohlc_5m_candles',
    start_offset => INTERVAL '10 minutes',
    end_offset => INTERVAL '0 minutes',
    schedule_interval => INTERVAL '5 minutes');

-- refresh 30m aggregates every 15 minutes with 1-hour lag
SELECT add_continuous_aggregate_policy('ohlc_30m_candles',
    start_offset => INTERVAL '1 hour',
    end_offset => INTERVAL '0 minutes',
    schedule_interval => INTERVAL '15 minutes');

-- refresh 1h aggregates every 30 minutes with 1-hour lag
SELECT add_continuous_aggregate_policy('ohlc_1h_candles',
    start_offset => INTERVAL '2 hours',
    end_offset => INTERVAL '0 minutes',
    schedule_interval => INTERVAL '30 minutes');

-- refresh daily aggregates every 6 hours with 1-day lag
SELECT add_continuous_aggregate_policy('ohlc_1d_candles',
    start_offset => INTERVAL '2 days',
    end_offset => INTERVAL '0 minutes',
    schedule_interval => INTERVAL '6 hours');

-- add a retention policy to automatically drop data older than 2 years
SELECT add_retention_policy('ohlc_candles', INTERVAL '2 years');