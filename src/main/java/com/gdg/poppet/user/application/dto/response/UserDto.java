package com.gdg.poppet.user.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserDto {
    private String name;

    public static UserDto of(String name) {
        return UserDto.builder().name(name).build();
    }
}
