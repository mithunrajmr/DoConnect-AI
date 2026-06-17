# Activity Diagram: AI Moderation

### Explanation
This diagram shows how content is checked for toxicity and spam before being fully visible or immediately flagged.

### Source Code References
- `AiController.checkModeration()`, `AiModerationService.java`

```mermaid
flowchart TD
    Start((Start)) --> UserPost[User Submits Text Content]
    UserPost --> CallMod[Call /moderation/check API]
    CallMod --> GeminiPrompt[Send Content to Gemini]
    GeminiPrompt --> ReturnJSON[Gemini returns JSON Analysis]
    
    ReturnJSON --> Eval{Is Toxic or<br/>Spam?}
    
    Eval -- Yes --> Flag[Set Status = FLAGGED]
    Flag --> Quarantine[Hide from Public Feed]
    Quarantine --> AdminDash[Show in Admin Dashboard]
    
    Eval -- No --> Approve[Set Status = APPROVED]
    Approve --> Publish[Visible in Global Feed]
    
    AdminDash --> AdminAction{Admin Decision}
    AdminAction -- Approve --> Publish
    AdminAction -- Delete --> DeleteRecord[Remove Record]
    
    Publish --> End((End))
    DeleteRecord --> End
```
