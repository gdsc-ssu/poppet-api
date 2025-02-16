package com.gdg.poppet.chat.application.service;

import com.gdg.poppet.chat.domain.converter.ChatConverter;
import com.gdg.poppet.chat.domain.model.Chat;
import com.gdg.poppet.chat.domain.model.ChatRoom;
import com.gdg.poppet.chat.domain.repository.ChatRepository;
import com.gdg.poppet.chat.domain.repository.ChatRoomRepository;
import com.gdg.poppet.chat.infra.gemini.application.service.GeminiService;
import com.gdg.poppet.chat.infra.speech.application.GoogleCloudService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final GoogleCloudService googleCloudService;
    private final GeminiService geminiService;
    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * 사용자의 음성 파일을 받아 맥락에 알맞게 이어질 대화 응답값을 생성 후 음성 파일로 변환해 반환한다.
     *
     * @param requestFile 1분 이하의 사용자로부터 받은 음성 파일
     * @return : AI의 대화 응답값을 음성 파일로 변환환 결과
     */
    @Override
    public Resource chat(List<MultipartFile> requestFile) {
        // TODO: TRANSACTION 분리

        // 1. STT를 이용해 텍스트 추출
        String requestText = speechToText(requestFile);
        log.info("[*] requestText : {}", requestText);

        // 2. Chatroom 생성
        ChatRoom chatRoom = getChatRoom();

        // 3. Chat 저장
        Chat requestChat = ChatConverter.toChat(0, requestText, chatRoom);
        chatRepository.save(requestChat);

        // 4. Gemini를 이용해 응답 생성
        String responseText = geminiService.generateAiResponse(requestText);
        log.info("[*] responseText : {}", responseText);

        // 5. Chat 저장
        Chat responseChat = ChatConverter.toChat(1, responseText, chatRoom);
        chatRepository.save(responseChat);

        // 6. TTS를 이용해 음성파일 추출
        return googleCloudService.textToSpeech(responseText);
    }

    private ChatRoom getChatRoom() {
        // TODO: userId 수정
        Long userId = 1L;

        // TODO: finding chatRoom query
        return chatRoomRepository.findByUserId(userId)
                .orElseGet(() -> createNewChatRoom(userId));
    }

    private ChatRoom createNewChatRoom(Long userId) {
        // 새로운 chatRoom 생성
        ChatRoom newChatRoom = ChatConverter.toChatRoom(userId);
        chatRoomRepository.save(newChatRoom);

        // 이전 chatRoom 제거

        // 가장 최근 chatroom summary 생성 (async)

        return newChatRoom;
    }

    private String speechToText(List<MultipartFile> requestFile) {
        StringBuilder requestText = new StringBuilder();

        for (MultipartFile file : requestFile) {
            String text = googleCloudService.speechToText(file);
            requestText.append(text);
        }
        return requestText.toString();
    }
}
