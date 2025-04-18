package com.gdg.poppet.email.domain.converter;

import com.gdg.poppet.email.application.dto.response.EmailDto;
import com.gdg.poppet.email.application.dto.response.EmailPeriodDto;
import com.gdg.poppet.email.domain.model.Email;
import com.gdg.poppet.user.domain.model.User;

public class EmailConverter {

    public static Email toEmail(String emailAddr, User user) {
        return Email.builder()
                .emailAddress(emailAddr)
                .user(user)
                .build();
    }

    public static EmailPeriodDto toEmailPeriodDto(int period) {
        return EmailPeriodDto.builder()
                .period(period)
                .build();
    }

    public static EmailDto toEmailDto(Email email) {
        return EmailDto.builder()
                .emailId(email.getEmailId())
                .emailAddress(email.getEmailAddress())
                .build();
    }
}