# DoConnect AI

An AI-powered collaborative discussion and knowledge platform — a full-stack capstone project combining a Q&A community (Stack Overflow-style) with a real-time global chat room, AI assistance, content moderation, and an analytics dashboard.

---

## Features

| Module | Description |
|---|---|
| **Authentication** | Register, login, JWT-secured sessions, role-based access (USER / MODERATOR / ADMIN) |
| **Questions & Answers** | Post questions with tags, browse a feed, submit answers, accept answers, delete own content |
| **AI Answer Assistant** | Gemini-powered answer suggestions, discussion summarisation, draft improvement |
| **AI Tag Prediction** | Automatically predict relevant tags as you type a question |
| **AI Similar Questions** | Surface related questions on each question detail page |
| **Real-time Chat** | Global STOMP/WebSocket chat room with message history |
| **Real-time Notifications** | WebSocket push notifications when your question receives an answer |
| **Content Moderation** | Admin moderation dashboard with AI-assisted flagging |
| **Analytics Dashboard** | View counts, question activity, tag usage, and answer statistics |
| **Profile Management** | Update display name and view own activity |

---

## Architecture

```
┌─────────────────────────────┐
│        React Frontend        │  :5173  (Vite dev server)
│    Vite + React Router +     │
│    Tailwind + STOMP client   │
└────────────┬────────────────┘
             │  REST + WebSocket (proxied via Vite in dev)
    ┌────────┴────────┐    ┌──────────────────────────┐
    │  Main Backend   │    │     Chat Microservice     │
    │  Spring Boot    │◄───│     Spring Boot           │
    │  :8080          │    │     :8090                 │
    │                 │    │                           │
    │  REST API       │    │  WebSocket/STOMP (/ws)    │
    │  WebSocket (/ws/│    │  REST history endpoint    │
    │  notifications) │    │  Forwards chat events ──► │
    │  Gemini AI      │    │  to backend /internal     │
    └────────┬────────┘    └──────────────────────────┘
             │
    ┌────────┴────────┐
    │    MySQL 8      │
    │  doconnect_ai   │  ← main schema
    │  doconnect_chat │  ← chat messages
    └─────────────────┘
```

**Key design decisions:**
- Vite's dev proxy routes `/api` → backend `:8080` and `/chat` → chat-service `:8090` — no CORS configuration needed in development.
- Both services share the **same JWT secret** so the chat service can validate tokens issued by the main backend.
- An internal HTTP token (`X-Internal-Token`) is used for chat-service → backend notification calls; this channel is not exposed to the public internet.

---

## Tech Stack

### Backend (`backend/`)
| Dependency | Version |
|---|---|
| Java | 21 |
| Spring Boot | 3.5.x |
| Spring Security | (included) |
| Spring Data JPA | (included) |
| Spring WebSocket + STOMP | (included) |
| MySQL Connector/J | runtime |
| Lombok | (annotation processor) |
| H2 | test scope |

### Chat Service (`chat-service/`)
Shares the same stack as the backend; no Lombok dependency.

### Frontend (`frontend/`)
| Dependency | Version |
|---|---|
| React | 19 |
| Vite | 8 |
| React Router | 7 |
| Axios | 1.x |
| @stomp/stompjs | 7 |
| Tailwind CSS | 4 |
| Lucide React | 1.x |

### AI
- Google Gemini API (`gemini-2.0-flash` or configured model)

---

## Prerequisites

| Tool | Minimum Version | Notes |
|---|---|---|
| Java JDK | 21 | Tested with OpenJDK 21 |
| Maven | 3.9 | Or use the included `mvnw` wrapper |
| Node.js | 20 (24 recommended) | LTS |
| npm | 10+ | Bundled with Node |
| MySQL | 8.0 | Create two schemas before starting |

---

## Database Setup

Connect to your MySQL instance and run:

```sql
CREATE DATABASE doconnect_ai
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE DATABASE doconnect_chat
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Grant your application user access (replace `appuser` / `apppassword`):

```sql
CREATE USER 'appuser'@'localhost' IDENTIFIED BY 'apppassword';
GRANT ALL PRIVILEGES ON doconnect_ai.* TO 'appuser'@'localhost';
GRANT ALL PRIVILEGES ON doconnect_chat.* TO 'appuser'@'localhost';
FLUSH PRIVILEGES;
```

Tables are created automatically on first start via Hibernate (`ddl-auto=update`).
**In production, set `JPA_DDL_AUTO=validate` and manage schema changes manually.**

---

## Environment Variables

### Backend (`backend/`)

Copy `backend/.env.example` and fill in the values.

| Variable | Required | Default (dev only) | Description |
|---|---|---|---|
| `SERVER_PORT` | No | `8080` | HTTP port |
| `DB_URL` | Yes | `jdbc:mysql://localhost:3306/doconnect_ai?...` | JDBC connection URL |
| `DB_USERNAME` | Yes | `root` | MySQL username |
| `DB_PASSWORD` | Yes | `root` | MySQL password |
| `JPA_DDL_AUTO` | No | `update` | Hibernate DDL strategy — use `validate` in prod |
| `JWT_SECRET` | **Yes** | _(none — app will fail to start)_ | HS256 signing secret, min 32 chars |
| `JWT_EXPIRATION_MINUTES` | No | `1440` | Token lifetime in minutes |
| `NOTIFICATION_ALLOWED_ORIGINS` | Yes | `http://localhost:5173` | Comma-separated WebSocket allowed origins |
| `NOTIFICATION_INTERNAL_TOKEN` | **Yes** | _(none — app will fail to start)_ | Shared secret for chat-service → backend calls |
| `GEMINI_API_KEY` | Yes | _(empty — AI features disabled)_ | Google AI Studio API key |
| `GEMINI_MODEL` | No | `gemini-2.0-flash` | Gemini model name |
| `GEMINI_BASE_URL` | No | `https://generativelanguage.googleapis.com` | Gemini API base URL |
| `GEMINI_TIMEOUT_SECONDS` | No | `30` | Request timeout |
| `GEMINI_TEMPERATURE` | No | `0.35` | Model temperature (0–1) |
| `GEMINI_MAX_OUTPUT_TOKENS` | No | `1200` | Maximum tokens in AI responses |

### Chat Service (`chat-service/`)

Copy `chat-service/.env.example` and fill in the values.

| Variable | Required | Default (dev only) | Description |
|---|---|---|---|
| `CHAT_SERVER_PORT` | No | `8090` | HTTP/WS port |
| `CHAT_DB_URL` | Yes | `jdbc:mysql://localhost:3306/doconnect_chat?...` | JDBC connection URL |
| `CHAT_DB_USERNAME` | No | _(falls back to `DB_USERNAME`)_ | MySQL username |
| `CHAT_DB_PASSWORD` | No | _(falls back to `DB_PASSWORD`)_ | MySQL password |
| `CHAT_JPA_DDL_AUTO` | No | `update` | Hibernate DDL strategy |
| `JWT_SECRET` | **Yes** | _(must match backend)_ | Same secret as the main backend |
| `CHAT_ALLOWED_ORIGINS` | Yes | `http://localhost:5173` | Comma-separated WebSocket allowed origins |
| `CHAT_GLOBAL_ROOM_ID` | No | `global` | Room identifier |
| `NOTIFICATION_BACKEND_URL` | Yes | `http://localhost:8080` | URL of the main backend (for forwarding events) |
| `NOTIFICATION_INTERNAL_TOKEN` | **Yes** | _(must match backend)_ | Same internal token as the main backend |

### Frontend (`frontend/`)

Copy `frontend/.env.example` and fill in the values.

| Variable | Required | Default | Description |
|---|---|---|---|
| `VITE_NOTIFY_WS_URL` | Yes in prod | _(uses `localhost:8080` fallback)_ | Full WebSocket URL for the notification endpoint, e.g. `wss://api.example.com/ws/notifications` |

---

## Running Locally

### 1 — Backend

```bash
cd backend

# Option A: Maven wrapper (no Maven installation required)
./mvnw spring-boot:run \
  -Dspring-boot.run.jvmArguments="\
    -DJWT_SECRET=your-local-dev-secret-32-chars-min \
    -DNOTIFICATION_INTERNAL_TOKEN=your-local-token \
    -DGEMINI_API_KEY=your-gemini-key \
    -DDB_USERNAME=appuser \
    -DDB_PASSWORD=apppassword"

# Option B: Set env vars in your shell first, then run
export JWT_SECRET=your-local-dev-secret-32-chars-min
export NOTIFICATION_INTERNAL_TOKEN=your-local-token
export GEMINI_API_KEY=your-gemini-key
export DB_USERNAME=appuser
export DB_PASSWORD=apppassword
./mvnw spring-boot:run
```

The backend starts on **http://localhost:8080**.

### 2 — Chat Service

```bash
cd chat-service

export JWT_SECRET=your-local-dev-secret-32-chars-min   # same as backend
export NOTIFICATION_INTERNAL_TOKEN=your-local-token    # same as backend
export DB_USERNAME=appuser
export DB_PASSWORD=apppassword
./mvnw spring-boot:run   # uses parent mvnw if no wrapper present
```

> **Note:** The chat-service does not include a Maven wrapper. Use system Maven (`mvn`) or copy the `mvnw` / `.mvn/` from the backend into `chat-service/`.

The chat service starts on **http://localhost:8090**.

### 3 — Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on **http://localhost:5173** with hot-module replacement.
Vite's dev proxy automatically routes API and WebSocket calls to the correct services.

---

## Build for Production

### Backend / Chat Service

```bash
cd backend
./mvnw clean package -DskipTests
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### Frontend

```bash
cd frontend
npm run build
# Output is in frontend/dist/ — serve with any static file server or CDN
```

---

## Running Tests

```bash
# Backend unit + integration tests (uses H2 in-memory)
cd backend && ./mvnw test

# Chat service tests
cd chat-service && mvn test
```

---

## Project Structure

```
DoConnect-AI/
├── backend/              Spring Boot main API
│   ├── src/main/java/com/doconnect/backend/
│   │   ├── ai/           Gemini AI services
│   │   ├── analytics/    Analytics endpoints
│   │   ├── answer/       Answer CRUD
│   │   ├── auth/         JWT, registration, login
│   │   ├── common/       Error handling
│   │   ├── config/       Security & WebSocket config
│   │   ├── notification/ Push notification service
│   │   ├── question/     Question CRUD
│   │   ├── tag/          Tag management
│   │   └── user/         User entity & roles
│   └── src/main/resources/application.properties
│
├── chat-service/         STOMP/WebSocket chat microservice
│   ├── src/main/java/com/doconnect/chatservice/
│   │   ├── chat/         Chat entities, controllers, WS handler
│   │   ├── common/       Shared error handling
│   │   ├── config/       WebSocket config
│   │   └── security/     JWT validation (read-only)
│   └── src/main/resources/application.properties
│
└── frontend/             React + Vite SPA
    └── src/
        ├── components/   Reusable UI components
        ├── context/      Auth & Notification context providers
        ├── hooks/         Custom hooks (useChat)
        ├── lib/           Axios + API helpers
        └── pages/         Route-level page components
```

---

## API Quick Reference

All endpoints (except `/api/auth/register` and `/api/auth/login`) require:

```
Authorization: Bearer <jwt-token>
```

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Register a new account |
| `POST` | `/api/auth/login` | Login, receive JWT |
| `GET` | `/api/auth/me` | Current user info |
| `GET` | `/api/questions` | List questions (paginated, filterable) |
| `POST` | `/api/questions` | Create a question |
| `GET` | `/api/questions/:id` | Question detail |
| `DELETE` | `/api/questions/:id` | Delete own question |
| `POST` | `/api/questions/:id/answers` | Post an answer |
| `DELETE` | `/api/answers/:id` | Delete own answer |
| `POST` | `/api/questions/:id/accept/:answerId` | Accept an answer |
| `POST` | `/api/ai/questions/:id/suggest-answer` | AI answer suggestion |
| `POST` | `/api/ai/questions/:id/summarize` | AI discussion summary |
| `POST` | `/api/ai/improve-draft` | AI draft improvement |
| `POST` | `/api/ai/recommendations/predict-tags` | AI tag prediction |
| `GET` | `/api/ai/recommendations/similar/:id` | Similar questions |
| `GET` | `/api/notifications` | Fetch notifications |
| `PUT` | `/api/notifications/:id/read` | Mark notification read |
| `GET` | `/api/analytics/summary` | Analytics summary (ADMIN) |

WebSocket endpoints:
- `ws://host/ws/notifications` — notification push (main backend)
- `ws://host/ws` — STOMP chat (chat-service)

---

## Licence

This project is an academic capstone. Not licensed for commercial use.
