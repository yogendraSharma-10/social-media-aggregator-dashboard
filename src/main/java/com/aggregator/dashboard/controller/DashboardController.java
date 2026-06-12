```java
package com.aggregator.dashboard.controller;

import com.aggregator.dashboard.exception.PlatformIntegrationException;
import com.aggregator.dashboard.model.dto.AggregatedContent;
import com.aggregator.dashboard.model.dto.AnalyticsSummary;
import com.aggregator.dashboard.model.dto.ConnectedAccount;
import com.aggregator.dashboard.model.dto.PostRequest;
import com.aggregator.dashboard.service.SocialMediaIntegrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * REST controller for handling dashboard-related API requests.
 * This controller provides endpoints for fetching aggregated content,
 * cross-posting to social media platforms, managing accounts, and retrieving analytics.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private final SocialMediaIntegrationService socialMediaIntegrationService;

    @Autowired
    public DashboardController(SocialMediaIntegrationService socialMediaIntegrationService) {
        this.socialMediaIntegrationService = socialMediaIntegrationService;
    }

    /**
     * Fetches the aggregated content feed from all connected social media platforms for the current user.
     *
     * @param principal The authenticated user principal, automatically provided by Spring Security.
     * @return A ResponseEntity containing a list of aggregated content items.
     */
    @GetMapping("/content")
    public ResponseEntity<List<AggregatedContent>> getAggregatedContent(Principal principal) {
        log.info("Fetching aggregated content for user: {}", principal.getName());
        try {
            List<AggregatedContent> content = socialMediaIntegrationService.getAggregatedFeed(principal.getName());
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            log.error("Error fetching aggregated content for user: {}", principal.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Posts content to one or more specified social media platforms.
     *
     * @param postRequest The request body containing the content and target platforms.
     * @param principal   The authenticated user principal.
     * @return A ResponseEntity containing a map of platform names to post statuses (e.g., "SUCCESS", "FAILURE").
     */
    @PostMapping("/post")
    public ResponseEntity<Map<String, String>> crossPostContent(@RequestBody PostRequest postRequest, Principal principal) {
        if (postRequest == null || postRequest.getContent() == null || postRequest.getContent().isBlank() || postRequest.getPlatforms() == null || postRequest.getPlatforms().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid post request. Content and platforms must be provided."));
        }
        log.info("Processing cross-post request for user: {} to platforms: {}", principal.getName(), postRequest.getPlatforms());
        try {
            Map<String, String> postResults = socialMediaIntegrationService.postToPlatforms(principal.getName(), postRequest.getContent(), postRequest.getPlatforms());
            return ResponseEntity.ok(postResults);
        } catch (PlatformIntegrationException e) {
            log.error("Platform integration error during cross-post for user: {}", principal.getName(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during cross-post for user: {}", principal.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred."));
        }
    }

    /**
     * Retrieves a summary of engagement analytics for the current user.
     *
     * @param principal The authenticated user principal.
     * @return A ResponseEntity containing the analytics summary.
     */
    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsSummary> getAnalyticsSummary(Principal principal) {
        log.info("Fetching analytics summary for user: {}", principal.getName());
        try {
            AnalyticsSummary summary = socialMediaIntegrationService.getAnalytics(principal.getName());
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error fetching analytics for user: {}", principal.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a list of social media accounts connected by the current user.
     *
     * @param principal The authenticated user principal.
     * @return A ResponseEntity containing a list of connected accounts.
     */
    @GetMapping("/accounts")
    public ResponseEntity<List<ConnectedAccount>> getConnectedAccounts(Principal principal) {
        log.info("Fetching connected accounts for user: {}", principal.getName());
        List<ConnectedAccount> accounts = socialMediaIntegrationService.getConnectedAccounts(principal.getName());
        return ResponseEntity.ok(accounts);
    }

    /**
     * Removes a connected social media account for the current user.
     *
     * @param platform  The name of the platform to disconnect (e.g., "twitter", "facebook").
     * @param principal The authenticated user principal.
     * @return A ResponseEntity indicating the success of the operation.
     */
    @DeleteMapping("/accounts/{platform}")
    public ResponseEntity<Void> removeAccount(@PathVariable String platform, Principal principal) {
        log.info("Request to remove account for platform: {} from user: {}", platform, principal.getName());
        try {
            socialMediaIntegrationService.removeAccount(principal.getName(), platform);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Attempted to remove non-existent or invalid platform '{}' for user {}", platform, principal.getName());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error removing account for platform {} for user {}", platform, principal.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Global exception handler for this controller.
     * Catches unhandled exceptions and returns a generic 500 Internal Server Error response.
     * This prevents leaking stack traces to the client.
     *
     * @param ex The exception that was thrown.
     * @return A ResponseEntity with a generic error message.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        log.error("An unexpected error occurred in DashboardController", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected server error occurred. Please try again later."));
    }
}
```