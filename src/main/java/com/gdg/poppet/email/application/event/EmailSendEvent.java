package com.gdg.poppet.email.application.event;

import com.gdg.poppet.chat.domain.model.ChatRoom;
import com.gdg.poppet.user.domain.model.User;
import lombok.Getter;

@Getter
public class EmailSendEvent {
    private final User user;
    private final ChatRoom chatRoom;

    public EmailSendEvent(User user, ChatRoom chatRoom) {
        this.user = user;
        this.chatRoom = chatRoom;
    }
}
