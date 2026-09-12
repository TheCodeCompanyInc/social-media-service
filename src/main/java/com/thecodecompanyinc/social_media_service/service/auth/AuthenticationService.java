package com.thecodecompanyinc.social_media_service.service.auth;

import com.thecodecompanyinc.social_media_service.dto.auth.*;
import com.thecodecompanyinc.social_media_service.entity.User;
import com.thecodecompanyinc.social_media_service.response.LoginResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface AuthenticationService {

    LoginResponse login(LoginUserDto loginUserDto);

    LoginResponse googleLogin(GoogleIdTokenDto request)
            throws GeneralSecurityException, IOException;

    User signup(RegisterUserDto registerUserDto, String role);

    LoginResponse refreshToken(String authHeader);

    boolean verifyUser(VerifyCodeDto verifyCodeDto);

    void regenerateOtp(String email);

    boolean resetPassword(ResetPasswordDto resetPasswordDto);

    User getMe(String authHeader);

}
