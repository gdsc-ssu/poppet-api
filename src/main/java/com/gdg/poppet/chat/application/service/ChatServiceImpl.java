package com.gdg.poppet.chat.application.service;

import com.gdg.poppet.chat.application.util.GoogleCloudUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final GoogleCloudUtil googleCloudUtil;

    /**
     * 사용자의 음성 파일을 받아 맥락에 알맞게 이어질 대화 응답값을 생성 후 음성 파일로 변환해 반환한다.
     *
     * @param requestFile 1분 이하의 사용자로부터 받은 음성 파일
     * @return : AI의 대화 응답값을 음성 파일로 변환환 결과
     */
    @Override
    public byte[] chat(List<MultipartFile> requestFile) {
        StringBuilder requestText = new StringBuilder();

        for (MultipartFile file : requestFile) {
            // 1. STT를 이용해 텍스트 추출
            String text = googleCloudUtil.speechToText(file);
            requestText.append(text);
        }
        log.info("[*] requestText : {}", requestText.toString());

        // 2. Gemini를 이용해 응답 생성

        // 3. TTS를 이용해 음성파일 추출
        return googleCloudUtil.textToSpeech(requestText.toString());
    }
}
