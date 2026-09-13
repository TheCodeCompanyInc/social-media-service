package com.thecodecompanyinc.social_media_service.dto.profile;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    @Size(min = 3, max = 30)
    public String username;
    @Size(min = 3, max = 50)
    public String firstName;
    @Size(min = 3, max = 50)
    public String lastName;
    public String imageUrl;
    @Size(max = 500)
    public String bio;
}