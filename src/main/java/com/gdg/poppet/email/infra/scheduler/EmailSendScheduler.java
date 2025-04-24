package com.gdg.poppet.email.infra.scheduler;

import com.gdg.poppet.email.application.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSendScheduler {
    private final EmailService emailService;

    // 매일 12시에 스케쥴러 수행
    @Scheduled(cron = "0 0 12 * * *")
    public void sendUnsentEmails() {
        emailService.sendEmail();
        log.info("[*] {} Email 전송 완료", LocalDateTime.now());
    }
}
