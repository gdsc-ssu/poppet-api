package com.gdg.poppet.email.infra.sender.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSendService {

    private final JavaMailSender mailSender;

    public void sendEmail(String to, ByteArrayResource body) throws MessagingException {
        String subject = "POPPET";

        // message 설정
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setSubject(subject);

        // image 배경 설정
        mimeMessageHelper.setText("<html><body><img src='cid:image' style='width:800px; height:auto;'/></body></html>", true);
        mimeMessageHelper.addInline("image", body, "image/png");

        // mail 전송
        mailSender.send(mimeMessage);
    }
}
