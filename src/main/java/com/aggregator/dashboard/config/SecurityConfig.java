```java
package com.aggregator.dashboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Configures the security settings for the application, including authentication,
 * authorization, CORS, and CSRF protection.
 * <p>
 * This configuration sets up a basic in-memory user for demonstration and development
 * purposes, simulating a logged-in user state without a full OAuth2 implementation.
 * It secures the API endpoints while allowing access to the frontend static assets.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Defines the password encoder bean that will be used for hashing passwords.
     * BCrypt is a strong, widely-used hashing algorithm.
     *
     * @return A PasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures an in-memory user store for demonstration purposes.
     * This simulates a user database and is useful for development and testing.
     * In a production environment, this would be replaced with a real UserDetailsService
     * implementation (e.g., using JDBC, LDAP, or an OAuth2 provider).
     *
     * @param passwordEncoder The password encoder to use for the user's password.
     * @return A UserDetailsService with a pre-configured user.
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.builder()
                .username("user")
                // In a real application, this password should be externalized and not hardcoded.
                .password(passwordEncoder.encode("password123"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }

    /**
     * Configures the main security filter chain for the application.
     * This method defines which endpoints are public and which are protected.
     *
     * @param http The HttpSecurity object to configure.
     * @return The configured SecurityFilterChain.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Apply CORS configuration from the corsConfigurationSource bean
                .cors(withDefaults())
                // Configure CSRF protection. We use CookieCsrfTokenRepository to make the token
                // easily accessible to the frontend JavaScript application (e.g., React, Vue).
                // withHttpOnlyFalse() is necessary for the frontend to read the cookie.
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                // Define authorization rules for HTTP requests
                .authorizeHttpRequests(authz -> authz
                        // Allow unauthenticated access to static resources and the main page
                        .requestMatchers("/", "/index.html", "/static/**", "/*.js", "/*.css", "/favicon.ico", "/manifest.json").permitAll()
                        // All API endpoints under /api/ require authentication
                        .requestMatchers("/api/**").authenticated()
                        // Any other request must be authenticated as a security best practice
                        .anyRequest().authenticated()
                )
                // Enable form-based login with default settings. The frontend will POST to /login.
                .formLogin(withDefaults())
                // Enable logout with default settings. The frontend can access /logout.
                .logout(withDefaults());

        return http.build();
    }

    /**
     * Configures Cross-Origin Resource Sharing (CORS) for the application.
     * This is essential for allowing the frontend (running on a different origin, e.g., localhost:3000)
     * to communicate with the backend API (e.g., localhost:8080).
     *
     * @return A CorsConfigurationSource instance.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // In a real production environment, restrict this to the actual frontend domain.
        // Using a specific list is more secure than allowing "*".
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://127.0.0.1:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-XSRF-TOKEN"));
        // This is crucial for session-based authentication (cookies) to work across origins.
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this CORS configuration to all paths.
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```