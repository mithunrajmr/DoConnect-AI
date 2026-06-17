# Chat Microservice Architecture Diagram

### Explanation
This highlights the STOMP message broker configuration within the isolated `chat-service`.

### Source Code References
- `WebSocketConfig.java`, `ChatController.java`, `InternalNotificationController.java`.

```mermaid
graph TD
    subgraph "Chat Microservice (Port 8090)"
        subgraph "STOMP Over WebSocket"
            WS[WebSocket Endpoint: /ws-chat]
            Broker[Simple In-Memory Message Broker]
        end
        
        subgraph "Controllers"
            CC[ChatController]
            INC[InternalNotificationController (REST)]
        end
        
        subgraph "Security"
            Filter[JwtChannelInterceptor]
        end
    end
    
    Client[React Frontend] -- "CONNECT /ws-chat" --> WS
    WS --> Filter
    Filter -- "Valid JWT" --> Broker
    
    Client -- "SEND /app/chat.sendMessage" --> CC
    CC -- "Saves to DB" --> DB[(MySQL DB)]
    CC -- "Sends to Broker" --> Broker
    
    Broker -- "MESSAGE /topic/global" --> Client
    
    MainAPI[Main Backend] -- "POST /internal/notifications" --> INC
    INC -- "Sends to Broker" --> Broker
    Broker -- "MESSAGE /queue/notifications-{userId}" --> Client
```
