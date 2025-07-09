package com.gdg.poppet.auth.infra.util;

import com.gdg.poppet.auth.application.dto.response.KakaoProfileDTO;
import com.gdg.poppet.auth.infra.config.KakaoOAuthConfig;
import com.gdg.poppet.global.exception.GlobalException;
import com.gdg.poppet.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoAuthClient {

    private final RestClient restClient;
    private final KakaoOAuthConfig kakaoOAuthConfig;

    /**
     * 모바일용 Access Token 검증 및 프로필 조회
     */
    public KakaoProfileDTO verifyAccessToken(String accessToken) {
        log.debug("Verifying Kakao access token");
        return requestProfile(accessToken);
    }

    /**
     * authorization code → Access Token 교환 후 바로 프로필 조회
     */
    public KakaoProfileDTO requestTokenAndProfile(String accessCode) {
        try {
            // 토큰 요청을 위한 파라미터 설정
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", kakaoOAuthConfig.getClient());
            params.add("redirect_uri", kakaoOAuthConfig.getRedirect());
            params.add("code", accessCode);

            // 토큰 요청
            Map<String, Object> tokenResponse = restClient.post()
                .uri(kakaoOAuthConfig.getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), (request, response) -> {
                    log.error("카카오 토큰 요청 클라이언트 오류: 상태 코드 - {}", response.getStatusCode());
                    if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        throw new GlobalException(ErrorStatus.OAUTH_TOKEN_ERROR);
                    }
                    throw new GlobalException(ErrorStatus.OAUTH_ERROR);
                })
                .onStatus(status -> status.is5xxServerError(), (request, response) -> {
                    log.error("카카오 토큰 요청 서버 오류: 상태 코드 - {}", response.getStatusCode());
                    throw new GlobalException(ErrorStatus.OAUTH_ERROR);
                })
                .body(Map.class);

            log.info("Kakao OAuth token received");

            // 토큰으로 프로필 조회
            String accessToken = (String) tokenResponse.get("access_token");
            if (accessToken == null) {
                throw new GlobalException(ErrorStatus.OAUTH_TOKEN_ERROR);
            }

            return requestProfile(accessToken);

        } catch (GlobalException e) {
            throw e; // 이미 처리된 예외는 재던지기
        } catch (Exception e) {
            log.error("카카오 토큰 요청 및 프로필 조회 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new GlobalException(ErrorStatus.OAUTH_ERROR);
        }
    }

    /**
     * Access Token으로 Kakao UserInfo 조회 (공통 메서드)
     */
    private KakaoProfileDTO requestProfile(String accessToken) {
        log.debug("Requesting Kakao profile with token");

        try {
            KakaoProfileDTO profile = restClient.get()
                .uri(kakaoOAuthConfig.getUserInfoUri())
                .header("Authorization", "Bearer " + accessToken)
                // GET 요청에서는 Content-Type 헤더 불필요하므로 제거
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), (request, response) -> {
                    log.error("카카오 프로필 조회 클라이언트 오류: 상태 코드 - {}, URI: {}",
                        response.getStatusCode(), kakaoOAuthConfig.getUserInfoUri());

                    if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        throw new GlobalException(ErrorStatus.INVALID_TOKEN);
                    } else if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
                        throw new GlobalException(ErrorStatus.FORBIDDEN);
                    }
                    throw new GlobalException(ErrorStatus.PROFILE_ERROR);
                })
                .onStatus(status -> status.is5xxServerError(), (request, response) -> {
                    log.error("카카오 프로필 조회 서버 오류: 상태 코드 - {}", response.getStatusCode());
                    throw new GlobalException(ErrorStatus.PROFILE_ERROR);
                })
                .body(KakaoProfileDTO.class);

            log.debug("Retrieved Kakao profile: {}", profile);
            return profile;

        } catch (GlobalException e) {
            throw e; // 이미 처리된 예외는 재던지기
        } catch (Exception e) {
            log.error("카카오 프로필 조회 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new GlobalException(ErrorStatus.PROFILE_ERROR);
        }
    }
}