package com.thecodecompanyinc.social_media_service.mapper;

import com.thecodecompanyinc.social_media_service.dto.profile.ProfileResponse;
import com.thecodecompanyinc.social_media_service.dto.profile.ProfileUpdateRequest;
import com.thecodecompanyinc.social_media_service.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    @Mapping(source = "id", target = "userId")
    ProfileResponse toResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void applyUpdate(ProfileUpdateRequest request, @MappingTarget User user);
}