package com.gdg.poppet.user.domain.converter;

import com.gdg.poppet.user.application.dto.response.EmailPeriodDto;

public class UserConverter {
    public static EmailPeriodDto toEmailPeriodDto(int period) {
        return EmailPeriodDto.builder()
                .period(period)
                .build();
    }
}
