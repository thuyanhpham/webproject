package com.example.demo.cinema.scheduler;

import com.example.demo.cinema.service.ShowtimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
    private final ShowtimeService showtimeService;

    @Autowired
    public ScheduledTasks(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
    }
    
    public void cleanupOldShowtimesTask() {
        try {
            showtimeService.cleanupOldShowtimesByMarkingAsDeleted();
        } catch (Exception e) {
            log.error("SCHEDULER: Error during scheduled cleanup of old showtimes", e);
        }
    }
}