package com.gdg.poppet.auth.infra.util;

import com.gdg.poppet.auth.application.dto.response.GoogleExtraProfile;
import com.gdg.poppet.auth.application.dto.response.GoogleTokenResponse;
import com.gdg.poppet.auth.application.dto.response.GoogleUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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

    /** 1) authorization code → Access Token 교환 */
    public GoogleTokenResponse requestToken(String code) {
        Mono<GoogleTokenResponse> mono = webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("client_id",     clientId)
                        .with("client_secret", clientSecret)
                        .with("redirect_uri",  redirectUri)
                        .with("code",          code))
                .retrieve()
                .bodyToMono(GoogleTokenResponse.class);

        GoogleTokenResponse token = mono.block();
        log.info("Google OAuth token: {}", token.getAccessToken());
        return token;
    }

    /** 2) Access Token → Google UserInfo 조회 */
    public GoogleUserInfo requestProfile(String accessToken) {
        Mono<GoogleUserInfo> mono = webClient.get()
                .uri(userInfoUri)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(GoogleUserInfo.class);

        GoogleUserInfo profile = mono.block();
        log.debug("Google profile: {}", profile);
        return profile;
    }

    /** 3) People API 로 Gender, Birthday 조회 */
    public GoogleExtraProfile requestExtraProfile(String accessToken) {
        Mono<GoogleExtraProfile> mono = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("people.googleapis.com")
                        .path("/v1/people/me")
                        .queryParam("personFields", "genders,birthdays")
                        .build()
                )
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(GoogleExtraProfile.class);

        GoogleExtraProfile extra = mono.block();
        log.debug("Google extra: {}", extra);
        return extra;
    }
}

