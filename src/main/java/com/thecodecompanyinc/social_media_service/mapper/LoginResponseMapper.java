package com.thecodecompanyinc.social_media_service.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.thecodecompanyinc.social_media_service.entity.User;
import com.thecodecompanyinc.social_media_service.response.LoginResponse;
import com.thecodecompanyinc.social_media_service.service.jwt.JwtService;

@Mapper(componentModel = "spring")
public interface LoginResponseMapper {

    @Mapping(target = "accessToken", source = "accessToken")
    @Mapping(target = "refreshToken", source = "refreshToken")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "accessTokenExpiresIn", expression = "java(jwtService.extractExpiration(accessToken))")
    @Mapping(target = "refreshTokenExpiresIn", expression = "java(jwtService.extractExpiration(refreshToken))")
    LoginResponse toLoginResponse(String accessToken, String refreshToken, User user, @Context JwtService jwtService);
}
