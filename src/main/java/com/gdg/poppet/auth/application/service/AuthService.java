package com.gdg.poppet.auth.application.service;

import com.gdg.poppet.auth.application.dto.response.OAuthResult;
import com.gdg.poppet.user.application.dto.response.UserDto;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    OAuthResult kakaoOAuthLogin(String accessCode);
    OAuthResult kakaoOAuthLoginWithTokens(String accessToken);
    OAuthResult appleOAuthLoginWithTokens(String identityToken);
}
