package com.gdg.poppet.auth.infra.util;

import com.gdg.poppet.auth.application.dto.response.KakaoOAuthTokenDTO;
import com.gdg.poppet.auth.application.dto.response.KakaoProfileDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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

    @Value("${kakao.auth.client}")
    private String client;
    @Value("${kakao.auth.redirect}")
    private String redirect;

    public KakaoOAuthTokenDTO requestToken(String accessCode) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", client);
        formData.add("redirect_uri", redirect);
        formData.add("code", accessCode);

        // POST /oauth/token
        Mono<KakaoOAuthTokenDTO> dtoMono = webClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(KakaoOAuthTokenDTO.class);

        KakaoOAuthTokenDTO tokenDto = dtoMono.block();
        log.info("oAuthToken : {}", tokenDto.getAccess_token());
        return tokenDto;

    }

    public KakaoProfileDTO requestProfile(KakaoOAuthTokenDTO oAuthToken) {
        // GET /v2/user/me
        Mono<KakaoProfileDTO> profileMono = webClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + oAuthToken.getAccess_token())
                .retrieve()
                .bodyToMono(KakaoProfileDTO.class);

        KakaoProfileDTO profile = profileMono.block();
        log.info("KakaoProfile: {}", profile);
        return profile;
    }
}
