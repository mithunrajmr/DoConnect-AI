package com.doconnect.chatservice.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.doconnect.chatservice.security.ChatPrincipal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

	@Mock
	private ChatMessageRepository messageRepository;

	@Mock
	private MessageModerationStrategy moderationStrategy;

	@Test
	void saveGlobalMessageCreatesModeratesAndPersistsMessage() {
		ChatMessageFactory factory = new ChatMessageFactory("global");
		MessageService messageService = new MessageService(factory, moderationStrategy, messageRepository, "global", 50, 200);
		ChatPrincipal sender = new ChatPrincipal(42L, "Mithun", "USER");
		ChatMessageRequest request = new ChatMessageRequest("  Hello global room  ");

		when(moderationStrategy.evaluate(request, sender))
				.thenReturn(new ModerationDecision(ModerationStatus.APPROVED, null));
		when(messageRepository.save(any(ChatMessage.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		ChatMessageResponse response = messageService.saveGlobalMessage(request, sender);

		ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
		verify(messageRepository).save(messageCaptor.capture());
		ChatMessage persisted = messageCaptor.getValue();

		assertThat(persisted.getSenderId()).isEqualTo(42L);
		assertThat(persisted.getUsername()).isEqualTo("Mithun");
		assertThat(persisted.getContent()).isEqualTo("Hello global room");
		assertThat(persisted.getRoomType()).isEqualTo(RoomType.GLOBAL);
		assertThat(persisted.getRoomId()).isEqualTo("global");
		assertThat(persisted.getModerationStatus()).isEqualTo(ModerationStatus.APPROVED);
		assertThat(response.getSenderId()).isEqualTo(42L);
		assertThat(response.getContent()).isEqualTo("Hello global room");
	}

	@Test
	void saveGlobalMessageRejectsBlankContent() {
		ChatMessageFactory factory = new ChatMessageFactory("global");
		MessageService messageService = new MessageService(factory, moderationStrategy, messageRepository, "global", 50, 200);
		ChatPrincipal sender = new ChatPrincipal(42L, "Mithun", "USER");

		assertThatThrownBy(() -> messageService.saveGlobalMessage(new ChatMessageRequest("   "), sender))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Message content is required");
	}

	@Test
	void getGlobalHistoryCapsLimitAndReturnsOldestFirst() throws Exception {
		ChatMessage newest = message("newest");
		Thread.sleep(2);
		ChatMessage older = message("older");
		List<ChatMessage> repositoryOrder = List.of(newest, older);

		when(messageRepository.findByRoomTypeAndRoomIdOrderByCreatedAtDesc(
				any(RoomType.class),
				any(String.class),
				any(Pageable.class)
		)).thenReturn(repositoryOrder);

		ChatMessageFactory factory = new ChatMessageFactory("global");
		MessageService messageService = new MessageService(factory, moderationStrategy, messageRepository, "global", 50, 200);

		List<ChatMessageResponse> history = messageService.getGlobalHistory(999);

		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(messageRepository).findByRoomTypeAndRoomIdOrderByCreatedAtDesc(
				any(RoomType.class),
				any(String.class),
				pageableCaptor.capture()
		);

		assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(200);
		assertThat(history).extracting(ChatMessageResponse::getContent)
				.containsExactly("newest", "older");
	}

	private ChatMessage message(String content) {
		ChatMessage message = new ChatMessage();
		message.setSenderId(1L);
		message.setUsername("User");
		message.setContent(content);
		message.setRoomType(RoomType.GLOBAL);
		message.setRoomId("global");
		message.onCreate();
		return message;
	}
}
