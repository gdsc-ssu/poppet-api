package com.gdg.poppet.email.application.service;

import com.gdg.poppet.chat.domain.model.ChatRoom;
import com.gdg.poppet.chat.domain.repository.ChatRoomRepository;
import com.gdg.poppet.email.application.event.EmailSendEvent;
import com.gdg.poppet.global.exception.GlobalException;
import com.gdg.poppet.global.status.ErrorStatus;
import com.gdg.poppet.email.application.dto.request.EmailRequestDto;
import com.gdg.poppet.email.application.dto.response.EmailDto;
import com.gdg.poppet.email.application.dto.response.EmailPeriodDto;
import com.gdg.poppet.email.domain.converter.EmailConverter;
import com.gdg.poppet.email.domain.model.Email;
import com.gdg.poppet.user.domain.model.User;
import com.gdg.poppet.email.domain.repository.EmailRepository;
import com.gdg.poppet.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final UserRepository userRepository;
    private final EmailRepository emailRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

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
    public List<EmailDto> getEmailAddressList(String username) {
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
     * @param emailRequestDto 새롭게 등록할 이메일 주소
     */
    @Transactional
    @Override
    public List<EmailDto> postEmailAddress(String username, EmailRequestDto emailRequestDto) {
        User user = getUser(username);

        validateDuplicateEmail(emailRequestDto.getNewEmail(), user);
        validateEmailFormat(emailRequestDto.getNewEmail());

        // 새로운 이메일 저장
        Email newEmail = EmailConverter.toEmail(emailRequestDto.getNewEmail(), user);
        emailRepository.save(newEmail);

        // 이메일 리스트 반환
        List<Email> emailList = emailRepository.findByUser(user);
        return emailList.stream()
                .map(EmailConverter::toEmailDto)
                .collect(Collectors.toList());
    }

    /**
     * 기존 보호자 이메일의 주소를 새로운 이메일 주소로 변경한다.
     * 이메일이 중복되거나 유저가 이메일에 대한 접근 권한이 없을 경우 예외를 반환한다.
     *
     * @param username
     * @param emailId 변경하려는 기존 이메일의 id
     * @param emailRequestDto 새롭게 변경할 이메일 주소
     */
    @Transactional
    @Override
    public void patchEmailAddress(String username, Long emailId, EmailRequestDto emailRequestDto) {
        User user = getUser(username);
        Email email = getEmail(emailId);

        validateIsUserAuthorizedForEmail(user, email);
        validateDuplicateEmail(emailRequestDto.getNewEmail(), user);
        validateEmailFormat(emailRequestDto.getNewEmail());

        email.updateEmailAddr(emailRequestDto.getNewEmail());
    }

    /**
     * 주어진 이메일 데이터를 제거한다.
     * 유저가 이메일에 대한 접근 권한이 없을 경우 예외를 반환한다.
     *
     * @param username
     * @param emailId 제거할 이메일의 id
     */
    @Transactional
    @Override
    public void deleteEmailAddress(String username, Long emailId) {
        User user = getUser(username);
        Email email = getEmail(emailId);
        validateIsUserAuthorizedForEmail(user, email);

        emailRepository.delete(email);
    }

    /**
     * 유저의 이메일 전송 주기에 따라, 주기 내에 생성된 이메일의 요약 내용을 이메일로 전송한다.
     */
    @Transactional
    @Override
    public void sendEmail() {
        for (User user : userRepository.findAll()) {

            // 가장 최근 생성되고 메일을 보내지 않은 채팅방 조회
            List<ChatRoom> chatRooms = chatRoomRepository.findByUsernameAndCreatedAtAndNotMailSent(user.getUsername());
            if (chatRooms.isEmpty()) return;

            // 메일 보낼 채팅방 요약 내용 추출
            ChatRoom chatRoom = chatRooms.get(0);
            String chatSummary = chatRoom.getSummary();
            if (chatSummary == null) return;

            // 메일 보냄 여부 수정
            chatRoom.updateIsMailSent();

            // 메일 전송 이벤트 발행
            applicationEventPublisher.publishEvent(new EmailSendEvent(user, chatRoom));
        }
    }

    private void validateIsUserAuthorizedForEmail(User user, Email email) {
        if (!email.getUser().equals(user)) {
            throw new GlobalException(ErrorStatus.USER_EMAIL_FORBIDDEN);
        }
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

    private Email getEmail(Long emailId) {
        return emailRepository.findById(emailId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.EMAIL_NOT_FOUND));
    }
}
