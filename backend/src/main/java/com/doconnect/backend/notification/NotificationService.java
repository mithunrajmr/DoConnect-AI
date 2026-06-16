package com.doconnect.backend.notification;

import com.doconnect.backend.answer.Answer;
import com.doconnect.backend.common.ResourceNotFoundException;
import com.doconnect.backend.question.Question;
import com.doconnect.backend.user.User;
import com.doconnect.backend.user.UserRepository;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class NotificationService {

	private static final String GLOBAL_CHAT_SOURCE_KEY = "chat:global";

	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository;
	private final NotificationRealtimeService realtimeService;

	public NotificationService(
			NotificationRepository notificationRepository,
			UserRepository userRepository,
			NotificationRealtimeService realtimeService
	) {
		this.notificationRepository = notificationRepository;
		this.userRepository = userRepository;
		this.realtimeService = realtimeService;
	}

	@Transactional(readOnly = true)
	public List<NotificationResponse> findAll(User currentUser) {
		return notificationRepository.findByRecipientIdOrderByUpdatedAtDesc(currentUser.getId()).stream()
				.map(NotificationResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public UnreadCountResponse unreadCount(User currentUser) {
		return new UnreadCountResponse(notificationRepository.countByRecipientIdAndReadFalse(currentUser.getId()));
	}

	@Transactional
	public NotificationResponse markRead(Long id, User currentUser) {
		Notification notification = notificationRepository.findByIdAndRecipientId(id, currentUser.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
		if (!notification.isRead()) {
			notification.setRead(true);
			notification = notificationRepository.save(notification);
			log.info("Notification marked as read. notificationId={}, userId={}", notification.getId(), currentUser.getId());
			realtimeService.unreadCountChanged(currentUser.getEmail(), notificationRepository.countByRecipientIdAndReadFalse(currentUser.getId()));
		}
		return NotificationResponse.from(notification);
	}

	@Transactional
	public UnreadCountResponse markAllRead(User currentUser) {
		notificationRepository.markAllAsReadByRecipientId(currentUser.getId());
		log.info("Notification marked as read. count=ALL, userId={}", currentUser.getId());
		realtimeService.unreadCountChanged(currentUser.getEmail(), 0L);
		return new UnreadCountResponse(0L);
	}

	@Transactional
	public void notifyQuestionAnswered(Question question, Answer answer) {
		User recipient = question.getAuthor();
		if (recipient.getId().equals(answer.getAuthor().getId())) {
			return;
		}

		Notification notification = new Notification();
		notification.setRecipient(recipient);
		notification.setType(NotificationType.ANSWER);
		notification.setTitle("New answer on your question");
		notification.setMessage(answer.getAuthor().getName() + " answered \"" + truncate(question.getTitle(), 80) + "\"");
		notification.setTargetPath("/questions/" + question.getId());
		notification.setReferenceId(answer.getId());
		notification.setSourceKey("answer:" + answer.getId());
		notification.setRead(false);
		notification.setOccurrenceCount(1);

		Notification saved = notificationRepository.save(notification);
		log.info("Notification created. notificationId={}, recipientId={}, type={}", saved.getId(), recipient.getId(), saved.getType());
		push(saved);
	}

	@Transactional
	public void notifyChatMessage(ChatNotificationEvent event) {
		for (User recipient : userRepository.findAll()) {
			if (recipient.getId().equals(event.senderId())) {
				continue;
			}

			Notification notification = notificationRepository
					.findFirstByRecipientIdAndTypeAndSourceKeyAndReadFalse(recipient.getId(), NotificationType.CHAT, GLOBAL_CHAT_SOURCE_KEY)
					.orElseGet(Notification::new);

			boolean isNew = notification.getId() == null;
			if (isNew) {
				notification.setRecipient(recipient);
				notification.setType(NotificationType.CHAT);
				notification.setTitle("New chat messages");
				notification.setTargetPath("/chat");
				notification.setSourceKey(GLOBAL_CHAT_SOURCE_KEY);
				notification.setRead(false);
				notification.setOccurrenceCount(0);
			}

			notification.setReferenceId(event.messageId());
			notification.setOccurrenceCount(notification.getOccurrenceCount() + 1);
			notification.setMessage(event.senderName() + ": " + truncate(event.content(), 120));

			Notification saved = notificationRepository.save(notification);
			if (isNew) {
				log.info("Notification created. notificationId={}, recipientId={}, type={}", saved.getId(), recipient.getId(), saved.getType());
			}
			push(saved);
		}
	}

	private void push(Notification notification) {
		long unreadCount = notificationRepository.countByRecipientIdAndReadFalse(notification.getRecipient().getId());
		realtimeService.sendToUser(notification.getRecipient().getEmail(), NotificationResponse.from(notification), unreadCount);
	}

	private String truncate(String value, int maxLength) {
		if (value == null) {
			return "";
		}
		String trimmed = value.trim();
		return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength - 3) + "...";
	}
}
