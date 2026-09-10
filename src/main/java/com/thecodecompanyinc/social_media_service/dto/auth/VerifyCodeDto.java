package com.thecodecompanyinc.social_media_service.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerifyCodeDto {
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @NotNull(message = "Verification code is required")
    @Min(value = 100000, message = "Code must be a 6-digit number")
    @Max(value = 999999, message = "Code must be a 6-digit number")
    private int code;
}
