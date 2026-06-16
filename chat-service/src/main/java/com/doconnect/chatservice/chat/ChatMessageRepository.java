package com.doconnect.chatservice.chat;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	List<ChatMessage> findByRoomTypeAndRoomIdOrderByCreatedAtDesc(RoomType roomType, String roomId, Pageable pageable);
}
