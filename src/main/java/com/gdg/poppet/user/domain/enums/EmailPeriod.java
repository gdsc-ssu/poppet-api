package com.gdg.poppet.user.domain.enums;

import com.gdg.poppet.global.exception.GlobalException;
import com.gdg.poppet.global.status.ErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmailPeriod {
    ONE(1), THREE(3), SEVEN(7);

    private final int value;

    public static EmailPeriod fromValue(int value) {
        for (EmailPeriod emailPeriod : EmailPeriod.values()) {
            if (emailPeriod.getValue() == value) {
                return emailPeriod;
            }
        }
        throw new GlobalException(ErrorStatus.EMAIL_PERIOD_INVALID);
    }
}
