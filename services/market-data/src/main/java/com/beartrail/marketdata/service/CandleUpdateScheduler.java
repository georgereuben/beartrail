package com.beartrail.marketdata.service;

import com.beartrail.marketdata.model.entity.TimeInterval;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CandleUpdateScheduler {

    private final CandleUpdateService candleUpdateService;

    public CandleUpdateScheduler(CandleUpdateService candleUpdateService) {
        this.candleUpdateService = candleUpdateService;
    }

    @Scheduled(cron = "0 0/1 * * * ?") // Runs every minute
    public void oneMinuteCandleUpdate() {
        log.info("Starting one minute candle update for all intervals");
        for (TimeInterval interval : TimeInterval.values()) {
            candleUpdateService.updateCandlesForInterval(interval);
        }
        log.info("Completed one minute candle update for all intervals");
    }

    @Scheduled(cron = "0 0/5 * * * ?") // Runs every 5 minutes
    public void fiveMinuteCandleUpdate() {
        log.info("Starting candle update for all intervals");
        for (TimeInterval interval : TimeInterval.values()) {
            candleUpdateService.updateCandlesForInterval(interval);
        }
        log.info("Completed candle update for all intervals");
    }

    @Scheduled(cron = "0 0/30 * * * ?") // Runs every 30 minutes
    public void thirtyMinuteCandleUpdate() {
        log.info("Starting thirty minute candle update for all intervals");
        for (TimeInterval interval : TimeInterval.values()) {
            candleUpdateService.updateCandlesForInterval(interval);
        }
        log.info("Completed thirty minute candle update for all intervals");
    }

    @Scheduled(cron = "0 0 0 * * ?")   // Runs daily at midnight
    public void dailyCandleUpdate() {
        log.info("Starting daily candle update for all intervals");
        for (TimeInterval interval : TimeInterval.values()) {
            candleUpdateService.updateCandlesForInterval(interval);
        }
        log.info("Completed daily candle update for all intervals");
    }
}
