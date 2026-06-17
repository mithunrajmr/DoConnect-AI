# AI Integration Architecture Diagram

### Explanation
Details the integration layer between the Spring Boot application and the Google Gemini API.

### Source Code References
- `GeminiService.java`, `AiModerationService.java`, `AiAnswerAssistantService.java`.

```mermaid
graph TD
    subgraph "DoConnect Core"
        QC[QuestionController]
        AnC[AnswerController]
    end

    subgraph "AI Subsystem"
        Mod[AiModerationService]
        Asst[AiAnswerAssistantService]
        Rec[RecommendationController]
        
        GemClient[GeminiClient / RestTemplate]
    end
    
    subgraph "Google Cloud"
        API[Gemini 2.0 Flash API]
    end

    QC --> Mod
    AnC --> Asst
    Rec --> GemClient
    Mod --> GemClient
    Asst --> GemClient
    
    GemClient -- "HTTP POST (JSON Prompt)" --> API
    API -- "JSON Response" --> GemClient
```
