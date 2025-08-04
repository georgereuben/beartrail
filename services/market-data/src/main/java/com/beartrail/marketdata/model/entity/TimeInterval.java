package com.beartrail.marketdata.model.entity;

import lombok.Getter;

@Getter
public enum TimeInterval {
    ONE_MINUTE("1m"),
    FIVE_MINUTES("5m"),
    FIFTEEN_MINUTES("15m"),
    THIRTY_MINUTES("30m"),
    ONE_HOUR("1h"),
    FOUR_HOURS("4h"),
    ONE_DAY("1d"),
    ONE_WEEK("1w");

    private final String value;

    TimeInterval(String value) {
        this.value = value;
    }

    public static TimeInterval fromValue(TimeInterval timeInterval) {
        for (TimeInterval interval : TimeInterval.values()) {
            if (interval.getValue().equals(timeInterval.getValue())) {
                return interval;
            }
        }
        throw new IllegalArgumentException("Invalid time interval: " + timeInterval.getValue());
    }
}