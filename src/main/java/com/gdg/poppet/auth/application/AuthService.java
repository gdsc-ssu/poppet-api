package com.gdg.poppet.auth.application;

import com.gdg.poppet.user.domain.model.User;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    User kakaoOAuthLogin(String accessCode, HttpServletResponse httpServletResponse);
}
