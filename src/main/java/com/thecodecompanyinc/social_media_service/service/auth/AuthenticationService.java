package com.thecodecompanyinc.social_media_service.service.auth;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.thecodecompanyinc.social_media_service.dto.auth.GoogleIdTokenDto;
import com.thecodecompanyinc.social_media_service.dto.auth.LoginUserDto;
import com.thecodecompanyinc.social_media_service.dto.auth.RegisterUserDto;
import com.thecodecompanyinc.social_media_service.dto.auth.ResetPasswordDto;
import com.thecodecompanyinc.social_media_service.dto.auth.VerifyCodeDto;
import com.thecodecompanyinc.social_media_service.entity.User;
import com.thecodecompanyinc.social_media_service.response.LoginResponse;

public interface AuthenticationService {

    LoginResponse login(LoginUserDto loginUserDto);

    LoginResponse googleLogin(GoogleIdTokenDto request)
            throws GeneralSecurityException, IOException;

    User signup(RegisterUserDto registerUserDto, String role);

    LoginResponse refreshToken(String authHeader);

    boolean verifyUser(VerifyCodeDto verifyCodeDto);

    void regenerateOtp(String email);

    boolean resetPassword(ResetPasswordDto resetPasswordDto);

}
