package com.doconnect.chatservice.chat;

import com.doconnect.chatservice.security.ChatPrincipal;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles persistence and retrieval of chat messages.
 */
@Service
public class MessageService {

	private final ChatMessageFactory messageFactory;
	private final MessageModerationStrategy moderationStrategy;
	private final ChatMessageRepository messageRepository;
	private final String globalRoomId;
	private final int defaultHistoryLimit;
	private final int maxHistoryLimit;

	public MessageService(
			ChatMessageFactory messageFactory,
			MessageModerationStrategy moderationStrategy,
			ChatMessageRepository messageRepository,
			@Value("${app.chat.global-room-id}") String globalRoomId,
			@Value("${app.chat.history-default-limit}") int defaultHistoryLimit,
			@Value("${app.chat.history-max-limit}") int maxHistoryLimit
	) {
		this.messageFactory = messageFactory;
		this.moderationStrategy = moderationStrategy;
		this.messageRepository = messageRepository;
		this.globalRoomId = globalRoomId;
		this.defaultHistoryLimit = defaultHistoryLimit;
		this.maxHistoryLimit = maxHistoryLimit;
	}

	@Transactional
	public ChatMessageResponse saveGlobalMessage(ChatMessageRequest request, ChatPrincipal sender) {
		validate(request);
		ModerationDecision moderationDecision = moderationStrategy.evaluate(request, sender);
		ChatMessage message = messageFactory.createGlobalMessage(request, sender, moderationDecision);
		return ChatMessageResponse.from(messageRepository.save(message));
	}

	@Transactional(readOnly = true)
	public List<ChatMessageResponse> getGlobalHistory(Integer requestedLimit) {
		int limit = normalizeLimit(requestedLimit);
		return messageRepository.findByRoomTypeAndRoomIdOrderByCreatedAtDesc(
						RoomType.GLOBAL,
						globalRoomId,
						PageRequest.of(0, limit)
				)
				.stream()
				.sorted(Comparator.comparing(ChatMessage::getCreatedAt))
				.map(ChatMessageResponse::from)
				.toList();
	}

	private void validate(ChatMessageRequest request) {
		if (request == null || request.content() == null || request.content().trim().isBlank()) {
			throw new IllegalArgumentException("Message content is required");
		}
		if (request.content().trim().length() > 1000) {
			throw new IllegalArgumentException("Message content must be 1000 characters or fewer");
		}
	}

	private int normalizeLimit(Integer requestedLimit) {
		if (requestedLimit == null) {
			return defaultHistoryLimit;
		}
		if (requestedLimit < 1) {
			return defaultHistoryLimit;
		}
		return Math.min(requestedLimit, maxHistoryLimit);
	}
}
