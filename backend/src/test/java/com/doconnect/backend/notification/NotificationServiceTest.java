package com.doconnect.backend.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.doconnect.backend.answer.Answer;
import com.doconnect.backend.question.Question;
import com.doconnect.backend.user.User;
import com.doconnect.backend.user.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private NotificationRealtimeService realtimeService;

	@Test
	void questionAnswerCreatesNotificationForQuestionOwner() {
		NotificationService service = service();
		User owner = user(1L, "Owner", "owner@example.com");
		User answerAuthor = user(2L, "Helper", "helper@example.com");
		Question question = new Question();
		ReflectionTestUtils.setField(question, "id", 10L);
		question.setTitle("JWT help");
		question.setAuthor(owner);
		Answer answer = new Answer();
		ReflectionTestUtils.setField(answer, "id", 22L);
		answer.setAuthor(answerAuthor);

		when(notificationRepository.save(org.mockito.ArgumentMatchers.any(Notification.class)))
				.thenAnswer(invocation -> {
					Notification notification = invocation.getArgument(0);
					ReflectionTestUtils.setField(notification, "id", 99L);
					return notification;
				});
		when(notificationRepository.countByRecipientIdAndReadFalse(1L)).thenReturn(1L);

		service.notifyQuestionAnswered(question, answer);

		ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
		verify(notificationRepository).save(captor.capture());
		assertThat(captor.getValue().getType()).isEqualTo(NotificationType.ANSWER);
		assertThat(captor.getValue().getRecipient().getId()).isEqualTo(1L);
		verify(realtimeService).sendToUser(eq("owner@example.com"), any(NotificationResponse.class), eq(1L));
	}

	@Test
	void chatMessagesAggregateUnreadNotifications() {
		NotificationService service = service();
		User sender = user(1L, "Sender", "sender@example.com");
		User recipient = user(2L, "Reader", "reader@example.com");
		Notification existing = new Notification();
		ReflectionTestUtils.setField(existing, "id", 15L);
		existing.setRecipient(recipient);
		existing.setType(NotificationType.CHAT);
		existing.setSourceKey("chat:global");
		existing.setOccurrenceCount(2);
		existing.setRead(false);

		when(userRepository.findAll()).thenReturn(List.of(sender, recipient));
		when(notificationRepository.findFirstByRecipientIdAndTypeAndSourceKeyAndReadFalse(2L, NotificationType.CHAT, "chat:global"))
				.thenReturn(Optional.of(existing));
		when(notificationRepository.save(org.mockito.ArgumentMatchers.any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(notificationRepository.countByRecipientIdAndReadFalse(2L)).thenReturn(1L);

		service.notifyChatMessage(new ChatNotificationEvent(1L, "Sender", 50L, "Hello world", "global"));

		assertThat(existing.getOccurrenceCount()).isEqualTo(3);
		assertThat(existing.getMessage()).contains("Sender");
		verify(realtimeService).sendToUser(eq("reader@example.com"), any(NotificationResponse.class), eq(1L));
	}

	private NotificationService service() {
		return new NotificationService(notificationRepository, userRepository, realtimeService);
	}

	private User user(Long id, String name, String email) {
		User user = new User();
		ReflectionTestUtils.setField(user, "id", id);
		user.setName(name);
		user.setEmail(email);
		user.setPasswordHash("hash");
		return user;
	}
}
