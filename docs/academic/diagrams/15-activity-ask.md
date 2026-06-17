# Activity Diagram: Ask Question

### Explanation
This activity diagram tracks the user and system activities when asking a new technical question, including optional AI assistance paths.

### Source Code References
- `QuestionController.create()`, `RecommendationController.predictTags()`, `AskPage.jsx`.

```mermaid
flowchart TD
    Start((Start)) --> EnterDetails[User enters Title and Body]
    EnterDetails --> OptAI{User requests<br/>AI Tags?}
    
    OptAI -- Yes --> FetchTags[Call /predict-tags API]
    FetchTags --> GeminiTags[Gemini Generates Tags]
    GeminiTags --> PopTags[Populate Tag Input]
    PopTags --> ReviewTags[User Reviews/Edits Tags]
    ReviewTags --> SubmitQ
    
    OptAI -- No --> ManualTags[User enters tags manually]
    ManualTags --> SubmitQ[User clicks Post Question]
    
    SubmitQ --> Valid{Is Valid?}
    Valid -- No --> ShowErr[Show Validation Errors]
    ShowErr --> EnterDetails
    
    Valid -- Yes --> SaveDB[Save Question to Database]
    SaveDB --> Redir[Redirect to Question Page]
    Redir --> End((End))
```
