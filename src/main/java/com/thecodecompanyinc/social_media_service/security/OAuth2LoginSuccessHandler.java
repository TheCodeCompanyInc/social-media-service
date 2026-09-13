package com.thecodecompanyinc.social_media_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thecodecompanyinc.social_media_service.dto.auth.GoogleLoginDto;
import com.thecodecompanyinc.social_media_service.entity.User;
import com.thecodecompanyinc.social_media_service.response.ApiResponse;
import com.thecodecompanyinc.social_media_service.response.LoginResponse;
import com.thecodecompanyinc.social_media_service.service.jwt.JwtService;
import com.thecodecompanyinc.social_media_service.service.user.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

  private final UserService userService;
  private final JwtService jwtService;
  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationSuccess(
      @NonNull HttpServletRequest request,
      HttpServletResponse response,
      org.springframework.security.core.Authentication authentication)
      throws IOException, ServletException {

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

    GoogleLoginDto googleLoginDto =
        new GoogleLoginDto(
            oAuth2User.getAttribute("email"),
            oAuth2User.getAttribute("name"),
            oAuth2User.getAttribute("given_name"),
            oAuth2User.getAttribute("family_name"));

    User user = userService.findOrCreateGoogleUser(googleLoginDto);

    String accessToken = jwtService.generateAccessToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);

    LoginResponse loginResponse = new LoginResponse();
    loginResponse.setAccessToken(accessToken);
    loginResponse.setRefreshToken(refreshToken);
    loginResponse.setAccessTokenExpiresIn(jwtService.extractExpiration(accessToken));
    loginResponse.setRefreshTokenExpiresIn(jwtService.extractExpiration(refreshToken));
    loginResponse.setUser(user);

    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    ApiResponse<Object> apiResponse = ApiResponse.createSuccessResponse(loginResponse);

    response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    response.getWriter().flush();
  }
}
