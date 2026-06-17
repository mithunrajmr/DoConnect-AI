# DFD Level 0 (Context Diagram)

### Explanation
This diagram shows the highest level view of the DoConnect AI system, representing it as a single process interacting with external entities (User, Admin, Google Gemini API).

### Source Code References
- **System**: `DoConnect AI System` (Backend + Frontend + Chat)
- **External Entities**: `Google Gemini API` (called via `GeminiService.java`).

```mermaid
graph TD
    User[/User/]
    Admin[/Admin/]
    System((DoConnect AI<br/>System))
    Gemini[/Google Gemini API/]

    User --->|"Credentials, Questions,<br/>Answers, Chat Messages"| System
    System --->|"Feed, Notifications,<br/>Chat Streams, AI Drafts"| User

    Admin --->|"Moderation Actions,<br/>Analysis Requests"| System
    System --->|"Flagged Content,<br/>Analytics Reports"| Admin

    System --->|"Content for Analysis,<br/>Prompts"| Gemini
    Gemini --->|"Risk Scores,<br/>Generated Text, Tags"| System
```
