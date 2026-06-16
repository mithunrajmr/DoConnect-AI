package com.doconnect.backend.notification;

import com.doconnect.backend.auth.AppUserDetails;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping
	public List<NotificationResponse> findAll(@AuthenticationPrincipal AppUserDetails principal) {
		return notificationService.findAll(principal.user());
	}

	@GetMapping("/unread-count")
	public UnreadCountResponse unreadCount(@AuthenticationPrincipal AppUserDetails principal) {
		return notificationService.unreadCount(principal.user());
	}

	@PutMapping("/{id}/read")
	public NotificationResponse markRead(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails principal) {
		return notificationService.markRead(id, principal.user());
	}

	@PutMapping("/read-all")
	public UnreadCountResponse markAllRead(@AuthenticationPrincipal AppUserDetails principal) {
		return notificationService.markAllRead(principal.user());
	}
}
