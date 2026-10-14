package com.github.zolototrubovavv.feed.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.github.zolototrubovavv.feed.service.NewsCacheService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheWarmer {
    
    private final NewsCacheService cacheService;
    
    @EventListener(ApplicationReadyEvent.class)
    public void warmUpCache() {
        log.info("Executing cache warm-up -> Trigger: ApplicationReadyEvent");
        try {
            cacheService.forceRefreshTopNewsCache();
            log.info("Successfully executed cache warm-up -> Trigger: ApplicationReadyEvent | Status: Completed");
        } catch (Exception e) {
            log.error("Failed to execute cache warm-up -> Trigger: ApplicationReadyEvent | Critical error", e);
        }
    }
    
    @Scheduled(cron = "0 0 * * * *")
    public void hourlyCacheWarmUp() {
        log.info("Executing cache warm-up -> Trigger: Hourly Scheduled Task");
        try {
            cacheService.forceRefreshTopNewsCache();
            log.info("Successfully executed cache warm-up -> Trigger: Hourly Scheduled Task | Status: Completed");
        } catch (Exception e) {
            log.error("Failed to execute cache warm-up -> Trigger: Hourly Scheduled Task | Critical error", e);
        }
    }
}