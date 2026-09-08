package com.thecodecompanyinc.social_media_service.service.auth;

import com.thecodecompanyinc.social_media_service.dto.LoginUserDto;
import com.thecodecompanyinc.social_media_service.dto.RegisterUserDto;
import com.thecodecompanyinc.social_media_service.dto.ResetPasswordDto;
import com.thecodecompanyinc.social_media_service.dto.VerifyCodeDto;
import com.thecodecompanyinc.social_media_service.entity.User;
import com.thecodecompanyinc.social_media_service.response.LoginResponse;

public interface AuthenticationService {

    LoginResponse login(LoginUserDto loginUserDto);

    User signup(RegisterUserDto registerUserDto, String role);

    LoginResponse refreshToken(String authHeader);

    boolean verifyUser(VerifyCodeDto verifyCodeDto);

    void regenerateOtp(String email);

    boolean resetPassword(ResetPasswordDto resetPasswordDto);

    User getMe(String authHeader);

}
