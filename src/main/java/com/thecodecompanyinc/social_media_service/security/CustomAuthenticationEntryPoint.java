package com.thecodecompanyinc.social_media_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thecodecompanyinc.social_media_service.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      @NonNull AuthenticationException authException)
      throws IOException, ServletException {

    log.warn("Unauthorized access attempt to: {}", request.getRequestURI());

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    ApiResponse<Object> apiResponse =
        ApiResponse.createFailureResponse(
            "Authentication required. Please provide valid credentials.");

    response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    response.getWriter().flush();
  }
}
