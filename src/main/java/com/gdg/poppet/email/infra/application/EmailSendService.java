package com.gdg.poppet.email.infra.application;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSendService {

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String body) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        String subject = "POPPET";

        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(body, true);

        mailSender.send(mimeMessage);
    }
}
