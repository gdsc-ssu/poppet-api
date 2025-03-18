package com.gdg.poppet.user.application;

import com.gdg.poppet.user.application.dto.response.EmailPeriodDto;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    EmailPeriodDto getEmailPeriod(String name);
    EmailPeriodDto patchEmailPeriod(String name, int period);
}
