package com.doconnect.chatservice.chat;

import com.doconnect.chatservice.security.ChatPrincipal;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles persistence and retrieval of chat messages.
 */
@Service
public class MessageService {

	private static final Logger log = LoggerFactory.getLogger(MessageService.class);

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
		try {
			ChatMessage savedMessage = messageRepository.save(message);
			log.info("Chat message persisted. messageId={}, senderId={}, roomId={}", savedMessage.getId(), sender.userId(), globalRoomId);
			return ChatMessageResponse.from(savedMessage);
		} catch (Exception e) {
			log.error("Chat persistence failures. error={}, senderId={}", e.getMessage(), sender.userId());
			throw e;
		}
	}

	@Transactional(readOnly = true)
	public List<ChatMessageResponse> getGlobalHistory(Integer requestedLimit) {
		int limit = normalizeLimit(requestedLimit);
		List<ChatMessageResponse> history = messageRepository.findByRoomTypeAndRoomIdOrderByCreatedAtDesc(
						RoomType.GLOBAL,
						globalRoomId,
						PageRequest.of(0, limit)
				)
				.stream()
				.sorted(Comparator.comparing(ChatMessage::getCreatedAt))
				.map(ChatMessageResponse::from)
				.toList();
		log.info("Chat history loaded. roomId={}, messageCount={}", globalRoomId, history.size());
		return history;
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
