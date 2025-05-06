package com.gdg.poppet.auth.application.service;

import com.gdg.poppet.auth.application.dto.response.GoogleExtraProfile;
import com.gdg.poppet.auth.application.dto.response.GoogleTokenResponse;
import com.gdg.poppet.auth.application.dto.response.GoogleUserInfo;
import com.gdg.poppet.auth.application.dto.response.KakaoOAuthTokenDTO;
import com.gdg.poppet.auth.application.dto.response.KakaoProfileDTO;
import com.gdg.poppet.auth.application.dto.response.OAuthResult;
import com.gdg.poppet.auth.infra.util.GoogleAuthClient;
import com.gdg.poppet.auth.infra.util.KakaoAuthClient;
import com.gdg.poppet.email.domain.enums.EmailPeriod;
import com.gdg.poppet.user.application.dto.response.UserDto;
import com.gdg.poppet.user.domain.enums.Gender;
import com.gdg.poppet.user.domain.enums.Provider;
import com.gdg.poppet.user.domain.model.User;
import com.gdg.poppet.user.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.Period;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final GoogleAuthClient googleAuthClient;
    private final KakaoAuthClient kakaoAuthClient;
    private final UserRepository userRepository;
    private final JwtService jwtService;


    @Override
    public OAuthResult kakaoOAuthLogin(String accessCode) {
        // 인가코드로 토근 발급
        KakaoOAuthTokenDTO oAuthToken = kakaoAuthClient.requestToken(accessCode);
        log.info("Kakao OAuth token: {}", oAuthToken);
        // 토큰으로 유저정보 가져오기
        KakaoProfileDTO kakaoProfile = kakaoAuthClient.requestProfile(oAuthToken);
        log.info("Kakao profile: {}", kakaoProfile);

        Provider provider = Provider.KAKAO;

        // 유저정보 ID로 조회 후, 없을 경우 User 생성
        User user = userRepository.findByUserIdAndProvider(kakaoProfile.getId(), provider)
                .orElseGet(() -> createNewUser(kakaoProfile));

        // 4) JWT 생성 + 헤더 추가
        String jwt = jwtService.createAccessToken(user.getUserId(), user.getProvider());
        UserDto dto = UserDto.of(user.getUsername());

        return new OAuthResult(jwt, dto);
    }

    @Override
    public OAuthResult googleOAuthLogin(String code) {
        // 1) 코드→토큰, 2) 토큰→프로필
        GoogleTokenResponse token   = googleAuthClient.requestToken(code);
        GoogleUserInfo profile = googleAuthClient.requestProfile(token.getAccessToken());
        GoogleExtraProfile extra = googleAuthClient.requestExtraProfile(token.getAccessToken());
        Provider provider = Provider.GOOGLE;

        // 3) 외부 ID + Provider 로 사용자 조회
        User user = userRepository.findByUserIdAndProvider(profile.getSub(), provider)
                .orElseGet(() -> createNewUser(profile, extra));

        // 4) JWT 발급 후 헤더 세팅
        String jwt = jwtService.createAccessToken(user.getUserId(), user.getProvider());
        UserDto dto = UserDto.of(user.getUsername());

        return new OAuthResult(jwt, dto);
    }

    private User createNewUser(KakaoProfileDTO kakaoProfile) {
        User newUser = User.builder()
                .userId(kakaoProfile.getId())
                .provider(Provider.KAKAO)
                .username(kakaoProfile.getKakaoAccount().getName())
                .gender(Gender.fromString(kakaoProfile.getKakaoAccount().getGender()))
                .emailPeriod(EmailPeriod.THREE)
                .build();
        newUser.setAge(getEstimatedAge(kakaoProfile.getKakaoAccount().getAgeRange()));

        return userRepository.save(newUser);
    }

    private User createNewUser(GoogleUserInfo profile, GoogleExtraProfile extra) {
        // 1) gender 매핑
        Gender gender = null;
        if (extra.getGenders() != null && !extra.getGenders().isEmpty()) {
            String genderValue = extra.getGenders().get(0).getValue();
            gender = Gender.fromString(genderValue);
        }

        // 2) birthday → age 계산
        int age = -1;
        if (extra.getBirthdays() != null && !extra.getBirthdays().isEmpty()) {
            GoogleExtraProfile.BirthdayWrapper bd = extra.getBirthdays().get(0);
            if (bd.getYear() != null && bd.getMonth() != null && bd.getDay() != null) {
                LocalDate birth = LocalDate.of(bd.getYear(), bd.getMonth(), bd.getDay());
                age = Period.between(birth, LocalDate.now()).getYears();
            }
        }

        // 3) User 엔티티 빌드 및 저장
        return userRepository.save(
                User.builder()
                        .userId(profile.getSub())
                        .provider(Provider.GOOGLE)
                        .username(profile.getName())
                        .gender(gender)           // enum 타입 필드
                        .age(age)                 // 계산된 나이
                        .emailPeriod(EmailPeriod.THREE)
                        .build()
        );
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
