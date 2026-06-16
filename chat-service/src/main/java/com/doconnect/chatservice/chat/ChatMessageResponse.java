package com.doconnect.chatservice.chat;

import java.time.Instant;

/**
 * Response DTO for chat messages. Uses a builder because later moderation and
 * analytics fields can be added without changing call sites heavily.
 */
public class ChatMessageResponse {

	private final Long id;
	private final Long senderId;
	private final String username;
	private final String content;
	private final String roomId;
	private final RoomType roomType;
	private final ModerationStatus moderationStatus;
	private final Instant createdAt;

	private ChatMessageResponse(Builder builder) {
		this.id = builder.id;
		this.senderId = builder.senderId;
		this.username = builder.username;
		this.content = builder.content;
		this.roomId = builder.roomId;
		this.roomType = builder.roomType;
		this.moderationStatus = builder.moderationStatus;
		this.createdAt = builder.createdAt;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static ChatMessageResponse from(ChatMessage message) {
		return builder()
				.id(message.getId())
				.senderId(message.getSenderId())
				.username(message.getUsername())
				.content(message.getContent())
				.roomId(message.getRoomId())
				.roomType(message.getRoomType())
				.moderationStatus(message.getModerationStatus())
				.createdAt(message.getCreatedAt())
				.build();
	}

	public Long getId() {
		return id;
	}

	public Long getSenderId() {
		return senderId;
	}

	public String getUsername() {
		return username;
	}

	public String getContent() {
		return content;
	}

	public String getRoomId() {
		return roomId;
	}

	public RoomType getRoomType() {
		return roomType;
	}

	public ModerationStatus getModerationStatus() {
		return moderationStatus;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public static class Builder {

		private Long id;
		private Long senderId;
		private String username;
		private String content;
		private String roomId;
		private RoomType roomType;
		private ModerationStatus moderationStatus;
		private Instant createdAt;

		public Builder id(Long id) {
			this.id = id;
			return this;
		}

		public Builder senderId(Long senderId) {
			this.senderId = senderId;
			return this;
		}

		public Builder username(String username) {
			this.username = username;
			return this;
		}

		public Builder content(String content) {
			this.content = content;
			return this;
		}

		public Builder roomId(String roomId) {
			this.roomId = roomId;
			return this;
		}

		public Builder roomType(RoomType roomType) {
			this.roomType = roomType;
			return this;
		}

		public Builder moderationStatus(ModerationStatus moderationStatus) {
			this.moderationStatus = moderationStatus;
			return this;
		}

		public Builder createdAt(Instant createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public ChatMessageResponse build() {
			return new ChatMessageResponse(this);
		}
	}
}
