package com.gdg.poppet.user.application.service;

import com.gdg.poppet.user.application.dto.response.EmailDto;
import com.gdg.poppet.user.application.dto.response.EmailPeriodDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EmailService {
    EmailPeriodDto getEmailPeriod(String name);
    EmailPeriodDto patchEmailPeriod(String name, int period);
    List<EmailDto> getEmailList(String name);
    void postEmail(String name, String email);
}
