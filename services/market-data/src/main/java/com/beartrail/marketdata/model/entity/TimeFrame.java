package com.beartrail.marketdata.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "timeframes")
public class TimeFrame {

    @Id
    @Column(name = "timeframe_id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name; // like ONE_MINUTE, THIRTY_MINUTES, ONE_DAY

    @Column(name = "value", nullable = false)
    private String value; // like I1, I30, 1d

    // helper method to get tfv enum from string value
    public TimeFrameValue getTimeFrameValue() {
        return TimeFrameValue.fromValue(this.value);
    }
}