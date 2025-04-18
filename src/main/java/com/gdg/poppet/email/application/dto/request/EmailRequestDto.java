package com.gdg.poppet.email.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class EmailRequestDto {

    @NotBlank(message = "새로운 이메일 주소를 입력해주세요")
    private String newEmail;
}
