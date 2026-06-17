# Activity Diagram: AI Similar Question Detection

### Explanation
This activity diagram tracks the logical decision flow when the user triggers the similar question detection feature.

### Source Code References
- `RecommendationService.java` (`findSimilarQuestions`)

```mermaid
graph TD
    Start((Start)) --> View[User views Question Page]
    View --> API[Frontend calls /similar/{id}]
    API --> DB1[(Fetch Target Question)]
    DB1 --> Check{Question Exists?}
    
    Check -- No --> Err[Return 404 Not Found] --> End((End))
    Check -- Yes --> DB2[(Fetch other Questions)]
    
    DB2 --> Filter[Filter out Target Question]
    Filter --> Gemini[Send Context to Gemini API]
    
    Gemini --> Parse{Valid JSON?}
    Parse -- No --> Fallback[Return Empty List] --> Display
    Parse -- Yes --> FetchSimilar[(Fetch IDs from DB)]
    
    FetchSimilar --> Format[Construct Question DTOs]
    Format --> Display[Return 200 OK]
    Display --> End
```
