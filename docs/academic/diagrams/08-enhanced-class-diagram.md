# Enhanced Class Diagram

### Explanation
This is a strict UML class diagram showing attributes, methods, visibility modifiers, and relationships for the core entity layer.

### Source Code References
- Entity classes in `com.doconnect.backend.*`

```mermaid
classDiagram
    class User {
        -Long id
        -String name
        -String email
        -String passwordHash
        -UserRole role
        -Instant createdAt
        -Instant updatedAt
        +Long getId()
        +String getEmail()
        +UserRole getRole()
        ~void onCreate()
        ~void onUpdate()
    }

    class Question {
        -Long id
        -String title
        -String body
        -QuestionStatus status
        -ModerationStatus moderationStatus
        -Long acceptedAnswerId
        +Long getId()
        +void setTags(Set~Tag~ tags)
        +void setStatus(QuestionStatus)
    }

    class Answer {
        -Long id
        -String body
        -boolean accepted
        -boolean aiGenerated
        -BigDecimal aiConfidence
        +void setAccepted(boolean)
        +boolean isAiGenerated()
    }

    class Tag {
        -Long id
        -String name
        -String displayName
        -long usageCount
        +void incrementUsageCount()
    }

    class UserRole {
        <<enumeration>>
        USER
        ADMIN
    }
    
    class QuestionStatus {
        <<enumeration>>
        OPEN
        SOLVED
        CLOSED
    }

    User "1" -- "*" Question : authors
    User "1" -- "*" Answer : authors
    Question "1" *-- "*" Answer : contains
    Question "*" -- "*" Tag : tagged with
    User ..> UserRole : uses
    Question ..> QuestionStatus : uses
```
