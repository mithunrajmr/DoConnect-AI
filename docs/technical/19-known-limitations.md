# 19 - Known Limitations

While DoConnect AI is functionally complete, certain architectural shortcuts were taken to ensure rapid development of the MVP. Engineers extending the system should be aware of these constraints.

## 1. LLM Latency & Thread Blocking
As noted in ADR 4, calls to the Gemini API are synchronous.
*   **The Issue:** If the LLM takes 5 seconds to generate a summary, the Tomcat HTTP worker thread is blocked for 5 seconds. Under high traffic, a sudden influx of "Summarize this thread" requests could exhaust the connection pool, causing the entire Main API to become unresponsive to basic CRUD requests.
*   **Impact:** Moderate to High risk at scale.

## 2. In-Memory STOMP Broker
The current Spring WebSocket configuration uses `registry.enableSimpleBroker("/topic", "/queue")`.
*   **The Issue:** This broker stores subscriptions and messages in the JVM's RAM.
*   **Impact:** If the Chat Service or Main API is scaled horizontally to 2 or more instances behind a load balancer, WebSockets break. A user connected to Instance A will not receive a chat message sent by a user connected to Instance B.

## 3. Database Search Performance
The application currently uses JPA method names (e.g., `findByTitleContainingIgnoreCase`) for searching questions.
*   **The Issue:** This compiles down to a SQL `LIKE '%term%'` query. This requires a full table scan and ignores indexes.
*   **Impact:** As the `questions` table grows to hundreds of thousands of rows, search functionality will become incredibly slow.

## 4. Chat Message Pagination
The frontend `ChatBox` component currently fetches the last 50 messages on load (`/api/chat/history?limit=50`).
*   **The Issue:** There is no "Load More" functionality or infinite scrolling implemented in the frontend or backend. Users cannot read history older than the last 50 messages.

## 5. Token Revocation
The system uses JWTs with a 24-hour expiration.
*   **The Issue:** Because JWTs are stateless, there is no way to instantly revoke a user's access (e.g., if an Admin bans them) until their current token expires naturally.

## 6. Lack of Application Logging
Currently, there is no structured logging framework implemented across the backend or chat-service (with the minor exception of the `NotificationClient`).
*   **The Issue:** During development or in production, if an error occurs (e.g., a 500 Internal Server Error, or an AI failure), the application fails silently without writing a descriptive message or stack trace to a log file.
*   **Impact:** High. Troubleshooting production issues or tracing requests will be extremely difficult without running the application in a debugger. Implementing `@Slf4j` and Logback configuration is highly recommended before production deployment.

---
*Next Document: [20-future-improvements.md](20-future-improvements.md)*
