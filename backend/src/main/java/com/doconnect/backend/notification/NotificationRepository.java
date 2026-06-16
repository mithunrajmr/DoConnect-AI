package com.doconnect.backend.notification;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByRecipientIdOrderByUpdatedAtDesc(Long recipientId);

	long countByRecipientIdAndReadFalse(Long recipientId);

	Optional<Notification> findByIdAndRecipientId(Long id, Long recipientId);

	Optional<Notification> findFirstByRecipientIdAndTypeAndSourceKeyAndReadFalse(Long recipientId, NotificationType type, String sourceKey);

	@Modifying
	@Query("""
			update Notification n
			set n.read = true
			where n.recipient.id = :recipientId and n.read = false
			""")
	int markAllAsReadByRecipientId(Long recipientId);
}
