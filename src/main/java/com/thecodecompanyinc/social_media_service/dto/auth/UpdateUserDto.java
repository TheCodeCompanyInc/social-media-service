package com.thecodecompanyinc.social_media_service.dto.auth;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserDto {
    @Size(max = 30, message = "First name must be less than 30 characters")
    private String firstName;

    @Size(max = 30, message = "Last name must be less than 30 characters")
    private String lastName;

    private String dob;
}
