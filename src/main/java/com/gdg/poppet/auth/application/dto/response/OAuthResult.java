package com.gdg.poppet.auth.application.dto.response;

import com.gdg.poppet.user.application.dto.response.UserDto;

public record OAuthResult(String accessToken, UserDto userDto) {
}
