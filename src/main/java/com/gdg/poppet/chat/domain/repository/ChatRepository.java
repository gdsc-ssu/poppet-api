package com.gdg.poppet.chat.domain.repository;

import com.gdg.poppet.chat.domain.model.Chat;
import com.gdg.poppet.chat.domain.model.ChatRoom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Modifying
    @Query("DELETE " +
            "FROM Chat c " +
            "WHERE c.chatRoom.chatRoomId = :chatRoomId")
    void deleteChatsByChatRoomId(@Value("chatRoomId") Long chatRoomId);
}
