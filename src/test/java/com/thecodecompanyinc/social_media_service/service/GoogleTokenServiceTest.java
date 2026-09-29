package com.thecodecompanyinc.social_media_service.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.thecodecompanyinc.social_media_service.dto.auth.GoogleLoginDto;
import com.thecodecompanyinc.social_media_service.service.google.GoogleTokenServiceImpl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** NOTE: ALL THOSE TESTS ARE AI-GENERATED AND REVIEWED MANUALLY */
@ExtendWith(MockitoExtension.class)
@DisplayName("GoogleTokenService Unit Tests")
class GoogleTokenServiceTest {

    @Mock private GoogleIdTokenVerifier verifier;

    @Mock private GoogleIdToken googleIdToken;

    @Mock private GoogleIdToken.Payload payload;

    @InjectMocks private GoogleTokenServiceImpl googleTokenService;

    @Test
    @DisplayName("Should verify valid Google ID token successfully")
    void shouldVerifyValidToken() throws GeneralSecurityException, IOException {
        // Arrange
        String idTokenString = "valid-google-id-token";

        when(verifier.verify(idTokenString)).thenReturn(googleIdToken);
        when(googleIdToken.getPayload()).thenReturn(payload);
        when(payload.getEmail()).thenReturn("google@example.com");
        when(payload.getEmailVerified()).thenReturn(true);
        when(payload.get("name")).thenReturn("John Doe");
        when(payload.get("given_name")).thenReturn("John");
        when(payload.get("family_name")).thenReturn("Doe");

        // Act
        GoogleLoginDto result = googleTokenService.verifyIdToken(idTokenString);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("google@example.com");
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getGivenName()).isEqualTo("John");
        assertThat(result.getFamilyName()).isEqualTo("Doe");
    }

    @Test
    @DisplayName("Should throw exception for invalid Google ID token")
    void shouldThrowExceptionForInvalidToken() throws GeneralSecurityException, IOException {
        // Arrange
        String idTokenString = "invalid-token";
        when(verifier.verify(idTokenString)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> googleTokenService.verifyIdToken(idTokenString))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Google ID token");
    }

    @Test
    @DisplayName("Should throw exception when email is not verified by Google")
    void shouldThrowExceptionForUnverifiedEmail() throws GeneralSecurityException, IOException {
        // Arrange
        String idTokenString = "unverified-email-token";

        when(verifier.verify(idTokenString)).thenReturn(googleIdToken);
        when(googleIdToken.getPayload()).thenReturn(payload);
        when(payload.getEmail()).thenReturn("unverified@example.com");
        when(payload.getEmailVerified()).thenReturn(false);
        when(payload.get("name")).thenReturn("Test");
        when(payload.get("given_name")).thenReturn("Test");
        when(payload.get("family_name")).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> googleTokenService.verifyIdToken(idTokenString))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email is not verified by Google");
    }

    @Test
    @DisplayName("Should fall back to name when given_name is null")
    void shouldFallBackToNameWhenGivenNameIsNull() throws GeneralSecurityException, IOException {
        // Arrange
        String idTokenString = "valid-token";

        when(verifier.verify(idTokenString)).thenReturn(googleIdToken);
        when(googleIdToken.getPayload()).thenReturn(payload);
        when(payload.getEmail()).thenReturn("test@example.com");
        when(payload.getEmailVerified()).thenReturn(true);
        when(payload.get("name")).thenReturn("Full Name");
        when(payload.get("given_name")).thenReturn(null);
        when(payload.get("family_name")).thenReturn(null);

        // Act
        GoogleLoginDto result = googleTokenService.verifyIdToken(idTokenString);

        // Assert
        assertThat(result.getGivenName()).isEqualTo("Full");
    }

    @Test
    @DisplayName("Should fall back to email when both given_name and name are null")
    void shouldFallBackToEmailWhenBothNamesAreNull() throws GeneralSecurityException, IOException {
        // Arrange
        String idTokenString = "valid-token";

        when(verifier.verify(idTokenString)).thenReturn(googleIdToken);
        when(googleIdToken.getPayload()).thenReturn(payload);
        when(payload.getEmail()).thenReturn("john@example.com");
        when(payload.getEmailVerified()).thenReturn(true);
        when(payload.get("name")).thenReturn(null);
        when(payload.get("given_name")).thenReturn(null);
        when(payload.get("family_name")).thenReturn(null);

        // Act
        GoogleLoginDto result = googleTokenService.verifyIdToken(idTokenString);

        // Assert
        assertThat(result.getGivenName()).isEqualTo("john");
    }

    @Test
    @DisplayName("Should fall back to 'User' when all name sources are null")
    void shouldFallBackToUserWhenAllNamesAreNull() throws GeneralSecurityException, IOException {
        // Arrange
        String idTokenString = "valid-token";

        when(verifier.verify(idTokenString)).thenReturn(googleIdToken);
        when(googleIdToken.getPayload()).thenReturn(payload);
        when(payload.getEmail()).thenReturn(null);
        when(payload.getEmailVerified()).thenReturn(true);
        when(payload.get("name")).thenReturn(null);
        when(payload.get("given_name")).thenReturn(null);
        when(payload.get("family_name")).thenReturn(null);

        // Act
        GoogleLoginDto result = googleTokenService.verifyIdToken(idTokenString);

        // Assert
        assertThat(result.getGivenName()).isEqualTo("User");
    }

    @Test
    @DisplayName("Should use first part of full name when given_name is blank")
    void shouldUseFirstPartOfFullNameWhenGivenNameIsBlank()
            throws GeneralSecurityException, IOException {
        // Arrange
        String idTokenString = "valid-token";

        when(verifier.verify(idTokenString)).thenReturn(googleIdToken);
        when(googleIdToken.getPayload()).thenReturn(payload);
        when(payload.getEmail()).thenReturn("test@example.com");
        when(payload.getEmailVerified()).thenReturn(true);
        when(payload.get("name")).thenReturn("John Michael Doe");
        when(payload.get("given_name")).thenReturn("");
        when(payload.get("family_name")).thenReturn("Doe");

        // Act
        GoogleLoginDto result = googleTokenService.verifyIdToken(idTokenString);

        // Assert
        assertThat(result.getGivenName()).isEqualTo("John");
    }
}
