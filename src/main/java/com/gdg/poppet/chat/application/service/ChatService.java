package com.gdg.poppet.chat.application.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface ChatService {
    String chat(MultipartFile message);
}
