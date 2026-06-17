# Package Diagram

### Explanation
This UML package diagram illustrates the modular structure of the Spring Boot backend, showing how domains are segregated.

### Source Code References
- Directory structure of `backend/src/main/java/com/doconnect/backend`.

```mermaid
classDiagram
    namespace com_doconnect_backend {
        class auth {
            <<package>>
        }
        class user {
            <<package>>
        }
        class question {
            <<package>>
        }
        class answer {
            <<package>>
        }
        class ai {
            <<package>>
        }
        class notification {
            <<package>>
        }
        class analytics {
            <<package>>
        }
    }

    question ..> user : depends on
    answer ..> question : depends on
    answer ..> user : depends on
    ai ..> question : analyzes
    ai ..> answer : analyzes
    notification ..> user : notifies
```
