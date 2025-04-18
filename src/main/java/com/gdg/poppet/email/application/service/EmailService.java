package com.gdg.poppet.email.application.service;

import com.gdg.poppet.email.application.dto.request.EmailRequestDto;
import com.gdg.poppet.email.application.dto.response.EmailDto;
import com.gdg.poppet.email.application.dto.response.EmailPeriodDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EmailService {
    EmailPeriodDto getEmailPeriod(String name);
    EmailPeriodDto patchEmailPeriod(String name, int period);

    List<EmailDto> getEmailAddressList(String name);
    List<EmailDto> postEmailAddress(String name, EmailRequestDto emailRequestDto);
    void patchEmailAddress(String name, Long emailId, EmailRequestDto emailRequestDto);
    void deleteEmailAddress(String name, Long emailId);

    void sendEmail(String username);
}
