# Backend Architecture Diagram

### Explanation
This component diagram focuses exclusively on the inner structure of the `backend` Spring Boot application.

### Source Code References
- Packages: `com.doconnect.backend.auth`, `question`, `answer`, `ai`, `notification`.

```mermaid
graph TD
    subgraph Spring Boot ApplicationContext
        subgraph Web Layer
            AC[AuthController]
            QC[QuestionController]
            AnC[AnswerController]
            AiC[AiController]
        end
        
        subgraph Security Layer
            Filter[JwtAuthenticationFilter]
            Manager[AuthenticationManager]
        end
        
        subgraph Service Layer
            AS[AuthService]
            QS[QuestionService]
            AnS[AnswerService]
            AiS[Gemini/Recommendation Services]
        end
        
        subgraph Data Access Layer
            UR[UserRepository]
            QR[QuestionRepository]
            AnR[AnswerRepository]
        end
    end
    
    Client --> Filter
    Filter --> Manager
    Manager --> AC
    Manager --> QC
    Manager --> AnC
    Manager --> AiC
    
    AC --> AS
    QC --> QS
    AnC --> AnS
    AiC --> AiS
    
    AS --> UR
    QS --> QR
    AnS --> AnR
```
