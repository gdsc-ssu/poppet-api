package com.gdg.poppet.auth.domain.converter;

import com.gdg.poppet.auth.application.dto.response.KakaoProfileDTO;
import com.gdg.poppet.email.domain.enums.EmailPeriod;
import com.gdg.poppet.user.domain.enums.Gender;
import com.gdg.poppet.user.domain.model.User;

public class AuthConverter {
    public static User toUser(KakaoProfileDTO kakaoProfile) {
        String username = kakaoProfile.getKakaoAccount().getName();
        if (username == null) {
            username = kakaoProfile.getKakaoAccount().getProfile().getNickname();
        }

        String gender = kakaoProfile.getKakaoAccount().getGender();
        if (gender == null) {
            gender = Gender.MALE.toString();
        }

        return User.builder()
                .userId(kakaoProfile.getId())
                .username(username)
                .gender(Gender.fromString(gender))
                .emailPeriod(EmailPeriod.THREE)
                .build();
    }
}
