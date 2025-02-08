package com.gdg.poppet.chat.application.util;

import com.gdg.poppet.global.exception.GlobalException;
import com.gdg.poppet.global.status.ErrorStatus;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GoogleCloudUtil {

    private final SpeechSettings speechSettings;

    /**
     * FLAC 형식의 오디오 파일을 STT API를 이용해 한국어 기반 텍스트로 전환 후 반환한다.
     *
     * @param file 1분 이하의 오디오 파일
     * @return 전환된 한국어 기반 텍스트
     */
    public String speechToText(MultipartFile file) {

        if (file.isEmpty()) {
            throw new GlobalException(ErrorStatus.STT_FILE_IS_EMPTY);
        }

        try (SpeechClient speechClient = SpeechClient.create(speechSettings)) {

            // 오디오 파일 decoding
            byte[] audioBytes = file.getBytes();
            ByteString audioData = ByteString.copyFrom(audioBytes);

            // 오디오 객체 생성
            RecognitionAudio recognitionAudio = RecognitionAudio.newBuilder()
                    .setContent(audioData)
                    .build();

            // 설정 객체 생성
            RecognitionConfig recognitionConfig =
                    RecognitionConfig.newBuilder()
                            .setEncoding(RecognitionConfig.AudioEncoding.FLAC)
                            .setSampleRateHertz(48000)
                            .setLanguageCode("ko-kR")
                            .build();

            // 해석된 결과 텍스트 반환
            RecognizeResponse response = speechClient.recognize(recognitionConfig, recognitionAudio);
            List<SpeechRecognitionResult> results = response.getResultsList();

            if (!results.isEmpty()) {
                SpeechRecognitionResult result = results.get(0);
                return result.getAlternatives(0).getTranscript();
            } else {
                // TODO: 예외처리 논의 필요
                return "";
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
