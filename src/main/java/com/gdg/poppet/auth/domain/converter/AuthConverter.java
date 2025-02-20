package com.gdg.poppet.auth.domain.converter;

import com.gdg.poppet.user.domain.enums.Gender;
import com.gdg.poppet.user.domain.model.User;

public class AuthConverter {
    public static User toUser(long userId, String username, String gender, int age) {
        return User.builder()
                .userId(userId)
                .username(username)
                .gender(Gender.fromString(gender))
                .age(age)
                .build();
    }
}
