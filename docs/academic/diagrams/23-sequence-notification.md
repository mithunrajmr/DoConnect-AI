# Sequence Diagram: Notification Delivery

### Explanation
Illustrates the inter-service communication required to deliver a real-time notification when an event occurs in the main backend.

### Source Code References
- `NotificationClient.java` (Main), `InternalNotificationController.java` (Chat).

```mermaid
sequenceDiagram
    participant Main as Main Backend API
    participant Client as NotificationClient
    participant Chat as Chat Microservice
    participant Broker as STOMP Broker
    participant User as User (Browser)

    User->>Broker: CONNECT /ws-chat
    User->>Broker: SUBSCRIBE /user/queue/notifications
    
    Note over Main: Event Occurs (e.g., Answer Posted)
    Main->>Client: sendNotification(userId, message)
    Client->>Chat: POST /internal/notifications<br/>Header: X-Internal-Token
    Chat->>Chat: Verify Internal Token
    Chat->>Broker: convertAndSendToUser(userId, "/queue/notifications", msg)
    Broker-->>User: MESSAGE Frame
```
