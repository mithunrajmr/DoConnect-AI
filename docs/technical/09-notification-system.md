# 09 - Notification System

## 1. Overview
The Notification System provides real-time, personalized alerts to users without requiring page refreshes. It handles two primary event types:
1.  **Q&A Events:** "Someone answered your question."
2.  **Chat Events:** "New message in the global chat."

## 2. Real-Time Topology

Notifications are pushed directly to the browser via STOMP WebSockets hosted on the Main API (Port 8080).

### 2.1 Connection and Security
*   **Endpoint:** `ws://localhost:8080/ws/notifications`
*   **Authentication:** Like the Chat Service, the Main API intercepts the STOMP `CONNECT` frame and validates the JWT Bearer token using `NotificationWebSocketAuthChannelInterceptor`.

### 2.2 User-Specific Queues
To ensure Alice doesn't receive Bob's notifications, the system uses Spring's `@SendToUser` paradigm.
*   The client subscribes to `/user/queue/notifications`.
*   Spring internally resolves the authenticated `Principal` (user email) and translates this into a unique session queue (e.g., `/queue/notifications-user123`).

## 3. The Notification Lifecycle

### 3.1 Triggering an Event
When a state change occurs (e.g., `NotificationService.notifyQuestionAnswered`), the backend executes two actions:
1.  **Persistence:** Saves a `Notification` entity to the MySQL database, setting `read_flag = false`.
2.  **Push:** Calls `NotificationRealtimeService.sendToUser()`.

### 3.2 Spam Prevention (`source_key` & `occurrence_count`)
If ten people answer a question in five minutes, the author shouldn't receive ten separate rows in their notification dropdown.
The service uses a `source_key` (e.g., `chat:global` or `question:123`).
If an unread notification with the same `source_key` already exists, the service increments its `occurrence_count` and updates the text (e.g., "5 new messages") instead of inserting a new database row.

### 3.3 The Real-Time Payload
The `NotificationRealtimeService` pushes two JSON payloads to the user:
1.  **The Notification Data:** Pushed to `/queue/notifications`. Contains the message, target URL, and occurrence count.
2.  **The Unread Badge Count:** Pushed to `/queue/notifications/unread-count`. This tells the frontend UI exactly what number to display over the bell icon.

## 4. Chat Notification Bridge

Because the Chat Service (Port 8090) and Main API (Port 8080) are decoupled, chat messages require a special bridge.

1.  A user sends a message on the Chat Service.
2.  The Chat Service's `NotificationClient` fires an HTTP POST to `http://localhost:8080/internal/notifications/chat-message`.
3.  This request includes the shared secret `X-Internal-Token`.
4.  The Main API's `InternalNotificationController` receives the event.
5.  It calls `NotificationService.notifyChatMessage`, which loops through all registered users (excluding the sender), upserts their chat notification row, and fires the STOMP message to their active browsers.

## 5. Acknowledgment Flow
When a user clicks the bell icon in the React UI:
1.  The frontend calls `POST /api/notifications/mark-all-read`.
2.  The Main API updates the MySQL database (`read_flag = true`).
3.  The Main API pushes a `0` to the user's `/queue/notifications/unread-count` topic, instantly clearing the badge across all open tabs the user might have.

---
*Next Document: [10-question-answer-lifecycle.md](10-question-answer-lifecycle.md)*
