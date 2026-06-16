package com.doconnect.chatservice.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.doconnect.chatservice.security.ChatPrincipal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

	@Mock
	private MessageService messageService;

	@Mock
	private SimpMessagingTemplate messagingTemplate;

	@Mock
	private NotificationClient notificationClient;

	@Test
	void publishGlobalMessagePersistsThenBroadcasts() {
		ChatService chatService = new ChatService(messageService, messagingTemplate, notificationClient);
		ChatMessageRequest request = new ChatMessageRequest("Hello");
		ChatPrincipal sender = new ChatPrincipal(7L, "Ananya", "USER");
		ChatMessageResponse response = ChatMessageResponse.builder()
				.id(1L)
				.senderId(7L)
				.username("Ananya")
				.content("Hello")
				.roomId("global")
				.roomType(RoomType.GLOBAL)
				.moderationStatus(ModerationStatus.APPROVED)
				.createdAt(Instant.now())
				.build();

		when(messageService.saveGlobalMessage(request, sender)).thenReturn(response);

		ChatMessageResponse actual = chatService.publishGlobalMessage(request, sender);

		assertThat(actual).isSameAs(response);
		verify(messageService).saveGlobalMessage(request, sender);
		verify(messagingTemplate).convertAndSend(ChatService.GLOBAL_TOPIC, response);
		verify(notificationClient).sendChatMessageNotification(response);
	}
}
