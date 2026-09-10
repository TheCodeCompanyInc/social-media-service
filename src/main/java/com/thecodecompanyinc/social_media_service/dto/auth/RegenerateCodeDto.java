package com.thecodecompanyinc.social_media_service.dto.auth;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class RegenerateCodeDto {
    @Email(message = "Email must be valid")
    private String email;
}
