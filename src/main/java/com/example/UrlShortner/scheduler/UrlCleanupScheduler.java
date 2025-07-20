package com.example.UrlShortner.scheduler;

import com.example.UrlShortner.service.UrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UrlCleanupScheduler {
    
    private final UrlService urlService;
    

    @Scheduled(fixedRate = 14400000) // Delete expired code every 4 hours
    public void cleanupExpiredUrls() {
        try {
            log.info("Starting scheduled cleanup of expired URLs");
            urlService.deleteExpiredUrls();
            log.info("Completed scheduled cleanup of expired URLs");
        } catch (Exception e) {
            log.error("Error during scheduled URL cleanup", e);
        }
    }
} 