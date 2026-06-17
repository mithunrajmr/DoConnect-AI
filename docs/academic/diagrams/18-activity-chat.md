# Activity Diagram: Chat Flow

### Explanation
This diagram illustrates the event loop for a user sending and receiving global chat messages.

### Source Code References
- `ChatController.sendMessage()`, `NotificationClient.java`

```mermaid
flowchart TD
    Start((Start)) --> Connect[Frontend Connects to WS]
    Connect --> Auth[Validate JWT Token]
    Auth --> Subscribe[Subscribe to /topic/global]
    
    Subscribe --> Wait[Wait for User Input]
    Wait --> Type[User types message]
    Type --> Send[STOMP SEND /app/chat.sendMessage]
    
    Send --> Controller[ChatController handles message]
    Controller --> Save[Save ChatMessage Entity]
    Save --> Broadcast[Broker convertsAndSends to /topic/global]
    Broadcast --> ClientRecv[All Clients Receive MESSAGE frame]
    ClientRecv --> UpdateUI[Render new message]
    
    Save --> Notify[Cross-Service call to Main Backend]
    Notify --> Wait
```
