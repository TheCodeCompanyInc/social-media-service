package com.thecodecompanyinc.social_media_service.service.jwt;

import io.jsonwebtoken.Claims;
import java.util.Date;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    String generateAccessToken(UserDetails userDetails);

    String generateRefreshToken(UserDetails userDetails);

    String getEmailFromToken(String token);

    boolean validateToken(String token);

    Date extractExpiration(String token);

    Claims extractAllClaims(String token);
}
