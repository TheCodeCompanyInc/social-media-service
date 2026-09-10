package com.thecodecompanyinc.social_media_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.thecodecompanyinc.social_media_service.entity.Role;
import com.thecodecompanyinc.social_media_service.entity.User;
import com.thecodecompanyinc.social_media_service.service.jwt.JwtServiceImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;

/** NOTE: ALL THOSE TESTS ARE AI-GENERATED AND REVIEWED MANUALLY */
@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtServiceImpl jwtService;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtServiceImpl();

        // Set fields via reflection since @Value won't work in unit tests
        setField(jwtService, "secretKey", "a-very-secret-key-that-is-at-least-32-bytes-long!!");
        setField(jwtService, "accessTokenExpiration", 3600000L); // 1 hour
        setField(jwtService, "refreshTokenExpiration", 86400000L); // 1 day

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPhoneNumber("1234567890");
        testUser.setRole(Role.CLIENT);
        testUser.setEmailVerified(true);

        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(Role.ADMIN);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    @DisplayName("Should generate access token")
    void shouldGenerateAccessToken() {
        // Act
        String token = jwtService.generateAccessToken(testUser);

        // Assert
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.getEmailFromToken(token)).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should generate refresh token")
    void shouldGenerateRefreshToken() {
        // Act
        String token = jwtService.generateRefreshToken(testUser);

        // Assert
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.getEmailFromToken(token)).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should extract email from token")
    void shouldExtractEmailFromToken() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);

        // Act
        String email = jwtService.getEmailFromToken(token);

        // Assert
        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should validate a valid token")
    void shouldValidateValidToken() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);

        // Act
        boolean isValid = jwtService.validateToken(token);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should return false for invalid token")
    void shouldReturnFalseForInvalidToken() {
        // Act
        boolean isValid = jwtService.validateToken("invalid.token.string");

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should throw ExpiredJwtException for expired token")
    void shouldThrowForExpiredToken() throws Exception {
        // Arrange - Set very short expiration
        setField(jwtService, "accessTokenExpiration", -1L);
        // Reset signing key cache
        setField(jwtService, "signingKey", null);

        String token = jwtService.generateAccessToken(testUser);

        // Act & Assert
        assertThatThrownBy(() -> jwtService.validateToken(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("Should extract expiration date from token")
    void shouldExtractExpirationDate() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);

        // Act
        Date expiration = jwtService.extractExpiration(token);

        // Assert
        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    @DisplayName("Should extract all claims from token")
    void shouldExtractAllClaims() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);

        // Act
        Claims claims = jwtService.extractAllClaims(token);

        // Assert
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo("test@example.com");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
        assertThat(claims.getId()).isNotNull();
        assertThat(claims.get("roles")).isNotNull();
    }

    @Test
    @DisplayName("Access token should expire before refresh token")
    void accessTokenShouldExpireBeforeRefreshToken() {
        // Arrange
        String accessToken = jwtService.generateAccessToken(testUser);
        String refreshToken = jwtService.generateRefreshToken(testUser);

        // Act
        Date accessExpiration = jwtService.extractExpiration(accessToken);
        Date refreshExpiration = jwtService.extractExpiration(refreshToken);

        // Assert
        assertThat(accessExpiration).isBefore(refreshExpiration);
    }
}
