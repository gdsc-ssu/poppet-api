package com.gdg.poppet.auth.application;

import com.gdg.poppet.user.application.dto.response.UserDto;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    UserDto kakaoOAuthLogin(String accessCode, HttpServletResponse httpServletResponse);
}
