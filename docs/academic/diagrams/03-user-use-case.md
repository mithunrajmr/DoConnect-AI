# User Use Case Diagram

### Explanation
This diagram highlights the standard authenticated user's interactions with the platform, showing `«include»` and `«extend»` relationships where AI features optionally assist the core workflow.

### Source Code References
- **Controllers**: `QuestionController`, `AnswerController`, `AiController`, `RecommendationController`.
- **Features**: `predictTags`, `suggestAnswer`, `checkSimilarQuestions`.

```mermaid
graph LR
    User([User])
    
    subgraph User Functions
        Auth(Login/Register)
        AQ(Ask Question)
        Ans(Post Answer)
        Chat(Send Global Chat Message)
        Acc(Accept Best Answer)
        
        %% AI Extensions
        Dup(Check Duplicates)
        Tag(Predict Tags)
        Sug(Suggest AI Answer)
        Imp(Improve Draft)
    end
    
    User --- Auth
    User --- AQ
    User --- Ans
    User --- Chat
    User --- Acc
    
    AQ -.->|<<extend>>| Dup
    AQ -.->|<<extend>>| Tag
    Ans -.->|<<extend>>| Sug
    Ans -.->|<<extend>>| Imp
```
