package com.thecodecompanyinc.social_media_service.response;

import com.thecodecompanyinc.social_media_service.entity.User;
import java.util.Date;
import lombok.Data;

@Data
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Date accessTokenExpiresIn;
    private Date refreshTokenExpiresIn;
    private User user;
}
