package com.example.demo.cinema.scheduler;

import com.example.demo.cinema.service.ShowtimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
    private final ShowtimeService showtimeService;

    @Autowired
    public ScheduledTasks(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
    }
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanupOldShowtimesHourly() { 
        log.info("SCHEDULER: Starting hourly cleanup of old showtimes...");
        try {
            showtimeService.cleanupOldShowtimes();
            log.info("SCHEDULER: Hourly cleanup of old showtimes completed successfully.");
        } catch (Exception e) {
            log.error("SCHEDULER: Error during hourly cleanup of old showtimes execution", e);
        }
    }
}