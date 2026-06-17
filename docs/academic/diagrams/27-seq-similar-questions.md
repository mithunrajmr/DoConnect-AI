# Sequence Diagram: AI Similar Question Detection

### Explanation
This sequence diagram illustrates the workflow of the system querying the backend to find semantically similar questions using the Gemini API.

### Source Code References
- `RecommendationController.java` (`@GetMapping("/similar/{questionId}")`)
- `RecommendationService.java` (`findSimilarQuestions`)
- `GeminiClient.java`

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant RecommendationController
    participant RecommendationService
    participant DB as MySQL
    participant Gemini as Google Gemini API

    User->>Frontend: View Question Page
    Frontend->>RecommendationController: GET /api/ai/recommendations/similar/{id}
    RecommendationController->>RecommendationService: findSimilarQuestions(id)
    RecommendationService->>DB: findById(id)
    DB-->>RecommendationService: Target Question Entity
    RecommendationService->>DB: findAll() (or indexed query)
    DB-->>RecommendationService: List of Other Questions
    RecommendationService->>RecommendationService: Construct Semantic Prompt
    RecommendationService->>Gemini: POST /generateContent (JSON Payload)
    Gemini-->>RecommendationService: JSON Array of Similar IDs
    RecommendationService->>DB: findAllById(Ids)
    DB-->>RecommendationService: Similar Question Entities
    RecommendationService-->>RecommendationController: List<QuestionResponse>
    RecommendationController-->>Frontend: 200 OK
    Frontend-->>User: Display "Related Questions" Panel
```
