```java
package com.aggregator.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Social Media Aggregator Dashboard application.
 * <p>
 * This class initializes the Spring Boot application context and enables
 * key features such as scheduling for background jobs (e.g., content refreshing)
 * and caching for performance optimization of frequently accessed data.
 * </p>
 */
@SpringBootApplication
@EnableScheduling // Enables Spring's scheduled task execution capabilities for background jobs.
@EnableCaching    // Enables Spring's caching abstraction to improve performance.
public class DashboardApplication {

    /**
     * The main method which serves as the entry point for the Java application.
     * It bootstraps and launches the Spring application from this main method.
     *
     * @param args command line arguments passed to the application. Not used in this application.
     */
    public static void main(String[] args) {
        SpringApplication.run(DashboardApplication.class, args);
    }

}
```