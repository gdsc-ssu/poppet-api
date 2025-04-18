package com.gdg.poppet.email.infra.application;

import com.gdg.poppet.email.application.event.EmailSendEvent;
import com.gdg.poppet.email.domain.model.Email;
import com.gdg.poppet.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSendEventListener {

    private final EmailSendService emailSendService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmailSendEvent(EmailSendEvent event) {
        User user = event.getUser();
        String summary = event.getChatRoom().getSummary();

        for (Email email : user.getEmails()) {
            try {
                emailSendService.sendEmail(email.getEmailAddress(), summary);
            } catch (Exception e) {
                log.error("[*] {}으로 이메일 전송 중 오류 발생", email.getEmailAddress(), e);
            }
        }
    }
}
