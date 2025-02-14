package com.gdg.poppet.chat.infra.gemini.application.service;

import com.gdg.poppet.chat.infra.gemini.application.dto.GeminiRequestDto;
import com.gdg.poppet.chat.infra.gemini.application.dto.GeminiResponseDto;
import com.gdg.poppet.global.util.ResourceLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiService {

    @Value("${ai.gemini.base-host}")
    private String geminiBaseHost;

    @Value("${ai.gemini.key}")
    private String geminiApiKey;

    private final WebClient webClient;

    private static final String GEMINI_CHAT_PROMPT = ResourceLoader.getResourceContent("gemini-chat-prompt.txt");


    /**
     * 사용자의 요청 텍스트를 받아 지정된 프롬프트와 함께 Gemini API를 호출 후 응답을 반환한다.
     * 프롬프팅을 통해 지정된 어투로 맥락에 알맞는 대화를 이어갈 응답을 반환하도록 지시한다.
     * 호출 도중 예외가 발생할 경우 RuntimeException을 발생시킨다.
     *
     * @param request 사용자의 대화 요청 텍스트
     * @return Gemini API의 응답값 중 TEXT Data
     */
    public String generateAiResponse(String request) {
        GeminiRequestDto requestDto = GeminiRequestDto.builder()
                .contents(List.of(new GeminiRequestDto.Content(
                        List.of(new GeminiRequestDto.Part(GEMINI_CHAT_PROMPT + request))
                )))
                .build();

        GeminiResponseDto responseDto = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host(geminiBaseHost)
                        .path("/v1beta/models/gemini-1.5-flash-latest:generateContent")
                        .queryParam("key", geminiApiKey)
                        .build())
                .header("Accept", "application/json")
                .bodyValue(requestDto)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                    log.error("[*] GeminiService Request Failed - Status: {}, Body: {}",
                            clientResponse.statusCode(),
                            clientResponse.bodyToMono(String.class).block());
                    return Mono.error(new RuntimeException("GeminiService Request Failed"));
                })
                .bodyToMono(GeminiResponseDto.class)
                .block();

        return responseDto.getCandidates().get(0).getContent().getParts().get(0).getText();
    }

}
