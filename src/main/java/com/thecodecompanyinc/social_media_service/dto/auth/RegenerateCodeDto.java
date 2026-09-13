package com.thecodecompanyinc.social_media_service.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegenerateCodeDto {
  @NotBlank(message = "Email cannot be blank")
  @Email(message = "Email must be valid")
  private String email;
}
