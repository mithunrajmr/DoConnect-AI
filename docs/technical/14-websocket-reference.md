# 14 - WebSocket & STOMP Reference

DoConnect AI uses two distinct STOMP WebSocket endpoints hosted on two separate microservices.

## 1. Notification Hub (Main API - Port 8080)

*   **Endpoint:** `ws://localhost:8080/ws/notifications`
*   **Authentication:** Requires `Authorization: Bearer <jwt>` header in the STOMP `CONNECT` frame.

### 1.1 Server-To-Client Subscriptions (Queues)
Spring resolves the `@SendToUser` destination using the principal's email address.
*   **Destination:** `/user/queue/notifications`
    *   **Payload:** `NotificationResponse` JSON.
    *   **Trigger:** Whenever an answer is posted on a user's question, or a global chat occurs.
*   **Destination:** `/user/queue/notifications/unread-count`
    *   **Payload:** `{"unreadCount": 5}`
    *   **Trigger:** On unread state changes (new notification arrives, or user clears their queue).

### 1.2 Client-To-Server Publishing
There are currently no inbound message mappings on this broker. Clients interact with notifications strictly via standard REST HTTP endpoints (`PUT /api/notifications/read-all`).

## 2. Global Chat Hub (Chat Service - Port 8090)

*   **Endpoint:** `ws://localhost:8090/ws`
*   **Authentication:** Requires `Authorization: Bearer <jwt>` header in the STOMP `CONNECT` frame. Uses a shared secret to decode the Main API's token.

### 2.1 Server-To-Client Subscriptions (Topics)
*   **Destination:** `/topic/chat/global`
    *   **Payload:** `ChatMessageResponse` JSON.
    *   **Trigger:** Broadcast whenever any user successfully sends a message to the global room.

### 2.2 Client-To-Server Publishing
*   **Destination:** `/app/chat.send`
    *   **Payload:** `{"content": "Hello World!"}`
    *   **Action:** The `ChatWebSocketController` intercepts this, saves it to the database, invokes the internal notification bridge, and broadcasts it to the `/topic/chat/global` topic.

## 3. Reconnection Strategies
The React frontend (via `useChat.js`) configures `@stomp/stompjs` with:
*   `reconnectDelay: 5000` (5 seconds)
*   `heartbeatIncoming: 10000` (10 seconds)
*   `heartbeatOutgoing: 10000` (10 seconds)
This ensures that if Tomcat restarts or a proxy drops the idle connection, the frontend automatically recovers the session without user intervention.

---
*Next Document: [15-configuration-reference.md](15-configuration-reference.md)*
