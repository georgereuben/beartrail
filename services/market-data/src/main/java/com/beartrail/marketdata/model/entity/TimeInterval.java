package com.beartrail.marketdata.model.entity;

import lombok.Getter;

@Getter
public enum TimeInterval {                  // only 1d, I1 and I30 supported by upstox api for now
    ONE_MINUTE("I1"),
    FIVE_MINUTES("I5"),
    FIFTEEN_MINUTES("I15"),
    THIRTY_MINUTES("I30"),
    ONE_HOUR("I60"),
    FOUR_HOURS("I240"),
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