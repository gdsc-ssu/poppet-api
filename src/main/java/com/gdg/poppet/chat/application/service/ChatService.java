package com.gdg.poppet.chat.application.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface ChatService {
    byte[] chat(List<MultipartFile> message);
}
