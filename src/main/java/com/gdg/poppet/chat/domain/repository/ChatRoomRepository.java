package com.gdg.poppet.chat.domain.repository;

import com.gdg.poppet.chat.domain.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT cr " +
            "FROM ChatRoom cr " +
            "WHERE cr.userId = :userId " +
            "") // 1. user의 이메일 발송 기간(N일) 이전에 생성된
    Optional<ChatRoom> findByUserId(@Param(value = "userId") Long userId);
}
