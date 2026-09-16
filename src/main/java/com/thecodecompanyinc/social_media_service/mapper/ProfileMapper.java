package com.thecodecompanyinc.social_media_service.mapper;

import com.thecodecompanyinc.social_media_service.response.ProfileResponse;
import com.thecodecompanyinc.social_media_service.dto.profile.ProfileUpdateRequestDto;
import com.thecodecompanyinc.social_media_service.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    @Mapping(source = "id", target = "userId")
    ProfileResponse toResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void applyUpdate(ProfileUpdateRequestDto request, @MappingTarget User user);
}