package com.beartrail.marketdata.model.entity;

import lombok.Getter;

@Getter
public enum TimeFrameValue {
    ONE_MINUTE("I1"),
    THIRTY_MINUTES("I30"),
    ONE_DAY("1d");

    private final String value;

    TimeFrameValue(String value) {
        this.value = value;
    }
}