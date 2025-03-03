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

        return UserDto.of(user.getUsername());
    }


    private User createNewUser(KakaoProfileDTO kakaoProfile) {
        User newUser = AuthConverter.toUser(
                kakaoProfile.getId(),
                kakaoProfile.getKakaoAccount().getName(),
                kakaoProfile.getKakaoAccount().getGender(),
                getEstimatedAge(kakaoProfile.getKakaoAccount().getAgeRange())
        );
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
