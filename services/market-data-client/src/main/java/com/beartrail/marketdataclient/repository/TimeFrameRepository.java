package com.beartrail.marketdataclient.repository;

import com.beartrail.marketdataclient.model.entity.TimeFrame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeFrameRepository extends JpaRepository<TimeFrame, Long> {
    TimeFrame findByValue(String value);
}
