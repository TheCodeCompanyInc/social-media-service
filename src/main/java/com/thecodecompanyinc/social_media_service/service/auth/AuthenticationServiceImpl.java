package com.thecodecompanyinc.social_media_service.service.auth;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thecodecompanyinc.social_media_service.dto.auth.LoginUserDto;
import com.thecodecompanyinc.social_media_service.dto.auth.RegisterUserDto;
import com.thecodecompanyinc.social_media_service.dto.auth.ResetPasswordDto;
import com.thecodecompanyinc.social_media_service.dto.auth.VerifyCodeDto;
import com.thecodecompanyinc.social_media_service.entity.Role;
import com.thecodecompanyinc.social_media_service.entity.User;
import com.thecodecompanyinc.social_media_service.exception.ResourceNotFoundException;
import com.thecodecompanyinc.social_media_service.mapper.LoginResponseMapper;
import com.thecodecompanyinc.social_media_service.mapper.UserMapper;
import com.thecodecompanyinc.social_media_service.response.LoginResponse;
import com.thecodecompanyinc.social_media_service.service.jwt.JwtService;
import com.thecodecompanyinc.social_media_service.service.mail.MailSenderService;
import com.thecodecompanyinc.social_media_service.service.user.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserService userService;
    private final JwtService jwtService;
    private final MailSenderService mailSenderService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final LoginResponseMapper loginResponseMapper;
    private static final long ONE_DAY_MS = 86_400_000L;

    @Override
    public LoginResponse login(LoginUserDto loginUserDto) {
        log.info("Login attempt for email: {}", loginUserDto.getEmail());
        User authenticatedUser = authenticate(loginUserDto);
        userService.updateFcmToken(loginUserDto.getEmail(), loginUserDto.getFcmToken());

        String accessToken = jwtService.generateAccessToken(authenticatedUser);
        String refreshToken = jwtService.generateRefreshToken(authenticatedUser);

        log.info("Login successful for email: {}", loginUserDto.getEmail());
        return loginResponseMapper.toLoginResponse(accessToken, refreshToken, authenticatedUser, jwtService);
    }

    private User authenticate(LoginUserDto loginUserDto) {
        log.debug("Authenticating user with email: {}", loginUserDto.getEmail());
        // 1. calls the UserDetailsService.loadUserByUsername() to fetch the user from
        // the database by email.
        // 2. uses the BCryptPasswordEncoder to compare the raw password from the
        // request against the stored hash.
        // 3. It automatically checks isAccountNonExpired(), isAccountNonLocked(),
        // isCredentialsNonExpired(), and isEnabled()
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginUserDto.getEmail(), loginUserDto.getPassword()));

        log.debug("Authentication successful for email: {}", loginUserDto.getEmail());
        return (User) auth.getPrincipal();
    }

    @Override
    @Transactional
    public User signup(RegisterUserDto registerUserDto, String role) {
        log.info("Signup attempt for email: {} with role: {}", registerUserDto.getEmail(), role);
        User user = userMapper.toUser(registerUserDto, passwordEncoder);
        if (role.equals("ADMIN")) {
            user.setRole(Role.ADMIN);
            log.debug("Setting role to ADMIN for email: {}", registerUserDto.getEmail());
        }

        String code = Integer.toString(user.getCode());
        User createdUser = userService.saveUser(user);
        log.debug("User saved, sending verification email to: {}", user.getEmail());
        mailSenderService.sendEmail(user.getEmail(), "Verification Code", code);

        log.info("Signup successful for email: {}", registerUserDto.getEmail());
        return createdUser;
    }

    private int generateRandomOtp() {
        log.debug("Generating random OTP");
        int otpLength = 6;

        int min = (int) Math.pow(10, otpLength - 1);
        int max = (int) Math.pow(10, otpLength) - 1;

        Random random = new Random();
        // return random.nextInt(max - min + 1) + min;
        // for testing environment, OTP is always 111111
        log.debug("OTP generated (test mode)");
        return 111111;
    }

    @Override
    public LoginResponse refreshToken(String authHeader) {
        log.info("Refresh token request received");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            final String refreshToken = authHeader.substring(7);
            if (jwtService.validateToken(refreshToken)) {
                log.debug("Refresh token is valid, generating new tokens");
                return handleValidToken(refreshToken);
            }
            log.warn("Refresh token validation failed");
        }

        log.warn("Invalid or missing refresh token");
        throw new IllegalArgumentException("Invalid or missing refresh token");
    }

    private LoginResponse handleValidToken(String refreshToken) {
        final String email = jwtService.getEmailFromToken(refreshToken);
        log.debug("Extracting email from refresh token: {}", email);
        User user = userService
                .getUser(email)
                .orElseThrow(
                        () -> {
                            log.error("User not found for email: {}", email);
                            return new ResourceNotFoundException("User not found");
                        });

        String accessToken = jwtService.generateAccessToken(user);
        log.debug("New access token generated for email: {}", email);

        return loginResponseMapper.toLoginResponse(accessToken, refreshToken, user, jwtService);
    }

    @Override
    @Transactional
    public boolean verifyUser(VerifyCodeDto verifyCodeDto) {
        String email = verifyCodeDto.getEmail();
        int code = verifyCodeDto.getCode();
        log.info("Verifying user with email: {}", email);

        User user = userService
                .getUser(email)
                .orElseThrow(
                        () -> {
                            log.error("User not found for email: {}", email);
                            return new ResourceNotFoundException("User not found");
                        });
        int verificationCode = user.getCode();

        if (code == verificationCode && isCodeValid(user.getCodeExpiredAt())) {
            user.setEmailVerified(true);
            log.info("User verified successfully for email: {}", email);
            return true;
        }

        log.warn("Verification failed for email: {} - invalid code or expired", email);
        return false;
    }

    @Override
    @Transactional
    public void regenerateOtp(String email) {
        log.info("Regenerating OTP for email: {}", email);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ONE_DAY_MS);
        User user = userService
                .getUser(email)
                .orElseThrow(
                        () -> {
                            log.error("User not found for email: {}", email);
                            return new ResourceNotFoundException("User not found");
                        });
        int newOtp = generateRandomOtp();

        user.setCode(newOtp);
        user.setCodeExpiredAt(new Timestamp(expiryDate.getTime()));
        user.setEmailVerified(false);

        String code = Integer.toString(user.getCode());
        mailSenderService.sendEmail(user.getEmail(), "Verification Code", code);
        log.info("OTP regenerated and sent to email: {}", email);
    }

    @Override
    @Transactional
    public boolean resetPassword(ResetPasswordDto resetPasswordDto) {
        String email = resetPasswordDto.getEmail();
        String newPassword = resetPasswordDto.getPassword();
        log.info("Reset password attempt for email: {}", email);

        int code = resetPasswordDto.getCode();
        User user = userService
                .getUser(email)
                .orElseThrow(
                        () -> {
                            log.error("User not found for email: {}", email);
                            return new ResourceNotFoundException("User not found");
                        });
        int verificationCode = user.getCode();

        if (code == verificationCode && isCodeValid(user.getCodeExpiredAt())) {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            user.setEmailVerified(true);
            log.info("Password reset successful for email: {}", email);
            return true;
        }

        log.warn("Password reset failed for email: {} - invalid code or expired", email);
        return false;
    }

    @Override
    public User getMe(String authHeader) {
        log.info("Getting user details from token");
        return userService.getUserByJwt(authHeader);
    }

    private boolean isCodeValid(Timestamp codeExpiredAt) {
        log.debug("Checking if code is valid, expiration: {}", codeExpiredAt);
        try {
            Date codeExpirationDate = new Date(codeExpiredAt.getTime());
            Date currentDate = new Date();
            boolean isValid = !currentDate.after(codeExpirationDate);
            log.debug("Code validity check result: {}", isValid);
            return isValid;
        } catch (Exception e) {
            // Default to code expired if parsing fails
            log.error("Error parsing code expiration date: {}", e.getMessage());
            return false;
        }
    }

}
