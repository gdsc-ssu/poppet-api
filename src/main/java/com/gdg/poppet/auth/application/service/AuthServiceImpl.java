package com.gdg.poppet.auth.application.service;

import com.gdg.poppet.auth.application.dto.response.GoogleExtraProfileDTO;
import com.gdg.poppet.auth.application.dto.response.GoogleOAuthTokenDTO;
import com.gdg.poppet.auth.application.dto.response.GoogleBasicProfileDTO;
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
        KakaoOAuthTokenDTO oAuthToken = kakaoAuthClient.requestToken(accessCode).block();
        // 토큰으로 유저정보 가져오기
        KakaoProfileDTO kakaoProfile = kakaoAuthClient.requestProfile(oAuthToken).block();
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
    public OAuthResult googleOAuthLogin(String accessCode) {
        // 1) 코드→토큰, 2) 토큰→프로필
        GoogleOAuthTokenDTO token = googleAuthClient.requestToken(accessCode).block();
        GoogleBasicProfileDTO profile = googleAuthClient.requestProfile(token.getAccessToken()).block();
        GoogleExtraProfileDTO extra = googleAuthClient.requestExtraProfile(token.getAccessToken()).block();
        Provider provider = Provider.GOOGLE;

        // 3) 외부 ID + Provider 로 사용자 조회
        User user = userRepository.findByUserIdAndProvider(profile.getSub(), provider)
                .orElseGet(() -> createNewUser(profile, extra));

        // 4) JWT 발급 후 헤더 세팅
        String jwt = jwtService.createAccessToken(user.getUserId(), user.getProvider());
        UserDto dto = UserDto.of(user.getUsername());

        return new OAuthResult(jwt, dto);
    }

    @Override
    public OAuthResult kakaoOAuthLoginWithTokens(String accessToken) {
        // 1) 액세스 토큰으로 프로필 조회 (verifyAccessToken은 앞서 추가한 메서드)
        KakaoProfileDTO profile = kakaoAuthClient.verifyAccessToken(accessToken).block();

        Provider provider = Provider.KAKAO;

        // 2) 사용자 조회/생성 (기존 createNewUser(KakaoProfileDTO) 재사용)
        User user = userRepository
                .findByUserIdAndProvider(profile.getId(), provider)
                .orElseGet(() -> createNewUser(profile));

        // 3) JWT 발급
        String jwt = jwtService.createAccessToken(user.getUserId(), user.getProvider());
        return new OAuthResult(jwt, UserDto.of(user.getUsername()));
    }

    @Override
    public OAuthResult googleOAuthLoginWithTokens(String idToken, String accessToken) {
        // 1) idToken 검증 (서명·만료 검사)
        GoogleBasicProfileDTO basic = googleAuthClient.verifyIdToken(idToken).block();
        // 2) accessToken 으로 프로필·추가정보 조회
        // GoogleExtraProfileDTO extra = googleAuthClient.requestExtraProfile(accessToken).block();
        Provider provider = Provider.GOOGLE;

        // 3) 사용자 조회/생성
        User user = userRepository
                .findByUserIdAndProvider(basic.getSub(), provider)
                .orElseGet(() -> createNewUser(basic, null)); //임시 : 성별, 연령정보 70,  FEMALE로 입력

        // 4) 자체 JWT 발급
        String jwt = jwtService.createAccessToken(user.getUserId(), user.getProvider());
        return new OAuthResult(jwt, UserDto.of(user.getUsername()));
    }

    private User createNewUser(KakaoProfileDTO kakaoProfile) {
        Gender gender = null;
        if (kakaoProfile.getKakaoAccount().getGender() != null && !kakaoProfile.getKakaoAccount().getGender()
                .isEmpty()) {
            String genderValue = kakaoProfile.getKakaoAccount().getGender();
            gender = Gender.fromString(genderValue);
        } else {
            gender = Gender.MALE;
        }

        int estimatedAge = getEstimatedAge(kakaoProfile.getKakaoAccount().getAgeRange());

        return userRepository.save(
                User.builder()
                        .userId(kakaoProfile.getId())
                        .provider(Provider.KAKAO)
                        .username(kakaoProfile.getKakaoAccount().getName())
                        .gender(gender)
                        .emailPeriod(EmailPeriod.THREE)
                        .age(estimatedAge)
                        .build());
    }

    private User createNewUser(GoogleBasicProfileDTO profile, GoogleExtraProfileDTO extra) {
        Gender gender = null;
        if (extra.getGenders() != null && !extra.getGenders().isEmpty()) {
            String genderValue = extra.getGenders().get(0).getValue();
            gender = Gender.fromString(genderValue);
        } else {
            gender = Gender.FEMALE;
        }

        // 2) birthday → age 계산
        int age = 70;
        if (!extra.getBirthdays().isEmpty()) {
            GoogleExtraProfileDTO.DateWrapper d = extra.getBirthdays().get(0).getDate();
            if (d.getYear() != null) {
                age = Period.between(
                        LocalDate.of(d.getYear(), d.getMonth(), d.getDay()),
                        LocalDate.now()
                ).getYears();
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
