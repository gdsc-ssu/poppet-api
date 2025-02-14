package com.gdg.poppet.auth.presentation;

import com.gdg.poppet.auth.application.AuthService;
import com.gdg.poppet.global.response.ApiResponse;
import com.gdg.poppet.global.status.SuccessStatus;
import com.gdg.poppet.user.domain.model.User;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/auth/login/kakao")
    public ResponseEntity<ApiResponse<User>> kakaoLogin(@RequestParam("code") String accessCode, HttpServletResponse httpServletResponse) {
        authService.kakaoOAuthLogin(accessCode, httpServletResponse);
        return ApiResponse.success(SuccessStatus.LOGIN_SUCCESS, new User());
    }
}
