package com.gdg.poppet.user.application.service;

import com.gdg.poppet.global.exception.GlobalException;
import com.gdg.poppet.global.status.ErrorStatus;
import com.gdg.poppet.user.application.dto.response.EmailDto;
import com.gdg.poppet.user.application.dto.response.EmailPeriodDto;
import com.gdg.poppet.user.domain.converter.EmailConverter;
import com.gdg.poppet.user.domain.model.Email;
import com.gdg.poppet.user.domain.model.User;
import com.gdg.poppet.user.domain.repository.EmailRepository;
import com.gdg.poppet.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final UserRepository userRepository;
    private final EmailRepository emailRepository;

    /**
     * 사용자의 이메일 전송 주기를 반환한다.
     *
     * @param username
     * @return 사용자의 이메일 전송 주기(1, 3, 7) 반환
     */
    @Override
    public EmailPeriodDto getEmailPeriod(String username) {
        User user = getUser(username);
        return EmailConverter.toEmailPeriodDto(user.getEmailPeriod().getValue());
    }

    /**
     * 사용자의 이메일 전송 주기를 파라미터로 들어온 period 데이터로 변경 한 후, 변경된 주기 데이터를 반환한다.
     *
     * @param username
     * @param period 변경할 전송 주기(1, 3, 7)
     * @return 변경된 사용자의 이메일 전송 주기(1, 3, 7) 반환
     */
    @Override
    @Transactional
    public EmailPeriodDto patchEmailPeriod(String username, int period) {
        User user = getUser(username);
        user.updateEmailPeriod(period);
        return EmailConverter.toEmailPeriodDto(user.getEmailPeriod().getValue());
    }

    /**
     * 사용자가 등록한 보호자 이메일 리스트를 등록 순으로 조회한다.
     *
     * @param username
     * @return 사용자가 등록한 보호자 이메일 리스트
     */
    @Override
    public List<EmailDto> getEmailList(String username) {
        User user = getUser(username);
        List<Email> emailList = emailRepository.findByUser(user);

        return emailList.stream()
                .map(EmailConverter::toEmailDto)
                .collect(Collectors.toList());
    }

    /**
     * 새로운 보호자 이메일을 등록한다.
     * 이메일 형식이 올바르지 않거나 중복될 경우 예외를 반환한다.
     *
     * @param username
     * @param email 새롭게 등록할 이메일 주소
     */
    @Override
    public void postEmail(String username, String email) {
        User user = getUser(username);
        validateDuplicateEmail(email, user);
        validateEmailFormat(email);

        Email newEmail = EmailConverter.toEmail(email, user);
        emailRepository.save(newEmail);
    }

    private void validateEmailFormat(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (!email.matches(emailRegex)) {
            throw new GlobalException(ErrorStatus.EMAIL_FORMAT_INVALID);
        }
    }

    private void validateDuplicateEmail(String email, User user) {
        if (isExistingEmail(email, user)) {
            throw new GlobalException(ErrorStatus.DUPLICATED_EMAIL_ADDR);
        }
    }

    private boolean isExistingEmail(String email, User user) {
        return user.getEmails().stream()
                .anyMatch(Email -> Email.getEmailAddress().equals(email));
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new GlobalException(ErrorStatus.USER_NOT_FOUND));
    }
}
