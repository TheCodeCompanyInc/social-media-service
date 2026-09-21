package com.thecodecompanyinc.social_media_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.thecodecompanyinc.social_media_service.dto.auth.GoogleIdTokenDto;
import com.thecodecompanyinc.social_media_service.dto.auth.GoogleLoginDto;
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
import com.thecodecompanyinc.social_media_service.service.auth.AuthenticationServiceImpl;
import com.thecodecompanyinc.social_media_service.service.google.GoogleTokenService;
import com.thecodecompanyinc.social_media_service.service.jwt.JwtService;
import com.thecodecompanyinc.social_media_service.service.mail.MailSenderService;
import com.thecodecompanyinc.social_media_service.service.user.UserService;

/** NOTE: ALL THOSE TESTS ARE AI-GENERATED AND REVIEWED MANUALLY */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Unit Tests")
class AuthenticationServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private MailSenderService mailSenderService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private GoogleTokenService googleTokenService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private LoginResponseMapper loginResponseMapper;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private User user;
    private LoginUserDto loginUserDto;
    private RegisterUserDto registerUserDto;
    private GoogleLoginDto googleLoginDto;

    @BeforeEach
    void setUp() {
        // Setup test user
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhoneNumber("1234567890");
        user.setRole(Role.CLIENT);
        user.setEmailVerified(true);
        user.setCode(111111);
        // 1 day from now
        user.setCodeExpiredAt(new Timestamp(System.currentTimeMillis() + 86400000));

        // Setup login DTO
        loginUserDto = new LoginUserDto();
        loginUserDto.setEmail("test@example.com");
        loginUserDto.setPassword("password123");
        loginUserDto.setFcmToken("fcm-token-123");

        // Setup register DTO
        registerUserDto = new RegisterUserDto();
        registerUserDto.setEmail("newuser@example.com");
        registerUserDto.setPassword("password123");
        registerUserDto.setFirstName("Jane");
        registerUserDto.setLastName("Smith");
        registerUserDto.setPhoneNumber("9876543210");

        // Setup Google login DTO
        googleLoginDto = new GoogleLoginDto();
        googleLoginDto.setEmail("google@example.com");
        googleLoginDto.setName("Google User");
        googleLoginDto.setGivenName("Google");
        googleLoginDto.setFamilyName("User");

        lenient().when(userMapper.toUser(any(RegisterUserDto.class), eq(passwordEncoder)))
                .thenAnswer(invocation -> {
                    RegisterUserDto dto = invocation.getArgument(0);
                    User mappedUser = new User();
                    mappedUser.setEmail(dto.getEmail());
                    mappedUser.setUsername(dto.getUsername());
                    mappedUser.setPhoneNumber(dto.getPhoneNumber());
                    mappedUser.setFirstName(dto.getFirstName());
                    mappedUser.setLastName(dto.getLastName());
                    mappedUser.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
                    mappedUser.setJoin_date(new Timestamp(System.currentTimeMillis()));
                    mappedUser.setCode(111111);
                    mappedUser.setCodeExpiredAt(
                            new Timestamp(System.currentTimeMillis() + 86400000));
                    mappedUser.setEmailVerified(false);
                    return mappedUser;
                });

        lenient().when(loginResponseMapper.toLoginResponse(
                anyString(), anyString(), any(User.class), eq(jwtService)))
                .thenAnswer(invocation -> {
                    LoginResponse response = new LoginResponse();
                    response.setAccessToken(invocation.getArgument(0));
                    response.setRefreshToken(invocation.getArgument(1));
                    response.setUser(invocation.getArgument(2));
                    return response;
                });
    }

    /** LOGIN TESTS */
    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfully() {
        // Arrange
        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        doNothing().when(userService).updateFcmToken(anyString(), anyString());

        // Act
        LoginResponse response = authenticationService.login(loginUserDto);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUser()).isEqualTo(user);

        // Verify interactions
        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService, times(1))
                .updateFcmToken(loginUserDto.getEmail(), loginUserDto.getFcmToken());
        verify(jwtService, times(1)).generateAccessToken(user);
        verify(jwtService, times(1)).generateRefreshToken(user);
    }

    @Test
    @DisplayName("Should throw exception when login fails due to wrong credentials")
    void shouldThrowExceptionWhenLoginFailsDueToWrongCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.login(loginUserDto))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Bad credentials");

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService, never()).updateFcmToken(anyString(), anyString());
    }

    /** SIGNUP TESTS */
    @Test
    @DisplayName("Should signup CLIENT user successfully")
    void shouldSignupClientUser() {
        // Arrange
        User newUser = new User();
        newUser.setEmail(registerUserDto.getEmail());

        when(passwordEncoder.encode(registerUserDto.getPassword())).thenReturn("encoded-password");
        when(userService.saveUser(any(User.class))).thenReturn(newUser);
        doNothing().when(mailSenderService).sendEmail(anyString(), anyString(), anyString());

        // Act
        User result = authenticationService.signup(registerUserDto, "CLIENT");

        // Assert
        assertThat(result).isNotNull();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).saveUser(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getEmail()).isEqualTo(registerUserDto.getEmail());
        assertThat(capturedUser.getFirstName()).isEqualTo(registerUserDto.getFirstName());
        assertThat(capturedUser.getLastName()).isEqualTo(registerUserDto.getLastName());
        assertThat(capturedUser.getPhoneNumber()).isEqualTo(registerUserDto.getPhoneNumber());
        assertThat(capturedUser.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(capturedUser.getRole()).isEqualTo(Role.CLIENT);
        assertThat(capturedUser.isEmailVerified()).isFalse();
        assertThat(capturedUser.getCode()).isEqualTo(111111); // Test OTP

        verify(mailSenderService, times(1))
                .sendEmail(eq(registerUserDto.getEmail()), eq("Verification Code"), eq("111111"));
    }

    @Test
    @DisplayName("Should signup ADMIN user successfully")
    void shouldSignupAdminUser() {
        // Arrange
        User newUser = new User();
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userService.saveUser(any(User.class))).thenReturn(newUser);
        doNothing().when(mailSenderService).sendEmail(anyString(), anyString(), anyString());

        // Act
        authenticationService.signup(registerUserDto, "ADMIN");

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).saveUser(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("Should generate 6-digit OTP on signup")
    void shouldGenerate6DigitOtpOnSignup() {
        // Arrange
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userService.saveUser(any(User.class))).thenReturn(new User());
        doNothing().when(mailSenderService).sendEmail(anyString(), anyString(), anyString());

        // Act
        authenticationService.signup(registerUserDto, "CLIENT");

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).saveUser(userCaptor.capture());

        int code = userCaptor.getValue().getCode();
        assertThat(code).isEqualTo(111111); // Test environment OTP
    }

    @Test
    @DisplayName("Should set code expiry to 1 day from now")
    void shouldSetCodeExpiryTo1DayFromNow() {
        // Arrange
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userService.saveUser(any(User.class))).thenReturn(new User());
        doNothing().when(mailSenderService).sendEmail(anyString(), anyString(), anyString());

        // Act
        authenticationService.signup(registerUserDto, "CLIENT");

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).saveUser(userCaptor.capture());

        Timestamp codeExpiry = userCaptor.getValue().getCodeExpiredAt();
        Timestamp tomorrow = new Timestamp(System.currentTimeMillis() + 86400000);

        // Allow 5 second difference for test execution time
        assertThat(codeExpiry.getTime())
                .isBetween(tomorrow.getTime() - 5000, tomorrow.getTime() + 5000);
    }

    /** VERIFY CODE TESTS */
    @Test
    @DisplayName("Should verify user successfully with valid code")
    void shouldVerifyUserWithValidCode() {
        // Arrange
        VerifyCodeDto verifyCodeDto = new VerifyCodeDto();
        verifyCodeDto.setEmail(user.getEmail());
        verifyCodeDto.setCode(111111);

        user.setEmailVerified(false);
        when(userService.getUser(user.getEmail())).thenReturn(Optional.of(user));

        // Act
        boolean result = authenticationService.verifyUser(verifyCodeDto);

        // Assert
        assertThat(result).isTrue();
        assertThat(user.isEmailVerified()).isTrue();
    }

    @Test
    @DisplayName("Should fail verification with wrong code")
    void shouldFailVerificationWithWrongCode() {
        // Arrange
        VerifyCodeDto verifyCodeDto = new VerifyCodeDto();
        verifyCodeDto.setEmail(user.getEmail());
        verifyCodeDto.setCode(999999); // Wrong code

        user.setEmailVerified(false);
        when(userService.getUser(user.getEmail())).thenReturn(Optional.of(user));

        // Act
        boolean result = authenticationService.verifyUser(verifyCodeDto);

        // Assert
        assertThat(result).isFalse();
        assertThat(user.isEmailVerified()).isFalse();
    }

    @Test
    @DisplayName("Should fail verification with expired code")
    void shouldFailVerificationWithExpiredCode() {
        // Arrange
        VerifyCodeDto verifyCodeDto = new VerifyCodeDto();
        verifyCodeDto.setEmail(user.getEmail());
        verifyCodeDto.setCode(111111);

        user.setCodeExpiredAt(new Timestamp(System.currentTimeMillis() - 1000)); // Expired
        user.setEmailVerified(false);
        when(userService.getUser(user.getEmail())).thenReturn(Optional.of(user));

        // Act
        boolean result = authenticationService.verifyUser(verifyCodeDto);

        // Assert
        assertThat(result).isFalse();
        assertThat(user.isEmailVerified()).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when verifying non-existent user")
    void shouldThrowExceptionWhenVerifyingNonExistentUser() {
        // Arrange
        VerifyCodeDto verifyCodeDto = new VerifyCodeDto();
        verifyCodeDto.setEmail("nonexistent@example.com");
        verifyCodeDto.setCode(111111);

        when(userService.getUser(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.verifyUser(verifyCodeDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    /** REGENERATE OTP TESTS */
    @Test
    @DisplayName("Should regenerate OTP successfully")
    void shouldRegenerateOtpSuccessfully() {
        // Arrange
        when(userService.getUser(user.getEmail())).thenReturn(Optional.of(user));
        doNothing().when(mailSenderService).sendEmail(anyString(), anyString(), anyString());

        // Act
        authenticationService.regenerateOtp(user.getEmail());

        // Assert
        assertThat(user.getCode()).isEqualTo(111111);
        assertThat(user.isEmailVerified()).isFalse();

        verify(mailSenderService, times(1))
                .sendEmail(eq(user.getEmail()), eq("Verification Code"), eq("111111"));
    }

    @Test
    @DisplayName("Should throw exception when regenerating OTP for non-existent user")
    void shouldThrowExceptionWhenRegeneratingOtpForNonExistentUser() {
        // Arrange
        when(userService.getUser(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.regenerateOtp("nonexistent@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(mailSenderService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    /** RESET PASSWORD TESTS */
    @Test
    @DisplayName("Should reset password successfully with valid code")
    void shouldResetPasswordWithValidCode() {
        // Arrange
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
        resetPasswordDto.setEmail(user.getEmail());
        resetPasswordDto.setPassword("newPassword123");
        resetPasswordDto.setCode(111111);

        when(userService.getUser(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encoded-new-password");

        // Act
        boolean result = authenticationService.resetPassword(resetPasswordDto);

        // Assert
        assertThat(result).isTrue();
        assertThat(user.getPasswordHash()).isEqualTo("encoded-new-password");
        assertThat(user.isEmailVerified()).isTrue();
    }

    @Test
    @DisplayName("Should fail password reset with wrong code")
    void shouldFailPasswordResetWithWrongCode() {
        // Arrange
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
        resetPasswordDto.setEmail(user.getEmail());
        resetPasswordDto.setPassword("newPassword123");
        resetPasswordDto.setCode(999999); // Wrong code

        String originalPasswordHash = user.getPasswordHash();
        when(userService.getUser(user.getEmail())).thenReturn(Optional.of(user));

        // Act
        boolean result = authenticationService.resetPassword(resetPasswordDto);

        // Assert
        assertThat(result).isFalse();
        assertThat(user.getPasswordHash()).isEqualTo(originalPasswordHash);
    }

    @Test
    @DisplayName("Should fail password reset with expired code")
    void shouldFailPasswordResetWithExpiredCode() {
        // Arrange
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
        resetPasswordDto.setEmail(user.getEmail());
        resetPasswordDto.setPassword("newPassword123");
        resetPasswordDto.setCode(111111);

        user.setCodeExpiredAt(new Timestamp(System.currentTimeMillis() - 1000)); // Expired
        when(userService.getUser(user.getEmail())).thenReturn(Optional.of(user));

        // Act
        boolean result = authenticationService.resetPassword(resetPasswordDto);

        // Assert
        assertThat(result).isFalse();
    }

    /** REFRESH TOKEN TESTS */
    @Test
    @DisplayName("Should refresh token successfully with valid refresh token")
    void shouldRefreshTokenSuccessfully() {
        // Arrange
        String authHeader = "Bearer valid-refresh-token";
        when(jwtService.validateToken("valid-refresh-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("valid-refresh-token")).thenReturn(user.getEmail());
        when(userService.getUser(user.getEmail()))
                .thenReturn(Optional.of(user))
                .thenReturn(Optional.of(user))
                .thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");

        // Act
        LoginResponse response = authenticationService.refreshToken(authHeader);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("valid-refresh-token");
    }

    @Test
    void shouldThrowWithInvalidRefreshToken() {
        when(jwtService.validateToken("invalid-token")).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.refreshToken("Bearer invalid-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid or missing refresh token");
    }

    @Test
    void shouldThrowWithInvalidTokenFormat() {
        assertThatThrownBy(() -> authenticationService.refreshToken("InvalidFormat"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid or missing refresh token");
    }

    @Test
    void shouldThrowWithNullAuthHeader() {
        assertThatThrownBy(() -> authenticationService.refreshToken(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid or missing refresh token");
    }

    /** GOOGLE LOGIN TESTS */
    @Test
    @DisplayName("Should login with Google successfully")
    void shouldGoogleLoginSuccessfully() throws Exception {
        // Arrange
        GoogleIdTokenDto googleIdTokenDto = new GoogleIdTokenDto();
        googleIdTokenDto.setIdToken("valid-google-token");
        googleIdTokenDto.setFcmToken("fcm-token");

        when(googleTokenService.verifyIdToken("valid-google-token")).thenReturn(googleLoginDto);
        when(userService.findOrCreateGoogleUser(googleLoginDto)).thenReturn(user);
        doNothing().when(userService).updateFcmToken(anyString(), anyString());
        when(jwtService.generateAccessToken(user)).thenReturn("google-access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("google-refresh-token");
        when(jwtService.extractExpiration(anyString())).thenReturn(new Date());

        // Act
        LoginResponse response = authenticationService.googleLogin(googleIdTokenDto);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("google-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("google-refresh-token");
        assertThat(response.getUser()).isEqualTo(user);

        verify(googleTokenService).verifyIdToken("valid-google-token");
        verify(userService).findOrCreateGoogleUser(googleLoginDto);
        verify(userService).updateFcmToken(user.getEmail(), "fcm-token");
    }

    @Test
    @DisplayName("Should login with Google without FCM token")
    void shouldGoogleLoginWithoutFcmToken() throws Exception {
        // Arrange
        GoogleIdTokenDto googleIdTokenDto = new GoogleIdTokenDto();
        googleIdTokenDto.setIdToken("valid-google-token");
        googleIdTokenDto.setFcmToken(null);

        when(googleTokenService.verifyIdToken("valid-google-token")).thenReturn(googleLoginDto);
        when(userService.findOrCreateGoogleUser(googleLoginDto)).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        when(jwtService.extractExpiration(anyString())).thenReturn(new Date());

        // Act
        LoginResponse response = authenticationService.googleLogin(googleIdTokenDto);

        // Assert
        assertThat(response).isNotNull();
        verify(userService, never()).updateFcmToken(anyString(), anyString());
    }

}
