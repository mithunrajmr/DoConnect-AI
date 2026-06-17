# Deployment Diagram

### Explanation
This UML Deployment Diagram maps software components to physical or virtual hardware nodes, typical for the projected production environment.

### Source Code References
- Presumed from standard Spring Boot + Vite + MySQL multi-service stacks (No explicit docker-compose exists in the codebase). This diagram represents the **Proposed Production Deployment Architecture**, differentiating the logical services from the recommended proxy.

```mermaid
graph TD
    subgraph "Client Node (Browser)"
        Browser[Web Browser]
    end

    subgraph "Web Server Node (Linux)"
        Nginx[Proposed NGINX Server]
        Static[Static Files: Vite Built Assets]
    end

    subgraph "App Server Node (Java 21/Linux)"
        Main[Main Backend Container<br/>Port 8080]
        Chat[Chat Service Container<br/>Port 8090]
    end

    subgraph "Database Node"
        MySQL[(MySQL 8 Server<br/>Port 3306)]
    end
    
    subgraph "Cloud Provider"
        Gemini[Google Gemini Platform]
    end

    Browser --->|"HTTPS/WSS"| Nginx
    Nginx --->|"Serves"| Static
    Nginx --->|"Proxy Pass"| Main
    Nginx --->|"Proxy Pass WS"| Chat
    
    Main --->|"JDBC"| MySQL
    Chat --->|"JDBC"| MySQL
    
    Main --->|"HTTPS"| Gemini
```
