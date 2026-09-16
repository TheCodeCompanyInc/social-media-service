package com.thecodecompanyinc.social_media_service.service.profile;


import com.thecodecompanyinc.social_media_service.response.ProfileResponse;
import com.thecodecompanyinc.social_media_service.dto.profile.ProfileUpdateRequestDto;
import com.thecodecompanyinc.social_media_service.entity.User;

public interface ProfileService {
    ProfileResponse getProfileById(Long userId);

    ProfileResponse getCurrentProfile(User user);

    ProfileResponse updateProfile(Long userId, ProfileUpdateRequestDto profileUpdateRequestDto);
}