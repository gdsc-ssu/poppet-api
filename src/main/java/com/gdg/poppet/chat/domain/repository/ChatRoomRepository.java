package com.gdg.poppet.chat.domain.repository;

import com.gdg.poppet.chat.domain.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query(value = "SELECT cr " +
            "FROM ChatRoom cr " +
            "WHERE cr.username = :username " +
            "ORDER BY cr.createdAt DESC")
    List<ChatRoom> findByUsernameAndCreatedAt(@Param(value = "username") String username);

    @Modifying
    @Query("DELETE " +
            "FROM ChatRoom cr " +
            "WHERE cr.chatRoomId = :chatRoomId")
    void deleteChatRoomByChatRoomId(@Param(value = "chatRoomId") Long chatRoomId);
}
