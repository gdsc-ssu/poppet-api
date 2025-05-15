package com.gdg.poppet.chat.application.service;

import com.gdg.poppet.chat.domain.converter.ChatConverter;
import com.gdg.poppet.chat.domain.model.Chat;
import com.gdg.poppet.chat.domain.model.ChatRoom;
import com.gdg.poppet.chat.domain.repository.ChatRepository;
import com.gdg.poppet.chat.domain.repository.ChatRoomRepository;
import com.gdg.poppet.chat.infra.gemini.application.service.GeminiService;
import com.gdg.poppet.chat.infra.speech.application.GoogleCloudService;
import com.gdg.poppet.global.exception.GlobalException;
import com.gdg.poppet.global.status.ErrorStatus;
import com.gdg.poppet.user.domain.enums.Provider;
import com.gdg.poppet.user.domain.model.User;
import com.gdg.poppet.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final GoogleCloudService googleCloudService;
    private final GeminiService geminiService;
    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    /**
     * 사용자의 음성 파일을 받아 맥락에 알맞게 이어질 대화 응답값을 생성 후 음성 파일로 변환해 반환한다.
     *
     * @param requestFile 1분 이하의 사용자로부터 받은 음성 파일
     * @return : AI의 대화 응답값을 음성 파일로 변환환 결과
     */
    @Override
    @Transactional
    public byte[] chat(List<MultipartFile> requestFile, String username) {
        // TODO: TRANSACTION 분리

        // 1. STT를 이용해 텍스트 추출
        String requestText = speechToText(requestFile);
        log.info("[*] requestText : {}", requestText);

        // 2. Chatroom 생성
        User user = getUser(username);
        ChatRoom chatRoom = getChatRoom(user);

        // 3. Chat 저장
        Chat requestChat = ChatConverter.toChat(0, requestText, chatRoom);
        chatRepository.save(requestChat);

        // 4. Gemini를 이용해 응답 생성
        String responseText = geminiService.generateAiResponse(chatRoom.getSummary(), requestText);
        log.info("[*] responseText : {}", responseText);

        // 5. Chat 저장
        Chat responseChat = ChatConverter.toChat(1, responseText, chatRoom);
        chatRepository.save(responseChat);

        // 6. TTS를 이용해 음성파일 추출
        return googleCloudService.textToSpeech(responseText);
    }

    private String speechToText(List<MultipartFile> requestFile) {
        StringBuilder requestText = new StringBuilder();

        for (MultipartFile file : requestFile) {
            String text = googleCloudService.speechToText(file);
            requestText.append(text);
        }
        return requestText.toString();
    }

    public ChatRoom getChatRoom(User user) {
        LocalDate emailPeriodDate = LocalDate.now().minusDays(user.getEmailPeriod().getValue());

        // 가장 최근 생성된 채팅방 조회
        List<ChatRoom> chatRooms = chatRoomRepository.findByUsernameAndCreatedAt(user.getUsername());
        ChatRoom chatRoom = chatRooms.isEmpty()
                ? createFirstChatRoom(user.getUsername())
                : chatRooms.get(0);

        // 설정된 이메일 전송 기간 내에 생성되었다면 채팅방 유지
        if (chatRoom.isValidChatRoom(emailPeriodDate)) {
            return chatRoom;
        }

        // 기간을 초과했다면 새로운 채팅방 생성
        return createNewChatRoom(user.getUsername(), chatRoom);
    }

    private ChatRoom createNewChatRoom(String username, ChatRoom chatRoom) {
        // 가장 최근 chatroom summary 생성
        String summaryRequest = parseChatSummaryRequest(chatRoom);
        String summary = geminiService.generateChatSummary(summaryRequest);
        log.info("[*] ChatRoomSummary : {}", summary);

        // 새로운 chatRoom 생성
        ChatRoom newChatRoom = ChatConverter.toChatRoom(username, summary);
        chatRoomRepository.save(newChatRoom);

        // 이전 chatRoom 제거
        deleteRecentChats(chatRoom.getChatRoomId());

        return newChatRoom;
    }

//    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteRecentChats(Long chatRoomId) {
        chatRepository.deleteChatsByChatRoomId(chatRoomId);
        chatRoomRepository.deleteChatRoomByChatRoomId(chatRoomId);
    }

    private String parseChatSummaryRequest(ChatRoom chatRoom) {
        StringBuilder chatSummary = new StringBuilder();
        chatRoom.getChats().forEach(
                chat -> {chatSummary.append(chat.getContent());});
        return chatSummary.toString();
    }

    private ChatRoom createFirstChatRoom(String username) {
        ChatRoom newChatRoom = ChatConverter.toChatRoom(username, null);
        return chatRoomRepository.save(newChatRoom);
    }

    private User getUser(String key) {
        String[] auth = key.split("#");
        return userRepository.findByUserIdAndProvider(auth[0], Provider.valueOf(auth[1]))
                .orElseThrow(() -> new GlobalException(ErrorStatus.USER_NOT_FOUND));
    }
}
