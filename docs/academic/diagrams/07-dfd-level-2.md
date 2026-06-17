# DFD Level 2 (Question Management)

### Explanation
This diagram explodes the "2.0 Q&A Management" process from Level 1 to show the specific data flows inside question creation and viewing.

### Source Code References
- **Processes**: `QuestionController.create()`, `QuestionController.findAll()`, `QuestionService.recordView()`.

```mermaid
graph TD
    User[/User/]
    P4((4.0 AI Integration))

    P21((2.1<br/>Validate & Save<br/>Question))
    P22((2.2<br/>Fetch Feed))
    P23((2.3<br/>Record View<br/>Count))

    D2[(D2: Questions)]
    D4[(D4: Tags)]

    User -- "Question DTO" --> P21
    P21 -- "Predict Tags Request" --> P4
    P4 -- "Suggested Tags" --> P21
    P21 -- "Insert" --> D2
    P21 -- "Map" --> D4

    User -- "Request Feed" --> P22
    D2 -- "Select" --> P22
    P22 -- "Paginated List" --> User

    User -- "Click Question" --> P23
    P23 -- "Update View Count" --> D2
```
