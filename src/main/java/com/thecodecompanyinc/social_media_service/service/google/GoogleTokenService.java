package com.thecodecompanyinc.social_media_service.service.google;

import com.thecodecompanyinc.social_media_service.dto.auth.GoogleLoginDto;
import java.io.IOException;
import java.security.GeneralSecurityException;

public interface GoogleTokenService {
  GoogleLoginDto verifyIdToken(String idTokenString) throws GeneralSecurityException, IOException;
}
