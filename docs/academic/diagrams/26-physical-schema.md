# Physical Database Schema

### Explanation
Represents the actual generated SQL schema structures based on Hibernate/JPA generation.

### Source Code References
- JPA annotations like `@JoinTable(name="question_tags")`.

```mermaid
erDiagram
    users ||--o{ questions : "author_id"
    users ||--o{ answers : "author_id"
    questions ||--o{ answers : "question_id"
    
    questions ||--o{ question_tags : "question_id"
    tags ||--o{ question_tags : "tag_id"
    
    users ||--o{ chat_messages : "sender_id"
    users ||--o{ notifications : "user_id"

    users {
        bigint id PK
        varchar(255) email
        varchar(255) name
        varchar(255) password_hash
        varchar(50) role
    }

    questions {
        bigint id PK
        bigint author_id FK
        varchar(255) title
        text body
        varchar(50) status
    }

    answers {
        bigint id PK
        bigint question_id FK
        bigint author_id FK
        text body
        boolean accepted
    }

    tags {
        bigint id PK
        varchar(255) name
    }

    question_tags {
        bigint question_id PK,FK
        bigint tag_id PK,FK
    }
```
