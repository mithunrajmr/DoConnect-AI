package com.doconnect.chatservice.chat;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

	private final MessageService messageService;

	public ChatRestController(MessageService messageService) {
		this.messageService = messageService;
	}

	@GetMapping("/messages")
	public List<ChatMessageResponse> history(@RequestParam(required = false) Integer limit) {
		return messageService.getGlobalHistory(limit);
	}
}
