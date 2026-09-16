package com.thecodecompanyinc.social_media_service.response;

import com.thecodecompanyinc.social_media_service.entity.OnlineStatus;
import lombok.Data;

@Data
public class ProfileResponse {
    public Long userId;
    public String username;
    public String firstName;
    public String lastName;
    public String imageUrl;
    public String bio;
    public OnlineStatus onlineStatus;
}