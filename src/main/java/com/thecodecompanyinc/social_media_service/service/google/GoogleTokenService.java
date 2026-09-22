package com.thecodecompanyinc.social_media_service.service.google;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.thecodecompanyinc.social_media_service.dto.auth.GoogleLoginDto;

public interface GoogleTokenService {
    GoogleLoginDto verifyIdToken(String idTokenString) throws GeneralSecurityException, IOException;
}
