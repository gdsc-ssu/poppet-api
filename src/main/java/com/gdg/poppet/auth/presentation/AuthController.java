package com.gdg.poppet.auth.presentation;

import com.gdg.poppet.auth.application.dto.response.LoginRequest;
import com.gdg.poppet.auth.application.dto.response.OAuthResult;
import com.gdg.poppet.auth.application.service.AuthService;
import com.gdg.poppet.global.response.ApiResponse;
import com.gdg.poppet.global.status.SuccessStatus;
import com.gdg.poppet.user.application.dto.response.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class AuthController {
    private final AuthService authService;

    // 웹용 OAuth 로그인 (기존 유지)
    @GetMapping("/auth/login/kakao")
    public ResponseEntity<ApiResponse<UserDto>> kakaoLogin(@RequestParam("code") String accessCode) {
        log.info("Web OAuth login attempt with code: {}", accessCode);
        OAuthResult result = authService.kakaoOAuthLogin(accessCode);
        return ApiResponse.successWithToken(
                SuccessStatus.LOGIN_SUCCESS,
                result.userDto(),
                result.accessToken()
        );
    }

    // Flutter 앱용 토큰 로그인 (개선된 엔드포인트)
    @PostMapping("/auth/login/kakao/mobile")
    public ResponseEntity<ApiResponse<UserDto>> kakaoMobileLogin(
            @RequestBody LoginRequest req
    ) {
        log.info("Mobile app login attempt with access token");
        try {
            OAuthResult result = authService.kakaoOAuthLoginWithTokens(req.getAccessToken());
            return ApiResponse.successWithToken(
                    SuccessStatus.LOGIN_SUCCESS,
                    result.userDto(),
                    result.accessToken()
            );
        } catch (Exception e) {
            log.error("Mobile kakao login failed", e);
            throw e;
        }
    }

    // 기존 POST 엔드포인트도 유지 (하위 호환성)
    @PostMapping("/auth/login/kakao")
    public ResponseEntity<ApiResponse<UserDto>> kakaoLoginWithToken(
            @RequestBody LoginRequest req
    ) {
        return kakaoMobileLogin(req);
    }

    // Apple 앱용 토큰 로그인
    @PostMapping("/auth/login/apple/mobile")
    public ResponseEntity<ApiResponse<UserDto>> appleMobileLogin(
            @RequestBody LoginRequest req
    ) {
        log.info("Apple mobile app login attempt with identity token");
        try {
            OAuthResult result = authService.appleOAuthLoginWithTokens(req.getAccessToken());
            return ApiResponse.successWithToken(
                    SuccessStatus.LOGIN_SUCCESS,
                    result.userDto(),
                    result.accessToken()
            );
        } catch (Exception e) {
            log.error("Apple mobile login failed", e);
            throw e;
        }
    }
}
