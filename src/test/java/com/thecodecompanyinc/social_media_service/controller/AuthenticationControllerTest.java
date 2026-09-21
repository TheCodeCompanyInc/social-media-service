package com.thecodecompanyinc.social_media_service.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thecodecompanyinc.social_media_service.dto.auth.GoogleIdTokenDto;
import com.thecodecompanyinc.social_media_service.dto.auth.LoginUserDto;
import com.thecodecompanyinc.social_media_service.dto.auth.RegenerateCodeDto;
import com.thecodecompanyinc.social_media_service.dto.auth.RegisterUserDto;
import com.thecodecompanyinc.social_media_service.dto.auth.ResetPasswordDto;
import com.thecodecompanyinc.social_media_service.dto.auth.VerifyCodeDto;
import com.thecodecompanyinc.social_media_service.entity.Role;
import com.thecodecompanyinc.social_media_service.entity.User;
import com.thecodecompanyinc.social_media_service.exception.GlobalExceptionHandler;
import com.thecodecompanyinc.social_media_service.response.LoginResponse;
import com.thecodecompanyinc.social_media_service.service.auth.AuthenticationService;

import io.jsonwebtoken.ExpiredJwtException;

/**
 * NOTE: ALL THOSE TESTS ARE AI-GENERATED AND REVIEWED MANUALLY
 *
 * <p>
 * Unit tests for AuthenticationController Tests REST endpoints with mocked
 * service layer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationController Unit Tests")
class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private User user;
    private LoginUserDto loginUserDto;
    private RegisterUserDto registerUserDto;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        // Setup test user
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("johndoe");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhoneNumber("1234567890");
        user.setRole(Role.CLIENT);

        // Setup login DTO
        loginUserDto = new LoginUserDto();
        loginUserDto.setEmail("test@example.com");
        loginUserDto.setPassword("password123");
        loginUserDto.setFcmToken("fcm-token");

        // Setup register DTO
        registerUserDto = new RegisterUserDto();
        registerUserDto.setUsername("johndoe");
        registerUserDto.setEmail("newuser@example.com");
        registerUserDto.setPassword("password123");
        registerUserDto.setFirstName("Jane");
        registerUserDto.setLastName("Smith");
        registerUserDto.setPhoneNumber("9876543210");

        // Setup login response
        loginResponse = new LoginResponse();
        loginResponse.setAccessToken("access-token");
        loginResponse.setRefreshToken("refresh-token");
        loginResponse.setAccessTokenExpiresIn(new Date());
        loginResponse.setRefreshTokenExpiresIn(new Date());
        loginResponse.setUser(user);
    }

    /** ADMIN SIGNUP TESTS */
    @Test
    @DisplayName("POST /api/auth/admin/signup - Should register admin successfully")
    void shouldRegisterAdminSuccessfully() throws Exception {
        // Arrange
        when(authenticationService.signup(any(RegisterUserDto.class), eq("ADMIN")))
                .thenReturn(user);

        // Act & Assert
        mockMvc.perform(
                post("/api/auth/admin/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(user.getEmail()))
                .andExpect(jsonPath("$.data.firstName").value(user.getFirstName()));

        verify(authenticationService, times(1)).signup(any(RegisterUserDto.class), eq("ADMIN"));
    }

    @Test
    @DisplayName("POST /api/auth/admin/signup - Should return 500 on failure")
    void shouldReturn500OnAdminSignupFailure() throws Exception {
        // Arrange
        when(authenticationService.signup(any(RegisterUserDto.class), eq("ADMIN")))
                .thenThrow(new RuntimeException("Email already exists"));

        // Act & Assert
        mockMvc.perform(
                post("/api/auth/admin/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDto)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Email already exists")));
    }

    /** CLIENT SIGNUP TESTS */
    @Test
    @DisplayName("POST /api/auth/client/signup - Should register client successfully")
    void shouldRegisterClientSuccessfully() throws Exception {
        // Arrange
        when(authenticationService.signup(any(RegisterUserDto.class), eq("CLIENT")))
                .thenReturn(user);

        // Act & Assert
        mockMvc.perform(
                post("/api/auth/client/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(user.getEmail()));

        verify(authenticationService, times(1)).signup(any(RegisterUserDto.class), eq("CLIENT"));
    }

    @Test
    @DisplayName("POST /api/auth/client/signup - Should return 409 on duplicate email")
    void shouldReturn409OnClientSignupFailure() throws Exception {
        // Arrange
        when(authenticationService.signup(any(RegisterUserDto.class), eq("CLIENT")))
                .thenThrow(new IllegalStateException("Email Already Exists!"));

        // Act & Assert
        mockMvc.perform(
                post("/api/auth/client/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDto)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email Already Exists!"));
    }

    /** LOGIN TESTS */
    @Test
    @DisplayName("POST /api/auth/login - Should login successfully")
    void shouldLoginSuccessfully() throws Exception {
        // Arrange
        when(authenticationService.login(any(LoginUserDto.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUserDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.user.email").value(user.getEmail()));

        verify(authenticationService, times(1)).login(any(LoginUserDto.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 401 with wrong credentials")
    void shouldReturn401WithWrongCredentials() throws Exception {
        // Arrange
        when(authenticationService.login(any(LoginUserDto.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUserDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Bad credentials")));
    }

    /** VERIFY CODE TESTS */
    @Test
    @DisplayName("POST /api/auth/verify-code - Should verify code successfully")
    void shouldVerifyCodeSuccessfully() throws Exception {
        // Arrange
        VerifyCodeDto verifyCodeDto = new VerifyCodeDto();
        verifyCodeDto.setEmail("test@example.com");
        verifyCodeDto.setCode(111111);

        when(authenticationService.verifyUser(any(VerifyCodeDto.class))).thenReturn(true);

        // Act & Assert
        mockMvc.perform(
                post("/api/auth/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyCodeDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Account Verified Successfully!"));

        verify(authenticationService, times(1)).verifyUser(any(VerifyCodeDto.class));
    }

    @Test
    @DisplayName("POST /api/auth/verify-code - Should return 400 with invalid code")
    void shouldReturn400WithInvalidCode() throws Exception {
        // Arrange
        VerifyCodeDto verifyCodeDto = new VerifyCodeDto();
        verifyCodeDto.setEmail("test@example.com");
        verifyCodeDto.setCode(999999);

        when(authenticationService.verifyUser(any(VerifyCodeDto.class))).thenReturn(false);

        // Act & Assert
        mockMvc.perform(
                post("/api/auth/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyCodeDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Verification Failed")));
    }

    @Test
    @DisplayName("POST /api/auth/verify-code - Should return 404 when user not found")
    void shouldReturn404WhenUserNotFoundOnVerify() throws Exception {
        // Arrange
        VerifyCodeDto verifyCodeDto = new VerifyCodeDto();
        verifyCodeDto.setEmail("nonexistent@example.com");
        verifyCodeDto.setCode(111111);

        when(authenticationService.verifyUser(any(VerifyCodeDto.class)))
                .thenThrow(new NoSuchElementException("User not found"));

        // Act & Assert
        mockMvc.perform(
                post("/api/auth/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyCodeDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("POST /api/auth/verify-code - Should handle generic exceptions")
    void shouldHandleGenericExceptionsOnVerify() throws Exception {
        // Arrange
        VerifyCodeDto verifyCodeDto = new VerifyCodeDto();
        verifyCodeDto.setEmail("test@example.com");
        verifyCodeDto.setCode(111111);

        when(authenticationService.verifyUser(any(VerifyCodeDto.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(
                post("/api/auth/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyCodeDto)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Database error")));
    }

    /** REGENERATE CODE TESTS */
    @Test
    @DisplayName("POST /api/auth/regenerate-code - Should regenerate OTP successfully")
    void shouldRegenerateOtpSuccessfully() throws Exception {
        // Arrange
        RegenerateCodeDto regenerateCodeDto = new RegenerateCodeDto();
        regenerateCodeDto.setEmail("test@example.com");

        doNothing().when(authenticationService).regenerateOtp(anyString());

        // Act & Assert
        mockMvc.perform(
                post("/api/auth/regenerate-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regenerateCodeDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(
                        jsonPath("$.data.message")
                                .value(containsString("OTP regenerated successfully")));

        verify(authenticationService, times(1)).regenerateOtp(regenerateCodeDto.getEmail());
    }

    @Test
    @DisplayName("POST /api/auth/regenerate-code - Should return 404 when user not found")
    void shouldReturn404WhenUserNotFoundOnRegenerate() throws Exception {
        // Arrange
        RegenerateCodeDto regenerateCodeDto = new RegenerateCodeDto();
        regenerateCodeDto.setEmail("nonexistent@example.com");

        doThrow(new NoSuchElementException("User not found"))
                .when(authenticationService)
                .regenerateOtp(anyString());

        // Act & Assert
        mockMvc.perform(
                post("/api/auth/regenerate-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regenerateCodeDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("POST /api/auth/regenerate-code - Should return 500 on internal error")
    void shouldReturn500OnInternalErrorDuringRegenerate() throws Exception {
        // Arrange
        RegenerateCodeDto regenerateCodeDto = new RegenerateCodeDto();
        regenerateCodeDto.setEmail("test@example.com");

        doThrow(new RuntimeException("Mail server error"))
                .when(authenticationService)
                .regenerateOtp(anyString());

        // Act & Assert
        mockMvc.perform(
                post("/api/auth/regenerate-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regenerateCodeDto)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Mail server error")));
    }

    /** TOKEN REFRESH TEST */
    @Test
    @DisplayName("GET /api/auth/token-refresh - Should refresh token successfully")
    void shouldRefreshTokenSuccessfully() throws Exception {
        // Arrange
        when(authenticationService.refreshToken(anyString())).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(
                get("/api/auth/token-refresh")
                        .header("Authorization", "Bearer valid-refresh-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));

        verify(authenticationService, times(1)).refreshToken(anyString());
    }

    @Test
    @DisplayName("GET /api/auth/token-refresh - Should return 400 when service returns null")
    void shouldReturn400WhenRefreshServiceReturnsNull() throws Exception {
        // Arrange
        when(authenticationService.refreshToken(anyString())).thenReturn(null);

        // Act & Assert
        mockMvc.perform(
                get("/api/auth/token-refresh")
                        .header("Authorization", "Bearer invalid-token"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Failed to refresh token: No authorization header"
                                                + " provided"));
    }

    @Test
    @DisplayName("GET /api/auth/token-refresh - Should return 401 with expired token")
    void shouldReturn401WithExpiredToken() throws Exception {
        // Arrange
        when(authenticationService.refreshToken(anyString()))
                .thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        // Act & Assert
        mockMvc.perform(
                get("/api/auth/token-refresh")
                        .header("Authorization", "Bearer expired-token"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Token expired")));
    }

    @Test
    @DisplayName("GET /api/auth/token-refresh - Should return 500 on unexpected error")
    void shouldReturn500OnUnexpectedErrorDuringRefresh() throws Exception {
        // Arrange
        when(authenticationService.refreshToken(anyString()))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/auth/token-refresh").header("Authorization", "Bearer token"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Database error")));
    }

    /** RESET PASSWORD TESTS */
    @Test
    @DisplayName("POST /api/auth/reset-password - Should reset password successfully")
    void shouldResetPasswordSuccessfully() throws Exception {
        // Arrange
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
        resetPasswordDto.setEmail("test@example.com");
        resetPasswordDto.setPassword("newPassword123");
        resetPasswordDto.setCode(111111);

        when(authenticationService.resetPassword(any(ResetPasswordDto.class))).thenReturn(true);

        // Act & Assert
        mockMvc.perform(
                post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Password Reset Successfully!"));

        verify(authenticationService, times(1)).resetPassword(any(ResetPasswordDto.class));
    }

    @Test
    @DisplayName("POST /api/auth/reset-password - Should return 403 with invalid code")
    void shouldReturn403WithInvalidCodeOnReset() throws Exception {
        // Arrange
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
        resetPasswordDto.setEmail("test@example.com");
        resetPasswordDto.setPassword("newPassword123");
        resetPasswordDto.setCode(999999);

        when(authenticationService.resetPassword(any(ResetPasswordDto.class))).thenReturn(false);

        // Act & Assert
        mockMvc.perform(
                post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordDto)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Reset Failed")));
    }

    @Test
    @DisplayName("POST /api/auth/reset-password - Should return 404 when user not found")
    void shouldReturn404WhenUserNotFoundOnReset() throws Exception {
        // Arrange
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
        resetPasswordDto.setEmail("nonexistent@example.com");
        resetPasswordDto.setPassword("newPassword123");
        resetPasswordDto.setCode(111111);

        when(authenticationService.resetPassword(any(ResetPasswordDto.class)))
                .thenThrow(new NoSuchElementException("User not found"));

        // Act & Assert
        mockMvc.perform(
                post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("POST /api/auth/reset-password - Should return 500 on internal error")
    void shouldReturn500OnInternalErrorDuringReset() throws Exception {
        // Arrange
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
        resetPasswordDto.setEmail("test@example.com");
        resetPasswordDto.setPassword("newPassword123");
        resetPasswordDto.setCode(111111);

        when(authenticationService.resetPassword(any(ResetPasswordDto.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(
                post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordDto)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Database error")));
    }

    /** TEST ENDPOINT */
    @Test
    @DisplayName("GET /api/auth/test - Should return service status")
    void shouldReturnServiceStatus() throws Exception {
        mockMvc.perform(get("/api/auth/test"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Authentication Service is up and running!"));
    }

    /** GOOGLE LOGIN TESTS */
    @Test
    @DisplayName("POST /api/auth/google - Should login with Google successfully")
    void shouldGoogleLoginSuccessfully() throws Exception {
        // Arrange
        GoogleIdTokenDto googleIdTokenDto = new GoogleIdTokenDto("valid-google-token", "fcm-token");

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken("access-token");
        loginResponse.setRefreshToken("refresh-token");
        loginResponse.setAccessTokenExpiresIn(new Date(System.currentTimeMillis() + 3600000));
        loginResponse.setRefreshTokenExpiresIn(new Date(System.currentTimeMillis() + 86400000));

        when(authenticationService.googleLogin(any(GoogleIdTokenDto.class)))
                .thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(
                post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(googleIdTokenDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("POST /api/auth/google - Should return 400 with invalid token")
    void shouldReturn400WithInvalidGoogleToken() throws Exception {
        // Arrange
        GoogleIdTokenDto googleIdTokenDto = new GoogleIdTokenDto("invalid-token", null);

        when(authenticationService.googleLogin(any(GoogleIdTokenDto.class)))
                .thenThrow(new IllegalArgumentException("Invalid token"));

        // Act & Assert
        mockMvc.perform(
                post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(googleIdTokenDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }

    @Test
    @DisplayName("POST /api/auth/google - Should return 401 on security exception")
    void shouldReturn401OnGoogleSecurityException() throws Exception {
        // Arrange
        GoogleIdTokenDto googleIdTokenDto = new GoogleIdTokenDto("some-token", null);

        when(authenticationService.googleLogin(any(GoogleIdTokenDto.class)))
                .thenThrow(new GeneralSecurityException("Verification failed"));

        // Act & Assert
        mockMvc.perform(
                post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(googleIdTokenDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Security verification failed"));
    }

    @Test
    @DisplayName("POST /api/auth/google - Should return 500 on unexpected error")
    void shouldReturn500OnGoogleUnexpectedError() throws Exception {
        // Arrange
        GoogleIdTokenDto googleIdTokenDto = new GoogleIdTokenDto("some-token", null);

        when(authenticationService.googleLogin(any(GoogleIdTokenDto.class)))
                .thenThrow(new RuntimeException("Unexpected"));

        // Act & Assert
        mockMvc.perform(
                post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(googleIdTokenDto)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Unexpected")));
    }
}
