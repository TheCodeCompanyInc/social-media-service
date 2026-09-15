package com.thecodecompanyinc.social_media_service.service.profile;


import com.thecodecompanyinc.social_media_service.response.ProfileResponse;
import com.thecodecompanyinc.social_media_service.dto.profile.ProfileUpdateRequestDto;

public interface ProfileService {
    ProfileResponse getProfileById(Long userId);

    ProfileResponse updateProfile(Long userId, ProfileUpdateRequestDto profileUpdateRequestDto);
}