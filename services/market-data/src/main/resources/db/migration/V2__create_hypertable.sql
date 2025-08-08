CREATE EXTENSION IF NOT EXISTS timescaledb;

select create_hypertable(
    'ohlc_candles',
    'timestamp',
    chunk_time_interval => interval '1 day',
    create_default_indexes => FALSE
);

create index idx_ohlc_symbol_time_desc
on ohlc_candles (stock_id, timeframe_id, timestamp desc);

create index idx_ohlc_timeframe_symbol_time
on ohlc_candles (timeframe_id, stock_id, timestamp desc);

create index idx_ohlc_symbol_timeframe_time
on ohlc_candles (stock_id, timeframe_id, timestamp desc);

alter table ohlc_candles set (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'stock_id, timeframe_id',
    timescaledb.compress_orderby = 'timestamp DESC'
);

select add_compression_policy('ohlc_candles', interval '3 days');