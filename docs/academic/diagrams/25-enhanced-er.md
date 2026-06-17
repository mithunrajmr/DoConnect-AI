# Enhanced ER Diagram

### Explanation
An Enhanced Entity-Relationship (EER) diagram detailing entities, attributes, and precise cardinalities.

### Source Code References
- `@Entity` classes (`User`, `Question`, `Answer`, `Tag`, `Notification`, `ChatMessage`).

```mermaid
erDiagram
    USER ||--o{ QUESTION : "writes (1:N)"
    USER ||--o{ ANSWER : "provides (1:N)"
    USER ||--o{ NOTIFICATION : "receives (1:N)"
    USER ||--o{ CHAT_MESSAGE : "sends (1:N)"
    
    QUESTION ||--o{ ANSWER : "contains (1:N)"
    QUESTION }o--o{ TAG : "has (M:N)"
    
    USER {
        Long id PK
        String name
        String email UK
        String password_hash
        String role
    }
    
    QUESTION {
        Long id PK
        Long author_id FK
        String title
        Text body
        String status
        String moderation_status
        Long accepted_answer_id
    }
    
    ANSWER {
        Long id PK
        Long question_id FK
        Long author_id FK
        Text body
        Boolean is_accepted
        Boolean ai_generated
    }
    
    TAG {
        Long id PK
        String name UK
    }
    
    NOTIFICATION {
        Long id PK
        Long user_id FK
        String message
        Boolean is_read
    }
    
    CHAT_MESSAGE {
        Long id PK
        Long sender_id FK
        Text content
        Timestamp sent_at
    }
```
