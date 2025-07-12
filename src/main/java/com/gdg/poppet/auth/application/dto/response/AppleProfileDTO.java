package com.gdg.poppet.auth.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppleProfileDTO {
    @JsonProperty("sub")
    private String sub; // 사용자 고유 식별자

    @JsonProperty("email")
    private String email; // 이메일 (첫 로그인시에만 제공)

    @JsonProperty("email_verified")
    private Boolean emailVerified;

    @JsonProperty("is_private_email")
    private Boolean isPrivateEmail;

    @JsonProperty("real_user_status")
    private Integer realUserStatus;

    @JsonProperty("aud")
    private String aud; // 앱 ID

    @JsonProperty("iss")
    private String iss; // 발급자 (https://appleid.apple.com)

    @JsonProperty("iat")
    private Long iat; // 발급 시간

    @JsonProperty("exp")
    private Long exp; // 만료 시간

    @JsonProperty("auth_time")
    private Long authTime; // 인증 시간

    @JsonProperty("nonce_supported")
    private Boolean nonceSupported;

    @JsonProperty("name")
    private String name; // 사용자 전체 이름 (firstName + lastName 조합)

    // 사용자 식별을 위한 ID 반환
    public String getId() {
        return this.sub;
    }

    // 이메일이 있을 때만 반환
    public String getValidEmail() {
        return (email != null && !email.trim().isEmpty()) ? email : null;
    }

    // 이름이 있을 때만 반환
    public String getValidName() {
        return (name != null && !name.trim().isEmpty()) ? name.trim() : null;
    }
}
