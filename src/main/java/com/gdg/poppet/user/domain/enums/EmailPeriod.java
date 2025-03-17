package com.gdg.poppet.user.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmailPeriod {
    ONE(1), THREE(3), SEVEN(7);

    private final int value;
}
