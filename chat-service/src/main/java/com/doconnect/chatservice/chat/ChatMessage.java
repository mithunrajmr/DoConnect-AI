package com.doconnect.chatservice.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Persisted chat message for the global DoConnect discussion room.
 */
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "sender_id", nullable = false)
	private Long senderId;

	@Column(name = "username", nullable = false, length = 160)
	private String username;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(name = "room_type", nullable = false, length = 30)
	private RoomType roomType = RoomType.GLOBAL;

	@Column(name = "room_id", nullable = false, length = 80)
	private String roomId = "global";

	@Enumerated(EnumType.STRING)
	@Column(name = "moderation_status", nullable = false, length = 20)
	private ModerationStatus moderationStatus = ModerationStatus.APPROVED;

	@Column(name = "moderation_reason", length = 255)
	private String moderationReason;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public Long getSenderId() {
		return senderId;
	}

	public void setSenderId(Long senderId) {
		this.senderId = senderId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public RoomType getRoomType() {
		return roomType;
	}

	public void setRoomType(RoomType roomType) {
		this.roomType = roomType;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public ModerationStatus getModerationStatus() {
		return moderationStatus;
	}

	public void setModerationStatus(ModerationStatus moderationStatus) {
		this.moderationStatus = moderationStatus;
	}

	public String getModerationReason() {
		return moderationReason;
	}

	public void setModerationReason(String moderationReason) {
		this.moderationReason = moderationReason;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
