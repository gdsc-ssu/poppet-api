package com.gdg.poppet.global.exception;

import com.gdg.poppet.global.base.BaseErrorStatus;
import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {
    protected final BaseErrorStatus errorStatus;

    public GlobalException(BaseErrorStatus errorStatus) {
        super(errorStatus.getMessage());
        this.errorStatus = errorStatus;
    }
}
