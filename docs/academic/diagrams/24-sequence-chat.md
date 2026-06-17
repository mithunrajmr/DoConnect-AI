# Sequence Diagram: Chat Message

### Explanation
Details the exact STOMP frame sequence when sending a chat message.

### Source Code References
- `ChatController.java` (`@MessageMapping("/chat.sendMessage")`).

```mermaid
sequenceDiagram
    participant UserA
    participant UserB
    participant Broker as STOMP Broker
    participant ChatController as ChatController
    participant DB as MySQL

    UserA->>Broker: CONNECT
    UserB->>Broker: CONNECT
    UserA->>Broker: SUBSCRIBE /topic/global
    UserB->>Broker: SUBSCRIBE /topic/global
    
    UserA->>Broker: SEND /app/chat.sendMessage (payload: "Hello")
    Broker->>ChatController: Route to @MessageMapping
    ChatController->>DB: Save ChatMessage(UserA, "Hello")
    DB-->>ChatController: Saved Entity
    ChatController->>Broker: return ChatMessage
    Broker->>Broker: convertAndSend("/topic/global")
    Broker-->>UserA: MESSAGE /topic/global ("Hello")
    Broker-->>UserB: MESSAGE /topic/global ("Hello")
```
