package com.gdg.poppet.auth.application;

import com.gdg.poppet.auth.application.dto.response.KakaoOAuthTokenDTO;
import com.gdg.poppet.auth.application.dto.response.KakaoProfileDTO;
import com.gdg.poppet.auth.application.util.KakaoUtil;
import com.gdg.poppet.user.domain.model.User;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final KakaoUtil kakaoUtil;

    @Override
    public User kakaoOAuthLogin(String accessCode, HttpServletResponse httpServletResponse) {
        // 인가코드로 토근 발급
        KakaoOAuthTokenDTO oAuthToken = kakaoUtil.requestToken(accessCode);
        log.info("Kakao OAuth token: {}", oAuthToken);
        // 토근으로 유저정보 가져오기
        KakaoProfileDTO kakaoProfile = kakaoUtil.requestProfile(oAuthToken);
        log.info("Kakao profile: {}", kakaoProfile);

        // TO-DO : 회원가입/로그인 분기처리
        return null;
    }
}
