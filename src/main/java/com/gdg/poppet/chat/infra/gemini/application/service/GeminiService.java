package com.gdg.poppet.chat.infra.gemini.application.service;

import com.gdg.poppet.chat.infra.gemini.application.dto.GeminiRequestDto;
import com.gdg.poppet.chat.infra.gemini.application.dto.GeminiResponseDto;
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

    public String generateAiResponse(String request) {
        GeminiRequestDto requestDto = GeminiRequestDto.builder()
                .contents(List.of(new GeminiRequestDto.Content(
                        List.of(new GeminiRequestDto.Part(request))
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
