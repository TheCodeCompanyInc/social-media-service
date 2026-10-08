package com.thecodecompanyinc.social_media_service.service.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.thecodecompanyinc.social_media_service.dto.auth.GoogleLoginDto;
import io.swagger.v3.oas.annotations.Operation;
import java.io.IOException;
import java.security.GeneralSecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleTokenServiceImpl implements GoogleTokenService {

    private final GoogleIdTokenVerifier verifier;

    /**
     * Verifies the Google ID token received from Flutter Google Sign-In.
     *
     * @param idTokenString The ID token string received from the mobile app
     * @return GoogleLoginDto containing user information if token is valid
     * @throws IllegalArgumentException if the token is invalid or expired
     */
    @Operation
    public GoogleLoginDto verifyIdToken(String idTokenString)
            throws GeneralSecurityException, IOException {
        log.info("Verifying Google ID token");
        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken == null) {
            log.error("Invalid Google ID token");
            throw new IllegalArgumentException("Invalid Google ID token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();

        String email = payload.getEmail();
        boolean emailVerified = payload.getEmailVerified();
        String name = (String) payload.get("name");
        String givenName = (String) payload.get("given_name");
        String familyName = (String) payload.get("family_name");

        String resolvedGivenName = getName(givenName, name, email);

        log.info("Google ID token verified successfully for email: {}", email);
        log.debug(
                "Token payload - name: {}, givenName: {}, familyName: {}, emailVerified: {}",
                name,
                resolvedGivenName,
                familyName,
                emailVerified);

        if (!emailVerified) {
            log.warn("Email not verified for Google user: {}", email);
            throw new IllegalArgumentException("Email is not verified by Google");
        }

        return new GoogleLoginDto(email, name, resolvedGivenName, familyName);
    }

    private static @NonNull String getName(String givenName, String name, String email) {
        String resolvedGivenName = givenName;

        if (resolvedGivenName == null || resolvedGivenName.isBlank()) {
            if (name != null && !name.isBlank()) {
                String trimmedName = name.trim();
                int spaceIndex = trimmedName.indexOf(' ');
                resolvedGivenName =
                        (spaceIndex > 0) ? trimmedName.substring(0, spaceIndex) : trimmedName;
            } else if (email != null && !email.isBlank()) {
                String localPart = email;
                int atIndex = email.indexOf('@');
                if (atIndex > 0) {
                    localPart = email.substring(0, atIndex);
                }
                resolvedGivenName = localPart.isBlank() ? "User" : localPart;
            } else {
                resolvedGivenName = "User";
            }
        }
        return resolvedGivenName;
    }
}
