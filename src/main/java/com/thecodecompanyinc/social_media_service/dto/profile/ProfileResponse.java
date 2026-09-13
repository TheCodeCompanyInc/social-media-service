package com.thecodecompanyinc.social_media_service.dto.profile;

import com.thecodecompanyinc.social_media_service.entity.OnlineStatus;

public class ProfileResponse {
    public Long userId;
    public String username;
    public String firstName;
    public String lastName;
    public String imageUrl;
    public String bio;
    public OnlineStatus onlineStatus;
}