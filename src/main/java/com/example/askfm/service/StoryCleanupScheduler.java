package com.example.askfm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StoryCleanupScheduler {
    private final StoryService storyService;

    /**
     * Запускается каждый час для удаления устаревших историй
     */
    @Scheduled(cron = "0 0 * * * *") // Каждый час
    // или @Scheduled(fixedRate = 3600000) // Каждый час в миллисекундах
    public void cleanupExpiredStories() {
        log.info("Starting scheduled cleanup of expired stories");
        try {
            storyService.deleteExpiredStories();
            log.info("Completed scheduled cleanup of expired stories");
        } catch (Exception e) {
            log.error("Error during scheduled cleanup of expired stories: {}", e.getMessage());
        }
    }
}
