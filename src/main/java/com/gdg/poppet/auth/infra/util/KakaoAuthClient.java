package com.gdg.poppet.auth.infra.util;

import com.gdg.poppet.auth.application.dto.response.KakaoProfileDTO;
import com.gdg.poppet.auth.infra.config.KakaoOAuthConfig;
import com.gdg.poppet.global.exception.GlobalException;
import com.gdg.poppet.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoAuthClient {

    private final WebClient webClient;
    private final KakaoOAuthConfig kakaoOAuthConfig;

    /**
     * 모바일용 Access Token 검증 및 프로필 조회
     */
    public Mono<KakaoProfileDTO> verifyAccessToken(String accessToken) {
        log.debug("Verifying Kakao access token");
        return webClient.get()
                .uri(kakaoOAuthConfig.getUserInfoUri())
                .headers(h -> {
                    h.set("Authorization", "Bearer " + accessToken);
                    h.set("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse -> {
                    log.error("카카오 프로필 조회 실패 - 상태 코드: {}, URI: {}",
                             clientResponse.statusCode(), kakaoOAuthConfig.getUserInfoUri());
                    return clientResponse.bodyToMono(String.class)
                            .doOnNext(errorBody -> log.error("카카오 API 에러 응답: {}", errorBody))
                            .then(Mono.error(new GlobalException(ErrorStatus.PROFILE_ERROR)));
                })
                .bodyToMono(KakaoProfileDTO.class)
                .doOnNext(profile -> log.debug("Verified Kakao profile: {}", profile));
    }

    /**
     * authorization code → Access Token 교환 후 바로 프로필 조회
     */
    public Mono<KakaoProfileDTO> requestTokenAndProfile(String accessCode) {
        return webClient.post()
                .uri(kakaoOAuthConfig.getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("client_id", kakaoOAuthConfig.getClient())
                        .with("redirect_uri", kakaoOAuthConfig.getRedirect())
                        .with("code", accessCode))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    log.error("클라이언트 오류 발생: 상태 코드 - {}", clientResponse.statusCode());
                    return clientResponse.bodyToMono(String.class)
                            .map(errorBody -> new GlobalException(ErrorStatus.OAUTH_ERROR));
                })
                .bodyToMono(Map.class)
                .doOnNext(tokenResponse -> log.info("Kakao OAuth token received"))
                .flatMap(this::requestProfileWithToken);
    }

    /**
     * Access Token으로 Kakao UserInfo 조회 (내부 메서드)
     */
    private Mono<KakaoProfileDTO> requestProfileWithToken(Map<String, Object> tokenResponse) {
        String accessToken = (String) tokenResponse.get("access_token");
        if (accessToken == null) {
            return Mono.error(new GlobalException(ErrorStatus.OAUTH_TOKEN_ERROR));
        }

        log.debug("Requesting Kakao profile with token");
        return webClient.get()
                .uri(kakaoOAuthConfig.getUserInfoUri())
                .headers(h -> {
                    h.set("Authorization", "Bearer " + accessToken);
                    h.set("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse -> {
                    log.error("카카오 프로필 조회 실패 - 상태 코드: {}", clientResponse.statusCode());
                    return clientResponse.bodyToMono(String.class)
                            .doOnNext(errorBody -> log.error("카카오 API 에러 응답: {}", errorBody))
                            .then(Mono.error(new GlobalException(ErrorStatus.PROFILE_ERROR)));
                })
                .bodyToMono(KakaoProfileDTO.class)
                .doOnNext(profile -> log.info("Kakao profile: {}", profile));
    }
}
