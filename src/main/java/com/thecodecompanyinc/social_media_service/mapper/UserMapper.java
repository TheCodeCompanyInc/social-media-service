package com.thecodecompanyinc.social_media_service.mapper;

import com.thecodecompanyinc.social_media_service.dto.auth.RegisterUserDto;
import com.thecodecompanyinc.social_media_service.entity.User;
import java.sql.Timestamp;
import java.util.Date;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(
            target = "passwordHash",
            expression = "java(passwordEncoder.encode(dto.getPassword()))")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "dob", ignore = true)
    @Mapping(target = "firebaseToken", ignore = true)
    User toUser(RegisterUserDto dto, @Context PasswordEncoder passwordEncoder);

    @AfterMapping
    default void setComputedFields(RegisterUserDto dto, @MappingTarget User.UserBuilder user) {
        Date now = new Date();
        long oneDayMs = 86_400_000L;
        user.join_date(new Timestamp(now.getTime()))
                .code(111111)
                .codeExpiredAt(new Timestamp(now.getTime() + oneDayMs))
                .emailVerified(false);
    }
}
