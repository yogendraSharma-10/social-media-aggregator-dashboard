```java
package com.aggregator.dashboard.scheduler;

import com.aggregator.dashboard.service.SocialMediaIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Scheduled job responsible for periodically refreshing social media content.
 * This job triggers the SocialMediaIntegrationService to fetch the latest posts
 * from all connected social media accounts and update the application's cache.
 *
 * It includes a locking mechanism to prevent concurrent executions if a job
 * takes longer than its scheduled interval to complete.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ContentRefreshJob {

    private final SocialMediaIntegrationService socialMediaIntegrationService;

    // AtomicBoolean to prevent concurrent executions of the job.
    // This is a safeguard in case the refresh process takes longer than the fixed rate interval.
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    /**
     * Periodically fetches new content from all integrated social media platforms.
     * The schedule is configured via the 'app.scheduler.refresh-rate-ms' property
     * in application.properties, with a default of 15 minutes.
     *
     * This method is designed to be robust, logging any errors that occur during
     * the refresh process without stopping subsequent scheduled executions.
     */
    @Scheduled(fixedRateString = "${app.scheduler.refresh-rate-ms:900000}")
    public void refreshAllContent() {
        if (isRunning.compareAndSet(false, true)) {
            log.info("Starting scheduled content refresh job at {}", Instant.now());
            try {
                // The core logic is delegated to the service layer.
                // This service will handle fetching data for all users and all their connected accounts.
                socialMediaIntegrationService.refreshAllFeeds();
                log.info("Successfully completed scheduled content refresh job.");
            } catch (Exception e) {
                // Catching a broad exception to ensure the scheduler doesn't die.
                // Specific exceptions should be handled within the service layer.
                log.error("An unexpected error occurred during the content refresh job", e);
            } finally {
                // Ensure the lock is always released, even if an exception occurs.
                isRunning.set(false);
                log.debug("Content refresh job lock released.");
            }
        } else {
            log.warn("Skipping content refresh job execution as a previous job is still running.");
        }
    }
}
```