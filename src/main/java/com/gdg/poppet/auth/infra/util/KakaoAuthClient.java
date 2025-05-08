package com.gdg.poppet.auth.infra.util;

import com.gdg.poppet.auth.application.dto.response.KakaoOAuthTokenDTO;
import com.gdg.poppet.auth.application.dto.response.KakaoProfileDTO;
import com.gdg.poppet.global.exception.GlobalException;
import com.gdg.poppet.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoAuthClient {

    private final WebClient webClient;

    @Value("${kakao.oauth2.client}")
    private String clientId;

    @Value("${kakao.oauth2.redirect}")
    private String redirectUri;

    @Value("${kakao.oauth2.token-uri}")
    private String tokenUri;

    @Value("${kakao.oauth2.user-info-uri}")
    private String userInfoUri;

    /**
     * 1) authorization code → Access Token 교환
     */
    public Mono<KakaoOAuthTokenDTO> requestToken(String accessCode) {
        return webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("client_id", clientId)
                        .with("redirect_uri", redirectUri)
                        .with("code", accessCode))
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        resp -> Mono.error(new GlobalException(ErrorStatus.OAUTH_ERROR))
                )
                .bodyToMono(KakaoOAuthTokenDTO.class)
                .doOnNext(token -> log.info("Kakao OAuth token: {}", token.getAccess_token()));
    }

    /**
     * 2) Access Token → Kakao UserInfo 조회
     */
    public Mono<KakaoProfileDTO> requestProfile(KakaoOAuthTokenDTO tokenDto) {
        return webClient.get()
                .uri(userInfoUri)
                .headers(h -> {
                    h.setBearerAuth(tokenDto.getAccess_token());
                    h.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                })
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        resp -> Mono.error(new GlobalException(ErrorStatus.PROFILE_ERROR))
                )
                .bodyToMono(KakaoProfileDTO.class)
                .doOnNext(profile -> log.info("Kakao profile: {}", profile));
    }
}
