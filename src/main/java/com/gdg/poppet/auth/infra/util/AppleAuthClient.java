package com.gdg.poppet.auth.infra.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.poppet.auth.application.dto.response.AppleProfileDTO;
import com.gdg.poppet.auth.infra.config.AppleOAuthConfig;
import com.gdg.poppet.global.exception.GlobalException;
import com.gdg.poppet.global.status.ErrorStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppleAuthClient {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final AppleOAuthConfig appleOAuthConfig;

    private static final String APPLE_KEYS_URL = "https://appleid.apple.com/auth/keys";
    private static final String APPLE_ISSUER = "https://appleid.apple.com";

    public Mono<AppleProfileDTO> verifyIdentityToken(String identityToken) {
        return Mono.fromCallable(() -> {
            try {
                // JWT 헤더에서 kid 추출
                String[] chunks = identityToken.split("\\.");
                if (chunks.length != 3) {
                    throw new GlobalException(ErrorStatus.INVALID_TOKEN);
                }

                String header = new String(Base64.getUrlDecoder().decode(chunks[0]));
                JsonNode headerJson = objectMapper.readTree(header);
                String kid = headerJson.get("kid").asText();

                // Apple 공개 키 가져오기
                PublicKey publicKey = getApplePublicKey(kid);

                // JWT 검증 및 파싱
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(publicKey)
                        .build()
                        .parseClaimsJws(identityToken)
                        .getBody();

                // 발급자 검증
                if (!APPLE_ISSUER.equals(claims.getIssuer())) {
                    throw new GlobalException(ErrorStatus.INVALID_TOKEN);
                }

                // Audience 검증 (Client ID 확인)
                Object audienceObj = claims.get("aud");
                String audience = null;

                if (audienceObj instanceof String) {
                    audience = (String) audienceObj;
                } else if (audienceObj instanceof java.util.List) {
                    @SuppressWarnings("unchecked")
                    java.util.List<String> audienceList = (java.util.List<String>) audienceObj;
                    if (!audienceList.isEmpty()) {
                        audience = audienceList.get(0);
                    }
                }

                if (audience == null || !appleOAuthConfig.getClientId().equals(audience)) {
                    log.error("Invalid audience. Expected: {}, Got: {}", appleOAuthConfig.getClientId(), audience);
                    throw new GlobalException(ErrorStatus.INVALID_TOKEN);
                }

                // 만료 시간 검증
                if (claims.getExpiration().getTime() < System.currentTimeMillis()) {
                    throw new GlobalException(ErrorStatus.EXPIRED_TOKEN);
                }

                // AppleProfileDTO 생성
                return AppleProfileDTO.builder()
                        .sub(claims.getSubject())
                        .email(claims.get("email", String.class))
                        .emailVerified(claims.get("email_verified", Boolean.class))
                        .isPrivateEmail(claims.get("is_private_email", Boolean.class))
                        .realUserStatus(claims.get("real_user_status", Integer.class))
                        .aud(claims.getAudience())
                        .iss(claims.getIssuer())
                        .iat(claims.getIssuedAt().getTime())
                        .exp(claims.getExpiration().getTime())
                        .authTime(claims.get("auth_time", Long.class))
                        .nonceSupported(claims.get("nonce_supported", Boolean.class))
                        .build();

            } catch (Exception e) {
                log.error("Apple identity token 검증 실패: {}", e.getMessage(), e);
                throw new GlobalException(ErrorStatus.INVALID_TOKEN);
            }
        });
    }

    private PublicKey getApplePublicKey(String kid) {
        try {
            // Apple 공개 키 요청
            String response = webClient.get()
                    .uri(APPLE_KEYS_URL)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null) {
                throw new GlobalException(ErrorStatus.OAUTH_ERROR);
            }

            JsonNode keysJson = objectMapper.readTree(response);
            JsonNode keys = keysJson.get("keys");

            // kid로 해당 키 찾기
            for (JsonNode key : keys) {
                if (kid.equals(key.get("kid").asText())) {
                    return createPublicKey(key);
                }
            }

            throw new GlobalException(ErrorStatus.INVALID_TOKEN);

        } catch (Exception e) {
            log.error("Apple 공개 키 가져오기 실패: {}", e.getMessage(), e);
            throw new GlobalException(ErrorStatus.OAUTH_ERROR);
        }
    }

    private PublicKey createPublicKey(JsonNode keyNode) {
        try {
            String nStr = keyNode.get("n").asText();
            String eStr = keyNode.get("e").asText();

            byte[] nBytes = Base64.getUrlDecoder().decode(nStr);
            byte[] eBytes = Base64.getUrlDecoder().decode(eStr);

            BigInteger n = new BigInteger(1, nBytes);
            BigInteger e = new BigInteger(1, eBytes);

            RSAPublicKeySpec spec = new RSAPublicKeySpec(n, e);
            KeyFactory factory = KeyFactory.getInstance("RSA");

            return factory.generatePublic(spec);

        } catch (Exception e) {
            log.error("공개 키 생성 실패: {}", e.getMessage(), e);
            throw new GlobalException(ErrorStatus.OAUTH_ERROR);
        }
    }
}
