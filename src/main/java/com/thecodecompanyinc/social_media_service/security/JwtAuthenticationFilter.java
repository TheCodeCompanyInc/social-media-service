package com.thecodecompanyinc.social_media_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thecodecompanyinc.social_media_service.response.ApiResponse;
import com.thecodecompanyinc.social_media_service.service.jwt.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      final String jwt = authHeader.substring(7);
      if (!jwtService.validateToken(jwt)) {
        filterChain.doFilter(request, response);
        return;
      }

      handleUserAuthentication(jwt, request);
    } catch (DisabledException e) {
      sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
      log.error("Authentication failed: {}", e.getMessage());
      return;
    } catch (ExpiredJwtException e) {
      sendErrorResponse(
          response, HttpServletResponse.SC_UNAUTHORIZED, "Token expired: " + e.getMessage());
      log.error("Token Expired: {}", e.getMessage());
      return;
    } catch (Exception e) {
      sendErrorResponse(
          response,
          HttpServletResponse.SC_UNAUTHORIZED,
          "JWT authentication failed: " + e.getMessage());
      log.error("JwtAuthenticationFilterError: {}", e.getMessage());
      return;
    }

    filterChain.doFilter(request, response);
  }

  private void handleUserAuthentication(String jwt, HttpServletRequest request) {
    final String email = jwtService.getEmailFromToken(jwt);

    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = userDetailsService.loadUserByUsername(email);

      if (!userDetails.isEnabled()) {
        throw new RuntimeException("User is not verified!");
      }

      UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

      authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authToken);
    }
  }

  private void sendErrorResponse(HttpServletResponse response, int status, String message)
      throws IOException {
    response.setStatus(status);
    response.setContentType("application/json");

    ApiResponse<Object> apiResponse = ApiResponse.createFailureResponse(message);
    PrintWriter out = response.getWriter();
    out.write(objectMapper.writeValueAsString(apiResponse));
    out.flush();
  }
}
