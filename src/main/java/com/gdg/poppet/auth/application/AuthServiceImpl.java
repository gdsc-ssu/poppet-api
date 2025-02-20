package com.gdg.poppet.auth.application;

import com.gdg.poppet.auth.application.dto.response.KakaoOAuthTokenDTO;
import com.gdg.poppet.auth.application.dto.response.KakaoProfileDTO;
import com.gdg.poppet.auth.domain.converter.AuthConverter;
import com.gdg.poppet.auth.infra.util.KakaoAuthClient;
import com.gdg.poppet.user.application.dto.response.UserDto;
import com.gdg.poppet.user.domain.model.User;
import com.gdg.poppet.user.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final KakaoAuthClient kakaoAuthClient;
    private final UserRepository userRepository;

    @Override
    public UserDto kakaoOAuthLogin(String accessCode, HttpServletResponse httpServletResponse) {
        // 인가코드로 토근 발급
        KakaoOAuthTokenDTO oAuthToken = kakaoAuthClient.requestToken(accessCode);
        log.info("Kakao OAuth token: {}", oAuthToken);
        // 토큰으로 유저정보 가져오기
        KakaoProfileDTO kakaoProfile = kakaoAuthClient.requestProfile(oAuthToken);
        log.info("Kakao profile: {}", kakaoProfile);

        // 유저정보 ID로 조회 후, 없을 경우 User 생성
        User user = userRepository.findByUserId(kakaoProfile.getId())
                .orElseGet(() -> createNewUser(kakaoProfile));

        // TODO DTO 객체 변환

        return null;
    }

    private User createNewUser(KakaoProfileDTO kakaoProfile) {
        User newUser = AuthConverter.toUser(
                kakaoProfile.getId(),
                kakaoProfile.getKakaoAccount().getEmail(),
                // TODO : gender랑 age 정보 승인 대기중
                null,
                20
        );
        return userRepository.save(newUser);
    }

}
