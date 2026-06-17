# DFD Level 1

### Explanation
This diagram breaks down the main system into its primary sub-processes (Auth, Q&A Management, Chat, AI Integration) and shows the data stores.

### Source Code References
- **Processes**: Mapped to `AuthController`, `QuestionController`/`AnswerController`, `ChatController`, `AiController`.
- **Data Stores**: `users`, `questions`, `answers`, `chat_messages` tables.

```mermaid
graph TD
    User[/User/]
    Gemini[/Google Gemini API/]

    P1((1.0<br/>Authentication<br/>Process))
    P2((2.0<br/>Q&A Management<br/>Process))
    P3((3.0<br/>Chat Messaging<br/>Process))
    P4((4.0<br/>AI Integration<br/>Process))

    D1[(D1: Users)]
    D2[(D2: Questions & Answers)]
    D3[(D3: Chat Messages)]

    User --->|"Login/Register"| P1
    P1 --->|"JWT Token"| User
    P1 <---> D1

    User --->|"Post Question/Answer"| P2
    P2 --->|"Q&A Feed"| User
    P2 <---> D2

    User --->|"Chat Message"| P3
    P3 --->|"Broadcast Message"| User
    P3 <---> D3

    P2 --->|"Check Toxicity/Drafts"| P4
    P4 --->|"Prompts"| Gemini
    Gemini --->|"AI Responses"| P4
    P4 --->|"AI Results"| P2
```
