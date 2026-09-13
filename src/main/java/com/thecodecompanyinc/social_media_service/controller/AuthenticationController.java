package com.thecodecompanyinc.social_media_service.controller;

import static com.thecodecompanyinc.social_media_service.response.ApiResponse.createFailureResponse;
import static com.thecodecompanyinc.social_media_service.response.ApiResponse.createSuccessResponse;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;

import com.thecodecompanyinc.social_media_service.dto.auth.LoginUserDto;
import com.thecodecompanyinc.social_media_service.dto.auth.RegenerateCodeDto;
import com.thecodecompanyinc.social_media_service.dto.auth.RegisterUserDto;
import com.thecodecompanyinc.social_media_service.dto.auth.ResetPasswordDto;
import com.thecodecompanyinc.social_media_service.dto.auth.VerifyCodeDto;
import com.thecodecompanyinc.social_media_service.entity.User;
import com.thecodecompanyinc.social_media_service.response.ApiResponse;
import com.thecodecompanyinc.social_media_service.response.LoginResponse;
import com.thecodecompanyinc.social_media_service.response.MessageResponse;
import com.thecodecompanyinc.social_media_service.service.auth.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and user registration APIs")
public class AuthenticationController {

  private final AuthenticationService authenticationService;

  @Operation(
      summary = "Register an admin user",
      description = "Creates a new admin user account with the provided registration details")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Admin user registered successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Email already exists")
      })
  @PostMapping("/admin/signup")
  public ResponseEntity<ApiResponse<User>> adminRegister(
      @Valid @RequestBody RegisterUserDto registerUserDto) {
    User registeredUser = authenticationService.signup(registerUserDto, "ADMIN");
    return ResponseEntity.status(CREATED).body(createSuccessResponse(registeredUser));
  }

  @Operation(
      summary = "Register a client user",
      description = "Creates a new client user account with the provided registration details")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Client user registered successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Email already exists")
      })
  @PostMapping("/client/signup")
  public ResponseEntity<ApiResponse<User>> clientRegister(
      @Valid @RequestBody RegisterUserDto registerUserDto) {
    User registeredUser = authenticationService.signup(registerUserDto, "CLIENT");
    return ResponseEntity.status(CREATED).body(createSuccessResponse(registeredUser));
  }

  @Operation(
      summary = "Verify user account",
      description = "Verifies a user account using the verification code sent to their email")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Account verified successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid or expired verification code"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found")
      })
  @PostMapping("/verify-code")
  public ResponseEntity<ApiResponse<MessageResponse>> verifyCode(
      @Valid @RequestBody VerifyCodeDto verifyCodeDto) {
    boolean success = authenticationService.verifyUser(verifyCodeDto);
    if (success) {
      return ResponseEntity.ok(
          createSuccessResponse(new MessageResponse("Account Verified Successfully!")));
    } else {
      return ResponseEntity.badRequest()
          .body(createFailureResponse("Verification Failed, Code Might Be Invalid Or Expired!"));
    }
  }

  @Operation(
      summary = "Regenerate OTP code",
      description = "Generates and sends a new OTP verification code to the user's email")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "OTP regenerated and sent successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found")
      })
  @PostMapping("/regenerate-code")
  public ResponseEntity<ApiResponse<MessageResponse>> regenerateOtp(
      @Valid @RequestBody RegenerateCodeDto regenerateCodeDto) {
    authenticationService.regenerateOtp(regenerateCodeDto.getEmail());
    return ResponseEntity.ok(
        createSuccessResponse(
            new MessageResponse(
                "OTP regenerated successfully." + " Check your email for the new OTP.")));
  }

  @Operation(
      summary = "User login",
      description = "Authenticates a user and returns access and refresh tokens")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Login successful, tokens returned"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Invalid credentials")
      })
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<LoginResponse>> login(
      @Valid @RequestBody LoginUserDto loginUserDto) {
    LoginResponse loginResponse = authenticationService.login(loginUserDto);
    return ResponseEntity.ok(createSuccessResponse(loginResponse));
  }

  @Operation(
      summary = "Refresh access token",
      description = "Generates a new access token using a valid refresh token")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Token refreshed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid or missing authorization header"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Refresh token expired")
      })
  @GetMapping("/token-refresh")
  public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
      @Parameter(description = "Bearer refresh token", required = true)
          @RequestHeader("Authorization")
          String authorizationHeader) {
    LoginResponse loginResponse = authenticationService.refreshToken(authorizationHeader);
    if (loginResponse == null)
      throw new IllegalArgumentException(
          "Failed to refresh token: No authorization header provided");
    return ResponseEntity.ok(createSuccessResponse(loginResponse));
  }

  @Operation(
      summary = "Reset password",
      description = "Resets user password using a valid verification code")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Password reset successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Invalid or expired reset code"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found")
      })
  @PostMapping("/reset-password")
  public ResponseEntity<ApiResponse<MessageResponse>> resetPassword(
      @Valid @RequestBody ResetPasswordDto resetPasswordDto) {
    boolean success = authenticationService.resetPassword(resetPasswordDto);
    if (success) {
      return ResponseEntity.ok(
          createSuccessResponse(new MessageResponse("Password Reset Successfully!")));
    } else {
      return ResponseEntity.status(FORBIDDEN)
          .body(createFailureResponse("Reset Failed," + " Code Might Be Invalid Or Expired!"));
    }
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<User>> getMe(
      @Parameter(description = "Bearer access token", required = true)
          @RequestHeader("Authorization")
          String authorizationHeader) {
    User user = authenticationService.getMe(authorizationHeader);
    return ResponseEntity.ok(createSuccessResponse(user));
  }

  @Operation(
      summary = "Health check",
      description = "Simple endpoint to verify the authentication service is running")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "200",
      description = "Service is healthy")
  @GetMapping("/test")
  public String test() {
    return "Authentication Service is up and running!";
  }
}
