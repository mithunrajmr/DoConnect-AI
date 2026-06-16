# 15 - Configuration & Environment Variables

## 1. Overview
The platform uses `.env` files to inject secrets and configuration into Spring Boot's `application.properties` and Vite's build process.

## 2. Main API (`backend/src/main/resources/application.properties`)

| Environment Variable | Property Key | Purpose |
| :--- | :--- | :--- |
| `DB_URL` | `spring.datasource.url` | JDBC URL for the `doconnect_ai` schema. |
| `DB_USER` | `spring.datasource.username` | MySQL Username. |
| `DB_PASSWORD` | `spring.datasource.password` | MySQL Password. |
| `JWT_SECRET` | `app.jwt.secret` | The HMAC-SHA256 secret. Must be 32+ characters. |
| `GEMINI_API_KEY` | `app.gemini.api-key` | Google AI Studio Key. Required for all AI features. |
| `NOTIFICATION_INTERNAL_TOKEN` | `app.notifications.internal-token` | The cryptographic shared secret allowing the Chat Service to trigger notifications. |

## 3. Chat Service (`chat-service/src/main/resources/application.properties`)

| Environment Variable | Property Key | Purpose |
| :--- | :--- | :--- |
| `DB_URL_CHAT` | `spring.datasource.url` | JDBC URL for the `doconnect_chat` schema. |
| `DB_USER` | `spring.datasource.username` | MySQL Username. |
| `DB_PASSWORD` | `spring.datasource.password` | MySQL Password. |
| `JWT_SECRET` | `app.jwt.secret` | Must perfectly match the `JWT_SECRET` of the Main API. |
| `NOTIFICATION_INTERNAL_TOKEN` | `app.notifications.internal-token` | Used to authenticate the POST request to the Main API. Must perfectly match the Main API's token. |
| `MAIN_BACKEND_URL` | `app.notifications.backend-url` | Default: `http://localhost:8080`. Target for internal RestClient. |

## 4. Frontend (`frontend/.env`)

In Vite, environment variables must be prefixed with `VITE_` to be exposed to the client bundle.

| Environment Variable | Purpose |
| :--- | :--- |
| `VITE_API_URL` | Used by Axios to route REST requests (usually handled by Vite proxy in dev). |
| `VITE_CHAT_WS_URL` | The raw WebSocket URL for the Chat Service (e.g., `ws://localhost:8090/ws`). |

## 5. Security Warning
*   Never commit `.env` files to version control.
*   If `JWT_SECRET` is compromised, attackers can forge admin tokens.
*   If `NOTIFICATION_INTERNAL_TOKEN` is compromised, attackers can bypass the gateway and flood the notification queue.

---
*Next Document: [16-testing-guide.md](16-testing-guide.md)*
