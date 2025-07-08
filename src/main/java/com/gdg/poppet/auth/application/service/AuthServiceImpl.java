package com.gdg.poppet.auth.application.service;

import com.gdg.poppet.auth.application.dto.response.AppleProfileDTO;
import com.gdg.poppet.auth.application.dto.response.KakaoProfileDTO;
import com.gdg.poppet.auth.application.dto.response.OAuthResult;
import com.gdg.poppet.auth.infra.util.AppleAuthClient;
import com.gdg.poppet.auth.infra.util.KakaoAuthClient;
import com.gdg.poppet.email.domain.enums.EmailPeriod;
import com.gdg.poppet.global.exception.GlobalException;
import com.gdg.poppet.global.status.ErrorStatus;
import com.gdg.poppet.user.application.dto.response.UserDto;
import com.gdg.poppet.user.domain.enums.Gender;
import com.gdg.poppet.user.domain.enums.Provider;
import com.gdg.poppet.user.domain.model.User;
import com.gdg.poppet.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final KakaoAuthClient kakaoAuthClient;
    private final AppleAuthClient appleAuthClient;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    // For Web
    @Override
    public OAuthResult kakaoOAuthLogin(String accessCode) {
        try {
            // 인가코드로 토큰 발급 후 바로 프로필 조회
            KakaoProfileDTO kakaoProfile = kakaoAuthClient.requestTokenAndProfile(accessCode);
            if (kakaoProfile == null) {
                throw new GlobalException(ErrorStatus.PROFILE_ERROR);
            }

            Provider provider = Provider.KAKAO;

            User user = userRepository.findByUserIdAndProvider(kakaoProfile.getId(), provider)
                    .orElseGet(() -> createNewUser(kakaoProfile));

            // JWT 생성
            String jwt = jwtService.createAccessToken(user.getUserId(), user.getProvider());
            UserDto dto = UserDto.of(user.getUsername());

            return new OAuthResult(jwt, dto);
        } catch (Exception e) {
            log.error("카카오 OAuth 로그인 실패: {}", e.getMessage(), e);
            throw new GlobalException(ErrorStatus.OAUTH_ERROR);
        }
    }

    //For Mobile
    @Override
    public OAuthResult kakaoOAuthLoginWithTokens(String accessToken) {
        try {
            // 액세스 토큰으로 프로필 조회
            KakaoProfileDTO profile = kakaoAuthClient.verifyAccessToken(accessToken);
            if (profile == null) {
                throw new GlobalException(ErrorStatus.PROFILE_ERROR);
            }

            Provider provider = Provider.KAKAO;

            // 사용자 조회/생성
            User user = userRepository
                    .findByUserIdAndProvider(profile.getId(), provider)
                    .orElseGet(() -> createNewUser(profile));

            // JWT 발급
            String jwt = jwtService.createAccessToken(user.getUserId(), user.getProvider());
            return new OAuthResult(jwt, UserDto.of(user.getUsername()));
        } catch (Exception e) {
            log.error("카카오 토큰 로그인 실패: {}", e.getMessage(), e);
            throw new GlobalException(ErrorStatus.OAUTH_ERROR);
        }
    }

    // For Apple Mobile
    @Override
    public OAuthResult appleOAuthLoginWithTokens(String identityToken) {
        try {
            // Identity Token 검증 및 프로필 조회
            AppleProfileDTO profile = appleAuthClient.verifyIdentityToken(identityToken);
            if (profile == null) {
                throw new GlobalException(ErrorStatus.PROFILE_ERROR);
            }

            Provider provider = Provider.APPLE;

            // 사용자 조회/생성
            User user = userRepository
                    .findByUserIdAndProvider(profile.getId(), provider)
                    .orElseGet(() -> createNewAppleUser(profile));

            // JWT 발급
            String jwt = jwtService.createAccessToken(user.getUserId(), user.getProvider());
            return new OAuthResult(jwt, UserDto.of(user.getUsername()));
        } catch (Exception e) {
            log.error("애플 토큰 로그인 실패: {}", e.getMessage(), e);
            throw new GlobalException(ErrorStatus.OAUTH_ERROR);
        }
    }

    private User createNewUser(KakaoProfileDTO kakaoProfile) {
        try {
            // 안전한 카카오 계정 정보 접근
            var kakaoAccount = kakaoProfile.getKakaoAccount();
            if (kakaoAccount == null) {
                throw new GlobalException(ErrorStatus.PROFILE_ERROR);
            }

            Gender gender = Gender.MALE; // 기본값
            if (kakaoAccount.getGender() != null && !kakaoAccount.getGender().isEmpty()) {
                try {
                    gender = Gender.fromString(kakaoAccount.getGender());
                } catch (Exception e) {
                    log.warn("성별 파싱 실패, 기본값 사용: {}", kakaoAccount.getGender());
                }
            }

            int estimatedAge = getEstimatedAge(kakaoAccount.getAgeRange());

            String username = kakaoAccount.getName();
            if (username == null || username.trim().isEmpty()) {
                username = "사용자" + kakaoProfile.getId(); // 기본 이름
            }

            return userRepository.save(
                    User.builder()
                            .userId(kakaoProfile.getId())
                            .provider(Provider.KAKAO)
                            .username(username)
                            .gender(gender)
                            .emailPeriod(EmailPeriod.THREE)
                            .age(estimatedAge)
                            .build());
        } catch (Exception e) {
            log.error("사용자 생성 실패: {}", e.getMessage(), e);
            throw new GlobalException(ErrorStatus.USER_CREATE_ERROR);
        }
    }

    private User createNewAppleUser(AppleProfileDTO appleProfile) {
        try {
            // Apple은 성별과 나이 정보를 제공하지 않으므로 기본값 사용
            Gender gender = Gender.MALE; // 기본값
            int defaultAge = 25; // 기본 나이

            // Apple은 이메일이 있을 때만 사용자명으로 사용, 없으면 기본 이름
            String username = appleProfile.getValidEmail();
            if (username == null || username.trim().isEmpty()) {
                username = "Apple사용자" + appleProfile.getId().substring(0, 8); // 기본 이름
            } else {
                // 이메일에서 @ 앞 부분을 사용자명으로 사용
                username = username.split("@")[0];
            }

            return userRepository.save(
                    User.builder()
                            .userId(appleProfile.getId())
                            .provider(Provider.APPLE)
                            .username(username)
                            .gender(gender)
                            .emailPeriod(EmailPeriod.THREE)
                            .age(defaultAge)
                            .build());
        } catch (Exception e) {
            log.error("Apple 사용자 생성 실패: {}", e.getMessage(), e);
            throw new GlobalException(ErrorStatus.USER_CREATE_ERROR);
        }
    }

    // 카카오 나이 범위 파싱 수정
    private int getEstimatedAge(String ageRange) {
        if (ageRange == null || ageRange.trim().isEmpty()) {
            return 25; // 기본 나이
        }

        try {
            // 카카오는 "20~29" 또는 "20대" 형태로 제공
            if (ageRange.contains("~")) {
                // "20~29" 형태
                String[] range = ageRange.split("~");
                int minAge = Integer.parseInt(range[0].trim());
                int maxAge = Integer.parseInt(range[1].trim());
                return (minAge + maxAge) / 2;
            } else if (ageRange.contains("대")) {
                // "20대" 형태
                String ageStr = ageRange.replace("대", "").trim();
                int baseAge = Integer.parseInt(ageStr);
                return baseAge + 5; // 20대 -> 25세
            } else {
                // 직접 숫자인 경우
                return Integer.parseInt(ageRange.trim());
            }
        } catch (NumberFormatException e) {
            log.warn("나이 파싱 실패, 기본값 사용: {}", ageRange);
            return 25; // 기본 나이
        }
    }

}
