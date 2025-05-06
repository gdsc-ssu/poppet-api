package com.gdg.poppet.auth.application.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.gdg.poppet.user.domain.enums.Provider;
import com.gdg.poppet.user.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Getter
@Slf4j
public class JwtService {

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private Long accessTokenExpirationPeriod;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpirationPeriod;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    /**
     * JWT의 Subject와 Claim으로 userName 사용 -> 클레임의 name을 "userId"으로 설정 JWT의 헤더에 들어오는 값 : 'Authorization(Key) = Bearer {토큰}
     * (Value)' 형식
     */
    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String USER_ID_CLAIM   = "userId";
    private static final String PROVIDER_CLAIM  = "provider";
    private static final String BEARER = "Bearer ";

    private final UserRepository userRepository;

    /**
     * AccessToken 생성 메소드
     */
    public String createAccessToken(String userId, Provider provider) {
        Date now = new Date();
        return JWT.create() // JWT 토큰을 생성하는 빌더 반환
                .withSubject(ACCESS_TOKEN_SUBJECT) // JWT의 Subject 지정 -> AccessToken이므로 AccessToken
                .withExpiresAt(new Date(now.getTime() + accessTokenExpirationPeriod)) // 토큰 만료 시간 설정
                .withClaim(USER_ID_CLAIM, userId)
                .withClaim(PROVIDER_CLAIM, provider.name())
                .sign(Algorithm.HMAC512(secretKey)); // HMAC512 알고리즘 사용, application.yml에서 지정한 secret 키로 암호화
    }

    /**
     * RefreshToken 생성 RefreshToken은 Claim에 email도 넣지 않으므로 withClaim() X
     */
    public String createRefreshToken() {
        Date now = new Date();
        return JWT.create()
                .withSubject(REFRESH_TOKEN_SUBJECT)
                .withExpiresAt(new Date(now.getTime() + refreshTokenExpirationPeriod))
                .sign(Algorithm.HMAC512(secretKey));
    }

    /**
     * AccessToken 헤더에 실어서 보내기
     */
    public void sendAccessToken(HttpServletResponse response, String accessToken) {
        response.setStatus(HttpServletResponse.SC_OK);

        response.setHeader(accessHeader, accessToken);
        log.info("재발급된 Access Token : {}", accessToken);
    }

    /**
     * AccessToken + RefreshToken 헤더에 실어서 보내기
     */
    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenHeader(response, accessToken);
        setRefreshTokenHeader(response, refreshToken);
        log.info("Access Token, Refresh Token 헤더 설정 완료");
    }

    /**
     * 헤더에서 RefreshToken 추출 토큰 형식 : Bearer XXX에서 Bearer를 제외하고 순수 토큰만 가져오기 위해서 헤더를 가져온 후 "Bearer"를 삭제(""로 replace)
     */
    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    /**
     * 헤더에서 AccessToken 추출 토큰 형식 : Bearer XXX에서 Bearer를 제외하고 순수 토큰만 가져오기 위해서 헤더를 가져온 후 "Bearer"를 삭제(""로 replace)
     */
    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    /**
     * AccessToken 헤더 설정
     */
    public void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
        response.setHeader(accessHeader, accessToken);
    }

    /**
     * RefreshToken 헤더 설정
     */
    public void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
        response.setHeader(refreshHeader, refreshToken);
    }

    /**
     * RefreshToken DB 저장(업데이트)
     */
    public void updateRefreshToken(String userId, Provider provider, String refreshToken) {
        userRepository.findByUserIdAndProvider(userId, provider)
                .ifPresentOrElse(
                        user -> user.updateRefreshToken(refreshToken),
                        () -> { throw new RuntimeException("일치하는 회원이 없습니다."); }
                );
    }

    public boolean isTokenValid(String token) {
        try {
            JWT.require(Algorithm.HMAC512(secretKey)).build().verify(token);
            return true;
        } catch (Exception e) {
            log.error("유효하지 않은 토큰입니다. {}", e.getMessage());
            return false;
        }
    }
    public Optional<String> extractUserId(String token) {
        try {
            return Optional.ofNullable(
                    JWT.require(Algorithm.HMAC512(secretKey))
                            .build()
                            .verify(token)
                            .getClaim(USER_ID_CLAIM)
                            .asString()
            );
        } catch (Exception e) {
            log.error("유효하지 않은 토큰입니다. {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Provider> extractProvider(String token) {
        try {
            String prov = JWT.require(Algorithm.HMAC512(secretKey))
                    .build()
                    .verify(token)
                    .getClaim(PROVIDER_CLAIM)
                    .asString();
            return Optional.of(Provider.valueOf(prov));
        } catch (Exception e) {
            log.error("프로바이더 추출 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

}
