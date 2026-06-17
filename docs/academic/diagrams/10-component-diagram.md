# Component Diagram

### Explanation
This UML Component Diagram shows the major structural modules of the DoConnect AI system and the interfaces they provide/require.

### Source Code References
- Modules: `Frontend` (React), `Main Backend` (Spring Boot REST), `Chat Service` (Spring Boot STOMP).
- Interfaces: REST APIs (`/api/*`), WebSocket endpoints (`/ws`, `/ws-chat`).

```mermaid
componentDiagram
    package "DoConnect AI Client" {
        [React SPA]
    }

    package "Main API Gateway" {
        [NGINX Reverse Proxy]
    }

    package "Core Microservices" {
        [Main Backend Service]
        [Chat Microservice]
    }

    package "External Systems" {
        [MySQL Database]
        [Google Gemini API]
    }

    [React SPA] ..> [NGINX Reverse Proxy] : HTTP/WS
    [NGINX Reverse Proxy] ..> [Main Backend Service] : HTTP /api/*
    [NGINX Reverse Proxy] ..> [Chat Microservice] : WS /ws-chat/*

    [Main Backend Service] ..> [MySQL Database] : JDBC (Port 3306)
    [Chat Microservice] ..> [MySQL Database] : JDBC (Port 3306)

    [Chat Microservice] ..> [Main Backend Service] : HTTP POST /internal/notifications

    [Main Backend Service] ..> [Google Gemini API] : HTTP REST
```
