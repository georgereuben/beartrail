CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;

create table stocks (
    stock_id SERIAL PRIMARY KEY,
    instrument_token VARCHAR(64) NOT NULL UNIQUE
);

create table timeframes (
    timeframe_id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE,
    value VARCHAR(64) NOT NULL UNIQUE
);

insert into timeframes (name, value) values
    ('1 minute', 'I1'),
    ('30 minutes', 'I30'),
    ('1 day', '1d');

create table ohlc_candles (
    candle_id BIGSERIAL,
    stock_id INTEGER NOT NULL REFERENCES stocks(stock_id) ON DELETE CASCADE,
    timeframe_id INTEGER NOT NULL REFERENCES timeframes(timeframe_id),
    timestamp TIMESTAMPTZ NOT NULL,
    open_price DECIMAL(15,4) NOT NULL CHECK (open_price > 0),
    high_price DECIMAL(15,4) NOT NULL CHECK (high_price > 0),
    low_price DECIMAL(15,4) NOT NULL CHECK (low_price > 0),
    close_price DECIMAL(15,4) NOT NULL CHECK (close_price > 0),
    volume BIGINT NOT NULL DEFAULT 0 CHECK (volume >= 0),
    created_at TIMESTAMPTZ DEFAULT NOW(),

    PRIMARY KEY (stock_id, timeframe_id, timestamp),

    constraint check_high_low CHECK (high_price >= low_price),
    constraint check_ohlc_logic CHECK (
        high_price >= open_price AND high_price >= close_price
        AND low_price <= open_price AND low_price <= close_price
    )
);