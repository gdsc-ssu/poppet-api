package com.gdg.poppet.global.status;

import com.gdg.poppet.global.base.BaseSuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseSuccessStatus {

    OK(HttpStatus.OK, 200, "요청이 성공적으로 처리되었습니다."),
    CHAT_SUCCESS(HttpStatus.OK, 200, "AI와의 대화가 성공적으로 완료되었습니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;
}
