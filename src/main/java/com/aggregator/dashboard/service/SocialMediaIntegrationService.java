```java
package com.aggregator.dashboard.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Service responsible for integrating with various social media platforms.
 * This includes fetching content, posting new content, and retrieving analytics.
 * <p>
 * NOTE: This implementation simulates API calls to social media platforms.
 * In a real-world scenario, you would use official SDKs or detailed REST API clients
 * with robust OAuth2 handling to manage user-specific access tokens.
 */
@Service
@Slf4j
public class SocialMediaIntegrationService {

    private final WebClient webClient;

    // In a real app, these would come from a secure vault and be platform-specific.
    @Value("${social.api.twitter.url:https://api.twitter.com/2}")
    private String twitterApiUrl;

    @Value("${social.api.facebook.url:https://graph.facebook.com/v18.0}")
    private String facebookApiUrl;

    @Value("${social.api.linkedin.url:https://api.linkedin.com/v2}")
    private String linkedinApiUrl;

    /**
     * Constructor for dependency injection.
     *
     * @param webClientBuilder Builder for creating WebClient instances.
     */
    public SocialMediaIntegrationService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Aggregates a feed of social media posts from all supported platforms for a given user.
     * The results are cached to reduce API calls and improve performance.
     *
     * @param userId The ID of the user for whom to fetch the feed. In a real app, this would be used
     *               to retrieve OAuth2 tokens for the user's connected accounts.
     * @return A sorted list of {@link SocialMediaPost} objects, newest first.
     */
    @Cacheable(value = "feeds", key = "#userId")
    public List<SocialMediaPost> getAggregatedFeed(String userId) {
        log.info("Fetching aggregated feed for user: {}", userId);

        // In a real application, you would fetch the user's connected accounts
        // and their corresponding OAuth2 tokens from a secure store.
        String mockAccessToken = "mock-token-for-" + userId;

        // Asynchronously fetch posts from all platforms to maximize performance.
        CompletableFuture<List<SocialMediaPost>> twitterFuture = fetchTwitterPosts(mockAccessToken);
        CompletableFuture<List<SocialMediaPost>> facebookFuture = fetchFacebookPosts(mockAccessToken);
        CompletableFuture<List<SocialMediaPost>> linkedinFuture = fetchLinkedInPosts(mockAccessToken);

        // Combine the results when all asynchronous operations complete.
        List<SocialMediaPost> aggregatedPosts = CompletableFuture.allOf(twitterFuture, facebookFuture, linkedinFuture)
                .thenApply(v -> {
                    List<SocialMediaPost> twitterPosts = twitterFuture.join();
                    List<SocialMediaPost> facebookPosts = facebookFuture.join();
                    List<SocialMediaPost> linkedinPosts = linkedinFuture.join();

                    return Arrays.asList(twitterPosts, facebookPosts, linkedinPosts)
                            .stream()
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
                }).join();

        // Sort the combined list by timestamp, newest first.
        aggregatedPosts.sort(Comparator.comparing(SocialMediaPost::getTimestamp).reversed());

        log.info("Successfully aggregated {} posts for user {}", aggregatedPosts.size(), userId);
        return aggregatedPosts;
    }

    /**
     * Posts content to a selection of social media platforms.
     * This operation will evict the user's feed cache to ensure fresh data is loaded on the next fetch.
     *
     * @param userId    The ID of the user posting the content.
     * @param content   The text content to be posted.
     * @param platforms The list of platforms to post to.
     * @return A map indicating the status of the post for each platform.
     */
    @CacheEvict(value = "feeds", key = "#userId")
    public Map<Platform, PostStatus> postToPlatforms(String userId, String content, List<Platform> platforms) {
        log.info("Posting content for user {} to platforms: {}", userId, platforms);
        String mockAccessToken = "mock-token-for-" + userId;

        List<CompletableFuture<Map.Entry<Platform, PostStatus>>> postFutures = platforms.stream()
                .map(platform -> postToPlatform(platform, mockAccessToken, content)
                        .thenApply(status -> Map.entry(platform, status)))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(postFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> postFutures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .join();
    }

    /**
     * Fetches analytics for a specific post from a specific platform.
     * Results are cached to avoid redundant API calls for popular or frequently viewed posts.
     *
     * @param userId    The user ID.
     * @param postId    The ID of the post.
     * @param platform  The platform the post belongs to.
     * @return An {@link AnalyticsData} object with engagement metrics.
     */
    @Cacheable(value = "analytics", key = "{#platform, #postId}")
    public AnalyticsData getPostAnalytics(String userId, String postId, Platform platform) {
        log.info("Fetching analytics for post {} on platform {} for user {}", postId, platform, userId);
        // SIMULATION: In a real app, this would call the specific platform's API
        // to get engagement metrics for the given postId.
        return new AnalyticsData(
                (long) (Math.random() * 1000), // likes
                (long) (Math.random() * 100),  // comments
                (long) (Math.random() * 50)    // shares
        );
    }

    // --- Private Helper Methods for Platform-Specific Integrations ---

    private CompletableFuture<List<SocialMediaPost>> fetchTwitterPosts(String accessToken) {
        // SIMULATION: In a real app, this would call the Twitter API's user timeline endpoint.
        log.debug("Simulating fetch from Twitter API.");
        return CompletableFuture.supplyAsync(() -> List.of(
                new SocialMediaPost("tw123", Platform.TWITTER, "TwitterDev", "Hello, developers! Check out the new API v2 features.", LocalDateTime.now().minusHours(1), 1500, 200, 300),
                new SocialMediaPost("tw124", Platform.TWITTER, "SpringFramework", "Spring Boot 3.2 is here with exciting new features!", LocalDateTime.now().minusHours(5), 850, 95, 150)
        )).exceptionally(ex -> {
            log.error("Failed to fetch posts from Twitter", ex);
            return List.of(); // Return empty list on failure to not break the entire aggregation.
        });
    }

    private CompletableFuture<List<SocialMediaPost>> fetchFacebookPosts(String accessToken) {
        // SIMULATION: In a real app, this would call the Facebook Graph API's /me/feed endpoint.
        log.debug("Simulating fetch from Facebook API.");
        return CompletableFuture.supplyAsync(() -> List.of(
                new SocialMediaPost("fb456", Platform.FACEBOOK, "John Doe", "Enjoying a beautiful day at the park!", LocalDateTime.now().minusHours(2), 120, 15, 5),
                new SocialMediaPost("fb457", Platform.FACEBOOK, "Jane Smith", "My new blog post on reactive programming is live!", LocalDateTime.now().minusDays(1), 250, 45, 20)
        )).exceptionally(ex -> {
            log.error("Failed to fetch posts from Facebook", ex);
            return List.of();
        });
    }

    private CompletableFuture<List<SocialMediaPost>> fetchLinkedInPosts(String accessToken) {
        // SIMULATION: In a real app, this would call the LinkedIn API's ugcPosts endpoint.
        log.debug("Simulating fetch from LinkedIn API.");
        return CompletableFuture.supplyAsync(() -> List.of(
                new SocialMediaPost("li789", Platform.LINKEDIN, "TechCorp Inc.", "We are hiring senior Java developers! #Java #SpringBoot", LocalDateTime.now().minusMinutes(30), 300, 50, 25)
        )).exceptionally(ex -> {
            log.error("Failed to fetch posts from LinkedIn", ex);
            return List.of();
        });
    }

    private CompletableFuture<PostStatus> postToPlatform(Platform platform, String accessToken, String content) {
        log.debug("Posting to {} with content: '{}'", platform, content);
        // SIMULATION: In a real app, this would make a POST request to the respective platform's API.
        // The logic would differ significantly for each platform (e.g., different endpoints, request bodies).
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate network delay
                Thread.sleep(200 + (long) (Math.random() * 300));
                if (content == null || content.isBlank()) {
                    return new PostStatus("FAILED", "Content cannot be empty.");
                }
                // Simulate a potential API failure based on content
                if (content.toLowerCase().contains("error")) {
                    return new PostStatus("FAILED", "Simulated API error due to content.");
                }
                return new PostStatus("SUCCESS", "post-id-" + platform.name().toLowerCase() + "-" + System.currentTimeMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new PlatformIntegrationException("Posting to " + platform + " was interrupted.", e);
            }
        }).exceptionally(ex -> {
            log.error("Failed to post to platform {}", platform, ex);
            return new PostStatus("FAILED", ex.getMessage());
        });
    }


    // --- Inner DTOs, Enums, and Exceptions for this Service ---

    /**
     * Enum representing the supported social media platforms.
     */
    public enum Platform {
        TWITTER,
        FACEBOOK,
        LINKEDIN,
        INSTAGRAM // Added for future extension
    }

    /**
     * A standardized representation of a social media post from any platform.
     */
    @Data
    @AllArgsConstructor
    public static class SocialMediaPost {
        private String id;
        private Platform platform;
        private String author;
        private String content;
        private LocalDateTime timestamp;
        private long likes;
        private long comments;
        private long shares;
    }

    /**
     * Represents engagement analytics for a post.
     */
    @Data
    @AllArgsConstructor
    public static class AnalyticsData {
        private long likes;
        private long comments;
        private long shares;
    }

    /**
     * Represents the status of a cross-posting operation.
     */
    @Data
    @AllArgsConstructor
    public static class PostStatus {
        private String status; // e.g., "SUCCESS", "FAILED"
        private String message; // e.g., post ID on success, error message on failure
    }

    /**
     * Custom exception for platform integration errors.
     */
    public static class PlatformIntegrationException extends RuntimeException {
        public PlatformIntegrationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
```