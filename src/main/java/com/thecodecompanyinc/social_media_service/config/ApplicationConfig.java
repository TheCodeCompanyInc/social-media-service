package com.thecodecompanyinc.social_media_service.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.thecodecompanyinc.social_media_service.exception.ResourceNotFoundException;
import com.thecodecompanyinc.social_media_service.service.user.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApplicationConfig {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String webClientId;

    @Value("${google.android.client-id}")
    private String androidClientId;

    @Value("${google.ios.client-id}")
    private String iosClientId;

    private final UserService userService;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        AuthenticationManager defaultManager = authenticationConfiguration.getAuthenticationManager();

        return new ProviderManager(List.of(authenticationProvider()), defaultManager);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> userService.getUser(email).orElseThrow(() -> new ResourceNotFoundException("User not found!"));
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    /**
     * Builds the GoogleIdTokenVerifier with all possible client IDs (Web, Android,
     * iOS) to support
     * tokens from different platforms.
     */
    @Bean
    public GoogleIdTokenVerifier buildVerifier() {
        log.debug("Building Google ID token verifier");

        // Build list of valid client IDs (web, android, ios)
        java.util.List<String> clientIds = new java.util.ArrayList<>();
        clientIds.add(webClientId);

        if (androidClientId != null && !androidClientId.isEmpty()) {
            clientIds.add(androidClientId);
            log.debug("Android client ID configured");
        }

        if (iosClientId != null && !iosClientId.isEmpty()) {
            clientIds.add(iosClientId);
            log.debug("iOS client ID configured");
        }

        log.debug("Verifier configured with {} client ID(s)", clientIds.size());

        return new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(clientIds)
                .build();
    }

}
