package com.thecodecompanyinc.social_media_service.service.profile;


import com.thecodecompanyinc.social_media_service.dto.profile.ProfileResponse;

public interface ProfileService {
    ProfileResponse getProfileById(Long userId);

}