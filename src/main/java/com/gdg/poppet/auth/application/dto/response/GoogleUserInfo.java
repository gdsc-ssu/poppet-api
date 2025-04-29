package com.gdg.poppet.auth.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GoogleUserInfo {

    /** Google 고유 식별자 */
    private String sub;

    /** 사용자 전체 이름 */
    private String name;

    /** 사용자 Given name */
    @JsonProperty("given_name")
    private String givenName;

    /** 사용자 Family name */
    @JsonProperty("family_name")
    private String familyName;

    /** 프로필 사진 URL */
    private String picture;

    /** 이메일 주소 */
    private String email;

    /** 이메일 검증 여부 */
    @JsonProperty("email_verified")
    private Boolean emailVerified;

    /** 로케일 정보 (예: "en", "ko") */
    private String locale;
}
