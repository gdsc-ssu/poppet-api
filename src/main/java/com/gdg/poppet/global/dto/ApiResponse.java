package com.gdg.poppet.global.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.gdg.poppet.global.base.BaseErrorStatus;
import com.gdg.poppet.global.base.BaseSuccessStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

@Getter
@RequiredArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "data"})
public class ApiResponse<T> {
    @JsonProperty("is_success")
    private final Boolean isSuccess;  // 성공 여부
    private final Integer code;        // 사용자 정의 코드
    private final String message;     // 응답 메시지
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;             // 응답 데이터

    // 성공 응답 (데이터 없음)
    public static <T> ResponseEntity<ApiResponse<T>> success(BaseSuccessStatus successStatus) {
        return ResponseEntity.status(successStatus.getHttpStatus())
                .body(new ApiResponse<>(true, successStatus.getCode(), successStatus.getMessage(), null));
    }

    // 성공 응답 (데이터 있음)
    public static <T> ResponseEntity<ApiResponse<T>> success(BaseSuccessStatus successStatus, T data) {
        return ResponseEntity.status(successStatus.getHttpStatus())
                .body(new ApiResponse<>(true, successStatus.getCode(), successStatus.getMessage(), data));
    }

    // 에러 응답 (데이터 없음)
    public static <T> ResponseEntity<ApiResponse<T>> error(BaseErrorStatus errorStatus) {
        return ResponseEntity.status(errorStatus.getHttpStatus())
                .body(new ApiResponse<>(false, errorStatus.getCode(), errorStatus.getMessage(), null));
    }

    // 에러 응답 (데이터 있음)
    public static <T> ResponseEntity<ApiResponse<T>> error(BaseErrorStatus errorStatus, T data) {
        return ResponseEntity.status(errorStatus.getHttpStatus())
                .body(new ApiResponse<>(false, errorStatus.getCode(), errorStatus.getMessage(), data));
    }

    // 에러 응답 (오버라이드 메서드용)
    public static ResponseEntity<Object> error(BaseErrorStatus errorStatus, String message) {
        return ResponseEntity.status(errorStatus.getHttpStatus())
                .body(new ApiResponse<>(false, errorStatus.getCode(), message, null));
    }
}
