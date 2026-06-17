# Activity Diagram: Answer Question

### Explanation
This activity diagram maps the process of a user drafting an answer, including the "Improve Draft" and "Generate Suggestion" AI paths.

### Source Code References
- `AnswerController.create()`, `AiController.improveDraft()`, `AiController.suggestAnswer()`.

```mermaid
flowchart TD
    Start((Start)) --> ViewQ[User Views Question]
    ViewQ --> Opt{Choose AI<br/>Help?}
    
    Opt -- Suggestion --> GenSug[Call /suggest-answer API]
    GenSug --> Gemini[Gemini drafts technical answer]
    Gemini --> Editor[Draft injected to Editor]
    
    Opt -- None --> DraftMan[User writes draft manually]
    DraftMan --> Editor
    
    Editor --> OptImp{Improve<br/>Draft?}
    OptImp -- Yes --> Improve[Call /improve-draft API]
    Improve --> Gemini2[Gemini formats markdown & grammar]
    Gemini2 --> Editor
    
    OptImp -- No --> SubmitAns[User clicks Submit]
    SubmitAns --> SaveDB[Save Answer to DB]
    SaveDB --> Notify[Send STOMP Notification to Author]
    Notify --> UpdateUI[Update UI with new Answer]
    UpdateUI --> End((End))
```
