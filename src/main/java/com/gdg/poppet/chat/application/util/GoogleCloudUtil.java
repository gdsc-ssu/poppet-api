package com.gdg.poppet.chat.application.util;

import com.gdg.poppet.global.exception.GlobalException;
import com.gdg.poppet.global.status.ErrorStatus;
import com.google.cloud.speech.v1.*;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleCloudUtil {
    private final SpeechClient speechClient;
    private final TextToSpeechClient textToSpeechClient;

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

        try {
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
                log.warn("[*] Google STT 결과 반환 중 오류 발생");
                return "";
            }
        } catch (Exception e) {
            log.warn("[*] Google STT 변환 중 오류 발생 : {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * TTS API를 이용해 한국어 기반 텍스트를 FLAC 형식의 오디오 파일로 변환 후 반환한다.
     *
     * @param text 오디오 파일로 전환할 텍스트
     */
    public byte[] textToSpeech(String text) {

        try {
            // 변환할 텍스트 설정
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            // Voice Request 객체 생성
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                            .setLanguageCode("ko-kr")
                            .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                            .build();

            // 오디오 타입 설정
            AudioConfig audioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();

            // 오디오 변환 결과
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
            ByteString audioContents = response.getAudioContent();
            return audioContents.toByteArray();

        } catch (Exception e) {
            log.warn("[*] Google TTS 변환 중 오류 발생 : {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
