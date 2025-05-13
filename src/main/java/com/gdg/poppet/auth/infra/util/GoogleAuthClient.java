package com.gdg.poppet.auth.infra.util;

import com.gdg.poppet.auth.application.dto.response.GoogleBasicProfileDTO;
import com.gdg.poppet.auth.application.dto.response.GoogleExtraProfileDTO;
import com.gdg.poppet.auth.application.dto.response.GoogleOAuthTokenDTO;
import com.gdg.poppet.global.exception.GlobalException;
import com.gdg.poppet.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Google OAuth2 API 호출 클라이언트
 * - 논블로킹 방식(Mono)으로 반환하도록 수정
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthClient {

    private final WebClient webClient;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String userInfoUri;

    @Value("${google.oauth2.people-api.base-uri}")
    private String peopleApiBaseUri;

    @Value("${google.oauth2.people-api.person-fields}")
    private String personFields;

    /**
     * 0) ID Token 검증 및 기본 프로필 반환
     */
    public Mono<GoogleBasicProfileDTO> verifyIdToken(String idToken) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("oauth2.googleapis.com")
                        .path("/tokeninfo")
                        .queryParam("id_token", idToken)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        resp -> Mono.error(new GlobalException(ErrorStatus.OAUTH_ERROR)))
                .bodyToMono(GoogleBasicProfileDTO.class)
                .doOnNext(profile -> log.debug("Verified ID token for: {}", profile.getEmail()));
    }

    /**
     * 1) authorization code → Access Token 교환
     */
    public Mono<GoogleOAuthTokenDTO> requestToken(String code) {
        return webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("client_id",     clientId)
                        .with("client_secret", clientSecret)
                        .with("redirect_uri",  redirectUri)
                        .with("code",          code))
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        resp -> Mono.error(new GlobalException(ErrorStatus.OAUTH_ERROR)))
                .bodyToMono(GoogleOAuthTokenDTO.class)
                .doOnNext(tok -> log.trace("Received Google OAuth token: {}", tok.getAccessToken()))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(500)));
    }

    /**
     * 2) Access Token → Google UserInfo 조회
     */
    public Mono<GoogleBasicProfileDTO> requestProfile(String accessToken) {
        return webClient.get()
                .uri(userInfoUri)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        resp -> Mono.error(new GlobalException(ErrorStatus.PROFILE_ERROR)))
                .bodyToMono(GoogleBasicProfileDTO.class)
                .doOnNext(profile -> log.debug("Google profile: {}", profile.getEmail()));
    }

    /**
     * 3) People API 로 Gender, Birthday 조회
     */
    public Mono<GoogleExtraProfileDTO> requestExtraProfile(String accessToken) {
        return webClient.get()
                .uri(peopleApiBaseUri + "?personFields=" + personFields)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        resp -> Mono.error(new GlobalException(ErrorStatus.PROFILE_ERROR)))
                .bodyToMono(GoogleExtraProfileDTO.class)
                .doOnNext(extra -> log.debug("Google extra: {}", extra));
    }
}
