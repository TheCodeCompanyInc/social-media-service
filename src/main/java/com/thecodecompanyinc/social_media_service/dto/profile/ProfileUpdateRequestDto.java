package com.thecodecompanyinc.social_media_service.dto.profile;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateRequestDto {
    @Size(min = 3, max = 30)
    @Pattern(regexp = "^[a-zA-Z0-9_.]+$", message = "Username can only contain letters, numbers, underscores and dots")
    public String username;
    @Size(max = 30, message = "First name must be less than 30 characters")
    public String firstName;
    @Size(max = 30, message = "Last name must be less than 30 characters")
    public String lastName;
    public String imageUrl;
    @Size(max = 500)
    public String bio;
}