# 18 - Architectural Design Decisions (ADR)

This document captures the "Why" behind the core technical choices in DoConnect AI.

## ADR 1: React Context API vs. Redux
**Context:** The frontend needs to manage authentication state, active chat connections, and real-time notification counts across multiple deeply nested components.
**Decision:** We chose React Context (`AuthContext`, `ChatContext`, `NotificationContext`) combined with custom hooks (`useAuth`, `useChat`, `useNotifications`) over Redux.
**Rationale:**
*   Redux introduces significant boilerplate (actions, reducers, stores).
*   The global state in this application is mostly isolated to specific domains (Auth is Auth, Chat is Chat).
*   React Context allows for clean, logical encapsulation. The `ChatProvider` can manage its own Stomp connection internally without polluting a global Redux store.

## ADR 2: Two Microservices (Main + Chat) vs. One Monolith
**Context:** Global real-time chat creates thousands of long-lived, idle WebSocket connections. Core Q&A CRUD operations require short-lived, transactional HTTP connections.
**Decision:** Extract the Chat domain into a separate Spring Boot application (`chat-service`) while keeping users, questions, answers, and notifications in the `backend`.
**Rationale:**
*   WebSocket thread pooling differs from Tomcat HTTP thread pooling.
*   Chat can scale horizontally based on active concurrent connections independently of the Main API.
*   Isolating chat prevents a sudden spike in chat traffic from locking up the database transactions required to post a Question.

## ADR 3: JWT over Session Cookies
**Context:** Securing API endpoints and establishing user identity.
**Decision:** Stateless JSON Web Tokens (JWT) passed via the `Authorization: Bearer` header.
**Rationale:**
*   Microservice compatibility: The Chat Service can authenticate users without needing a shared Redis session store. It just needs the shared JWT cryptographic secret.
*   WebSockets: STOMP doesn't easily transmit HTTPOnly cookies during the handshake phase across different domains, but it easily accepts custom headers in the `CONNECT` frame.

## ADR 4: Synchronous AI LLM Calls
**Context:** Integrating Google Gemini for moderation, drafting, and summarizing.
**Decision:** Blocking, synchronous REST calls (`geminiClient.generateText()`) inside the Spring `@Service` layer.
**Rationale:**
*   Simplicity for the MVP. It allows the frontend to show a loading spinner and immediately receive the text without implementing a complex polling or WebHook architecture.
*   *Tradeoff:* Documented in [19-known-limitations](19-known-limitations.md).

## ADR 5: Minimal High-Value Logging Strategy
**Context:** Over-logging (e.g., logging every HTTP request, method entry/exit, or full database queries) leads to log spam, increased storage costs, and difficulty finding actual issues during an incident.
**Decision:** We adopted a minimal, high-value structured logging approach using standard SLF4J (with `@Slf4j`).
**Rationale:**
*   Only critical business workflows (e.g., user registration, question creation, AI moderation) and actual failures (e.g., unauthorized access attempts, external API errors) are logged.
*   Sensitive data (passwords, JWTs, full AI prompts/responses) are strictly excluded from logs to maintain security and compliance.
*   This ensures logs remain highly actionable and cost-effective for production observability (e.g., ELK Stack, Datadog).

---
*Next Document: [19-known-limitations.md](19-known-limitations.md)*
