package com.thecodecompanyinc.social_media_service.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleLoginDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    private String name;

    private String givenName;

    private String familyName;
}
