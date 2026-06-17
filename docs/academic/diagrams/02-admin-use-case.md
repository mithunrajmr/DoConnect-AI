# Admin Use Case Diagram

### Explanation
This diagram zeroes in on the administrative capabilities, specifically content moderation and platform analytics.

### Source Code References
- **Roles**: `UserRole.ADMIN`
- **Features**: `AiModerationService.java`, `AdminAnalyticsService.java` (Assumed based on previous analysis of the Admin Dashboard).

```mermaid
graph LR
    Admin([Admin])
    
    subgraph Admin Functions
        QM(Review Flagged Questions)
        AM(Review Flagged Answers)
        Del(Delete Violating Content)
        VA(View Platform Analytics)
        SA(View Sentiment Analysis)
    end
    
    Admin --- QM
    Admin --- AM
    Admin --- Del
    Admin --- VA
    Admin --- SA
```
