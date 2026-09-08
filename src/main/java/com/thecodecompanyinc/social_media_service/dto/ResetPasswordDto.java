package com.thecodecompanyinc.social_media_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ResetPasswordDto {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    private String password;

    @NotNull(message = "Verification code is required")
    @Min(value = 100000, message = "Code must be a 6-digit number")
    @Max(value = 999999, message = "Code must be a 6-digit number")
    private Integer code;
}
