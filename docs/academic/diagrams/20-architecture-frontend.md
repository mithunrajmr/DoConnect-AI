# Frontend Architecture Diagram

### Explanation
This diagram outlines the component hierarchy and state management flow within the React SPA.

### Source Code References
- React components in `frontend/src/` (e.g., `App.jsx`, `AuthContext.jsx`, `pages/`, `components/`).

```mermaid
graph TD
    subgraph "React SPA Hierarchy"
        Index[main.jsx] --> App[App.jsx]
        
        App --> Context[AuthContext Provider]
        App --> Router[React Router]
        
        Context --> State[(Context State:<br/>user, token)]
        
        Router --> Pages
        
        subgraph "Pages / Routes"
            Home[HomePage]
            Login[LoginPage]
            Ask[AskPage]
            QDetail[QuestionDetailPage]
            Admin[AdminDashboard]
        end
        
        subgraph "Shared Components"
            Nav[Navbar]
            Chat[ChatWidget]
            Notif[NotificationTray]
        end
        
        Router --> Nav
        Router --> Chat
        Router --> Notif
    end
    
    Nav -.-> Context
    Home -.-> Context
    Chat -.-> Context
```
