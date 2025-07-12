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
import org.springframework.web.client.RestClient;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppleAuthClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final AppleOAuthConfig appleOAuthConfig;

    private static final String APPLE_KEYS_URL = "https://appleid.apple.com/auth/keys";
    private static final String APPLE_ISSUER = "https://appleid.apple.com";

    /**
     * Apple Identity Token 검증 및 프로필 정보 추출
     */
    public AppleProfileDTO verifyIdentityToken(String identityToken) {
        log.debug("Verifying Apple identity token");

        try {
            // JWT 구조 검증
            validateJwtStructure(identityToken);

            // JWT 헤더에서 kid 추출
            String kid = extractKidFromHeader(identityToken);

            // Apple 공개 키로 JWT 검증 및 Claims 추출
            Claims claims = verifyJwtWithApplePublicKey(identityToken, kid);

            // Claims 검증 (issuer, audience, expiration)
            validateClaims(claims);

            // AppleProfileDTO 생성 및 반환
            AppleProfileDTO profile = createProfileFromClaims(claims);
            log.debug("Verified Apple profile: {}", profile);

            return profile;

        } catch (GlobalException e) {
            throw e; // 이미 처리된 예외는 재던지기
        } catch (Exception e) {
            log.error("Apple identity token 검증 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new GlobalException(ErrorStatus.INVALID_TOKEN);
        }
    }

    /**
     * JWT 구조 검증 (3개 부분으로 구성되어야 함)
     */
    private void validateJwtStructure(String identityToken) {
        String[] chunks = identityToken.split("\\.");
        if (chunks.length != 3) {
            log.error("JWT 구조가 올바르지 않음: {} 개 부분", chunks.length);
            throw new GlobalException(ErrorStatus.INVALID_TOKEN);
        }
    }

    /**
     * JWT 헤더에서 Key ID (kid) 추출
     */
    private String extractKidFromHeader(String identityToken) {
        try {
            String[] chunks = identityToken.split("\\.");
            String header = new String(Base64.getUrlDecoder().decode(chunks[0]));
            JsonNode headerJson = objectMapper.readTree(header);

            JsonNode kidNode = headerJson.get("kid");
            if (kidNode == null) {
                throw new GlobalException(ErrorStatus.INVALID_TOKEN);
            }

            return kidNode.asText();

        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            log.error("JWT 헤더에서 kid 추출 실패: {}", e.getMessage(), e);
            throw new GlobalException(ErrorStatus.INVALID_TOKEN);
        }
    }

    /**
     * Apple 공개 키로 JWT 검증 및 Claims 추출
     */
    private Claims verifyJwtWithApplePublicKey(String identityToken, String kid) {
        try {
            PublicKey publicKey = getApplePublicKey(kid);

            return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(identityToken)
                .getBody();

        } catch (Exception e) {
            log.error("JWT 검증 실패: {}", e.getMessage(), e);
            throw new GlobalException(ErrorStatus.INVALID_TOKEN);
        }
    }

    /**
     * Claims 검증 (issuer, audience, expiration)
     */
    private void validateClaims(Claims claims) {
        // 발급자 검증
        if (!APPLE_ISSUER.equals(claims.getIssuer())) {
            log.error("Invalid issuer. Expected: {}, Got: {}", APPLE_ISSUER, claims.getIssuer());
            throw new GlobalException(ErrorStatus.INVALID_TOKEN);
        }

        // Audience 검증 (Client ID 확인)
        String audience = extractAudienceFromClaims(claims);
        if (!appleOAuthConfig.getClientId().equals(audience)) {
            log.error("Invalid audience. Expected: {}, Got: {}", appleOAuthConfig.getClientId(), audience);
            throw new GlobalException(ErrorStatus.INVALID_TOKEN);
        }

        // 만료 시간 검증
        if (claims.getExpiration().getTime() < System.currentTimeMillis()) {
            log.error("Token expired at: {}", claims.getExpiration());
            throw new GlobalException(ErrorStatus.EXPIRED_TOKEN);
        }
    }

    /**
     * Claims에서 audience 추출 (String 또는 List<String> 처리)
     */
    private String extractAudienceFromClaims(Claims claims) {
        Object audienceObj = claims.get("aud");

        if (audienceObj instanceof String) {
            return (String) audienceObj;
        } else if (audienceObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> audienceList = (List<String>) audienceObj;
            if (!audienceList.isEmpty()) {
                return audienceList.get(0);
            }
        }

        log.error("Invalid audience format: {}", audienceObj);
        throw new GlobalException(ErrorStatus.INVALID_TOKEN);
    }

    /**
     * Claims에서 AppleProfileDTO 생성
     */
    private AppleProfileDTO createProfileFromClaims(Claims claims) {
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
            .name(parseFullNameFromClaims(claims))
            .build();
    }

    /**
     * Claims에서 전체 이름 파싱 (firstName + lastName 조합)
     */
    private String parseFullNameFromClaims(Claims claims) {
        try {
            @SuppressWarnings("unchecked")
            var nameMap = claims.get("name", java.util.Map.class);
            
            if (nameMap == null) {
                return null;
            }
            
            String firstName = (String) nameMap.get("firstName");
            String lastName = (String) nameMap.get("lastName");
            
            StringBuilder fullName = new StringBuilder();
            if (firstName != null && !firstName.trim().isEmpty()) {
                fullName.append(firstName.trim());
            }
            if (lastName != null && !lastName.trim().isEmpty()) {
                if (fullName.length() > 0) {
                    fullName.append(" ");
                }
                fullName.append(lastName.trim());
            }
            
            return fullName.length() > 0 ? fullName.toString() : null;
        } catch (Exception e) {
            log.warn("name claim 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Apple 공개 키 가져오기
     */
    private PublicKey getApplePublicKey(String kid) {
        log.debug("Requesting Apple public key for kid: {}", kid);

        try {
            String response = restClient.get()
                .uri(APPLE_KEYS_URL)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), (request, responseEntity) -> {
                    log.error("Apple 공개 키 요청 클라이언트 오류: 상태 코드 - {}", responseEntity.getStatusCode());
                    throw new GlobalException(ErrorStatus.OAUTH_ERROR);
                })
                .onStatus(status -> status.is5xxServerError(), (request, responseEntity) -> {
                    log.error("Apple 공개 키 요청 서버 오류: 상태 코드 - {}", responseEntity.getStatusCode());
                    throw new GlobalException(ErrorStatus.OAUTH_ERROR);
                })
                .body(String.class);

            if (response == null) {
                throw new GlobalException(ErrorStatus.OAUTH_ERROR);
            }

            return findPublicKeyByKid(response, kid);

        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            log.error("Apple 공개 키 가져오기 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new GlobalException(ErrorStatus.OAUTH_ERROR);
        }
    }

    /**
     * 응답에서 kid로 해당 공개 키 찾기
     */
    private PublicKey findPublicKeyByKid(String response, String kid) {
        try {
            JsonNode keysJson = objectMapper.readTree(response);
            JsonNode keys = keysJson.get("keys");

            for (JsonNode key : keys) {
                if (kid.equals(key.get("kid").asText())) {
                    return createPublicKey(key);
                }
            }

            log.error("해당 kid에 대한 공개 키를 찾을 수 없음: {}", kid);
            throw new GlobalException(ErrorStatus.INVALID_TOKEN);

        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            log.error("공개 키 파싱 실패: {}", e.getMessage(), e);
            throw new GlobalException(ErrorStatus.OAUTH_ERROR);
        }
    }

    /**
     * RSA 공개 키 생성
     */
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
            log.error("RSA 공개 키 생성 실패: {}", e.getMessage(), e);
            throw new GlobalException(ErrorStatus.OAUTH_ERROR);
        }
    }
}