package com.gdg.poppet.chat.presentation;

import com.gdg.poppet.chat.application.service.ChatService;
import com.gdg.poppet.chat.infra.gemini.application.service.GeminiService;
import com.gdg.poppet.global.response.ApiResponse;
import com.gdg.poppet.global.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chats")
public class ChatController {

    private final ChatService chatService;
    private final GeminiService geminiService;

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> postChat(
            @RequestParam("chat") List<MultipartFile> requestChat,
            @AuthenticationPrincipal UserDetails user
    ){
        byte[] response = chatService.chat(requestChat, user.getUsername());
        return ApiResponse.success(SuccessStatus.CHAT_SUCCESS, Base64.getEncoder().encodeToString(response));
    }

    @PostMapping("/test") // 대화 기능 테스트를 위한 임시 API
    public ResponseEntity<ApiResponse<String>> testChat(@RequestParam("chat") String chat) {
        return ApiResponse.success(SuccessStatus.CHAT_SUCCESS, geminiService.generateAiResponse("", chat));
    }
}
