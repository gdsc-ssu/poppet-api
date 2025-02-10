package com.gdg.poppet.chat.presentation;

import com.gdg.poppet.chat.application.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

}
