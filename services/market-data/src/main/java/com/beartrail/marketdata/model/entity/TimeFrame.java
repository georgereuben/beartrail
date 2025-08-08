package com.beartrail.marketdata.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeFrame {

    @Id
    @Column(name = "timeframe_id", nullable = false)
    private String id;

    @Column(name = "name", nullable = false)
    private String name; // like ONE_MINUTE, THIRTY_MINUTES, ONE_DAY

    @Column(name = "value", nullable = false)
    private TimeFrameValue value; // like I1, I30, 1d
}

@Getter
enum TimeFrameValue {
    I1("I1"),
    I30("I30"),
    ONE_DAY("1d");

    private final String value;

    TimeFrameValue(String value) {
        this.value = value;
    }
}