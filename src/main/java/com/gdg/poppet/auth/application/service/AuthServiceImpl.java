package com.gdg.poppet.auth.application.service;

import com.gdg.poppet.auth.application.dto.response.GoogleTokenResponse;
import com.gdg.poppet.auth.application.dto.response.GoogleUserInfo;
import com.gdg.poppet.auth.application.dto.response.KakaoOAuthTokenDTO;
import com.gdg.poppet.auth.application.dto.response.KakaoProfileDTO;
import com.gdg.poppet.auth.domain.converter.AuthConverter;
import com.gdg.poppet.auth.infra.util.KakaoAuthClient;
import com.gdg.poppet.user.application.dto.response.UserDto;
import com.gdg.poppet.user.domain.model.User;
import com.gdg.poppet.user.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final WebClient webClient;
    private final KakaoAuthClient kakaoAuthClient;
    private final UserRepository userRepository;
    private final JwtService jwtService;


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

        return UserDto.of(user.getUsername());
    }

    public UserDto googleOAuthLogin(String code, HttpServletResponse response) {
        // 1) code → AccessToken 교환
        GoogleTokenResponse tokenResponse = webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters
                        .fromFormData("grant_type", "authorization_code")
                        .with("client_id",     clientId)
                        .with("client_secret", clientSecret)
                        .with("redirect_uri",  redirectUri)
                        .with("code",          code)
                )
                .retrieve()
                .bodyToMono(GoogleTokenResponse.class)
                .block();

        // 2) AccessToken → 사용자 정보 조회
        GoogleUserInfo userInfo = webClient.get()
                .uri(userInfoUri)
                .headers(h -> h.setBearerAuth(tokenResponse.getAccessToken()))
                .retrieve()
                .bodyToMono(GoogleUserInfo.class)
                .block();

        // 3) DB 조회/가입 처리
        User user = userRepository.findByEmail(userInfo.getEmail())
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(userInfo.getEmail())
                                .name(userInfo.getName())
                                .roles(Set.of("ROLE_USER"))
                                .build()
                ));

        // 4) JWT 발급 및 응답 헤더/쿠키 세팅
        String jwt = jwtService.createAccessToken(user.getEmail());
        response.addHeader("Authorization", "Bearer " + jwt);

        return new UserDto(user);
    }



    private User createNewUser(KakaoProfileDTO kakaoProfile) {
        User newUser = AuthConverter.toUser(kakaoProfile);
        newUser.setAge(getEstimatedAge(kakaoProfile.getKakaoAccount().getAgeRange()));

        return userRepository.save(newUser);
    }

    // 카카오는 나이를 20대, 30대 형태로 제공해 줌.
    // 나이 범위 -> 평균 나이
    private int getEstimatedAge(String ageRange) {
        if (ageRange != null && ageRange.contains("~")) {
            try {
                String[] range = ageRange.split("~");
                int minAge = Integer.parseInt(range[0].trim());
                int maxAge = Integer.parseInt(range[1].trim());
                return (minAge + maxAge) / 2;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return -1;
    }

}
