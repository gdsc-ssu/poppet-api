package com.gdg.poppet.user.application;

import com.gdg.poppet.global.exception.GlobalException;
import com.gdg.poppet.global.status.ErrorStatus;
import com.gdg.poppet.user.application.dto.response.EmailPeriodDto;
import com.gdg.poppet.user.domain.converter.UserConverter;
import com.gdg.poppet.user.domain.model.User;
import com.gdg.poppet.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;


    /**
     * 사용자의 이메일 전송 주기를 반환한다.
     *
     * @param username
     * @return 사용자의 이메일 전송 주기(1, 3, 7) 반환
     */
    @Override
    public EmailPeriodDto getEmailPeriod(String username) {
        User user = getUser(username);
        return UserConverter.toEmailPeriodDto(user.getEmailPeriod().getValue());
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
        return UserConverter.toEmailPeriodDto(user.getEmailPeriod().getValue());
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new GlobalException(ErrorStatus.USER_NOT_FOUND));
    }
}
