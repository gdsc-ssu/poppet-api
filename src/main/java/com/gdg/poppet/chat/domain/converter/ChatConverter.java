package com.gdg.poppet.chat.domain.converter;

import com.gdg.poppet.chat.domain.model.Chat;
import com.gdg.poppet.chat.domain.model.ChatRoom;

public class ChatConverter {
    public static Chat toChat(int author, String content, ChatRoom chatRoom) {
        return Chat.builder()
                .author(author)
                .content(content)
                .chatRoom(chatRoom)
                .build();
    }

    public static ChatRoom toChatRoom(Long userId) {
        return ChatRoom.builder()
                .userId(userId)
                .build();
    }
}
