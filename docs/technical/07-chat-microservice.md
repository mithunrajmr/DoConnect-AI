# 07 - Chat Microservice Architecture

## 1. Overview
The Chat Service (Port 8090) is a dedicated Spring Boot application running alongside the Main API. Its sole responsibility is to handle the high-throughput, highly concurrent real-time STOMP messaging for the global community chat.

## 2. Why a Microservice?
Global chat rooms present a unique architectural challenge. A thousand users sitting idly in a chat room means a thousand open, persistent TCP connections (WebSockets). If these connections were hosted on the Main API:
1.  **Thread Starvation:** WebSocket management consumes threads and memory.
2.  **Deployment Friction:** Restarting the Main API to deploy a new feature (like a new Q&A endpoint) would instantly drop all active chat connections, resulting in a poor user experience.
By isolating Chat, it can scale horizontally and fail independently of the core business logic.

## 3. WebSocket Configuration & STOMP

The service utilizes Spring's `spring-boot-starter-websocket` to configure a STOMP broker.

### 3.1 Broker Topology
*   **Endpoint:** `/ws` (Where the client initiates the handshake).
*   **Application Destination Prefix:** `/app` (Where clients send messages, e.g., `/app/chat.send`).
*   **Broker Destination Prefix:** `/topic` (Where clients subscribe to receive messages, e.g., `/topic/chat/global`).

### 3.2 The Message Flow
1.  **Publishing:** The frontend publishes a JSON payload to `/app/chat.send`.
2.  **Controller:** This maps to `@MessageMapping("/chat.send")` in the `ChatWebSocketController`.
3.  **Processing:** The controller passes the request and the `ChatPrincipal` (authenticated user) to the `ChatService`.
4.  **Persistence:** The message is saved to the `doconnect_chat` MySQL database.
5.  **Broadcasting:** The service uses `SimpMessagingTemplate` to push the persisted message out to `/topic/chat/global`, which is received by all connected clients.

## 4. Security & Authentication

Because the Chat Service is physically separated from the Main API, it cannot directly access the `users` table to verify passwords.

### 4.1 Shared JWT Secret
The Chat Service relies on the exact same `app.jwt.secret` defined in the Main API.
When a user connects, the `WebSocketAuthChannelInterceptor` extracts the Bearer token from the STOMP `CONNECT` frame header. It decodes the token locally, trusting the cryptographic signature. It extracts the `userId`, `name`, and `role` directly from the token claims to construct a local `ChatPrincipal`.

## 5. Bridging the Gap: The Notification Client

When a user is actively looking at a Q&A thread on the Main API (and thus not connected to the Chat Service WebSocket), they still need to know if someone sent a chat message.

### 5.1 Internal REST Client
The Chat Service utilizes Spring's `RestClient` via the `NotificationClient` class.
After saving a chat message, it makes a synchronous HTTP POST call to the Main API: `POST http://localhost:8080/internal/notifications/chat-message`.

### 5.2 The Security Token
To prevent malicious actors from triggering fake notifications by hitting that Main API endpoint directly, the `NotificationClient` injects an `X-Internal-Token` header. This token (`app.notifications.internal-token`) must precisely match between the two services.

*Note on Reliability:* The internal HTTP call is wrapped in a `try/catch`. If the Main API is offline, the chat message still succeeds and is broadcasted to active chat listeners. Real-time notifications are treated as "best effort," ensuring the core chat loop remains highly available.

---
*Next Document: [08-ai-features.md](08-ai-features.md)*
