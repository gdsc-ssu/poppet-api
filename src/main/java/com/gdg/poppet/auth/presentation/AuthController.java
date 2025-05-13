package com.gdg.poppet.auth.presentation;

import com.gdg.poppet.auth.application.dto.response.LoginRequest;
import com.gdg.poppet.auth.application.dto.response.OAuthResult;
import com.gdg.poppet.auth.application.service.AuthService;
import com.gdg.poppet.global.response.ApiResponse;
import com.gdg.poppet.global.status.SuccessStatus;
import com.gdg.poppet.user.application.dto.response.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/auth/login/kakao")
    public ResponseEntity<ApiResponse<UserDto>> kakaoLogin(@RequestParam("code") String accessCode) {
        OAuthResult result = authService.kakaoOAuthLogin(accessCode);
        return ApiResponse.successWithToken(
                SuccessStatus.LOGIN_SUCCESS,
                result.userDto(),
                result.accessToken()
        );
    }

    @PostMapping("/auth/login/kakao")
    public ResponseEntity<ApiResponse<UserDto>> kakaoLoginWithToken(
            @RequestBody LoginRequest req
    ) {
        OAuthResult result = authService.kakaoOAuthLoginWithTokens(req.getAccessToken());
        return ApiResponse.successWithToken(
                SuccessStatus.LOGIN_SUCCESS,
                result.userDto(),
                result.accessToken()
        );
    }

    @GetMapping("/auth/login/google")
    public ResponseEntity<ApiResponse<UserDto>> googleLogin(@RequestParam("code") String accessCode) {
        OAuthResult result = authService.googleOAuthLogin(accessCode);
        return ApiResponse.successWithToken(
                SuccessStatus.LOGIN_SUCCESS,
                result.userDto(),
                result.accessToken()
        );
    }

    @PostMapping("/auth/login/google")
    public ResponseEntity<ApiResponse<UserDto>> googleLoginWithTokens(
            @RequestBody LoginRequest req
    ) {
        OAuthResult result = authService.googleOAuthLoginWithTokens(
                req.getIdToken(), req.getAccessToken()
        );
        return ApiResponse.successWithToken(
                SuccessStatus.LOGIN_SUCCESS,
                result.userDto(),
                result.accessToken()
        );
    }

}
