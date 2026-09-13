package com.thecodecompanyinc.social_media_service.service.profile;


import com.thecodecompanyinc.social_media_service.dto.profile.ProfileResponse;
import com.thecodecompanyinc.social_media_service.dto.profile.ProfileUpdateRequest;

public interface ProfileService {
    ProfileResponse getProfileById(Long userId);

    ProfileResponse updateProfile(Long userId, ProfileUpdateRequest profileUpdateRequest);
}