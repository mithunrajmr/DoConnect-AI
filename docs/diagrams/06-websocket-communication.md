# WebSocket Communication Diagram

This diagram shows the STOMP topics and messaging flow between the Frontend and the Chat Service.

```mermaid
graph TD
    subgraph Frontend [React SPA Client]
        UI[Global Chat UI]
        STOMP_JS[STOMP.js Client]
    end

    subgraph Chat Service [Port 8090]
        WS_Endpoint[/ws-chat]
        Broker[(Simple Message Broker)]
        Topic_Global[/topic/global]
        Controller[ChatController]
        DB[(MySQL - chat_messages)]
    end

    UI --> |Sends Message| STOMP_JS
    STOMP_JS --> |"SEND /app/chat.sendMessage"| WS_Endpoint
    WS_Endpoint --> Controller
    Controller --> |Saves to DB| DB
    Controller --> |Routes Message| Broker
    Broker --> |Broadcasts| Topic_Global
    Topic_Global --> |"MESSAGE"| STOMP_JS
    STOMP_JS --> |Updates| UI
```
