# 20 - Future Improvements Roadmap

Based on the limitations outlined in Document 19, the following initiatives should be prioritized for V2 of DoConnect AI.

## Phase 1: Stability & Scale

### 1. External Message Broker (RabbitMQ / Redis)
*   **Action:** Replace the Spring `SimpleBroker` with a full STOMP relay (RabbitMQ) or Redis Pub/Sub.
*   **Benefit:** Allows the Chat Service and Main API Notification Hub to scale horizontally to multiple instances. Ensures messages are distributed across the cluster.

### 2. Asynchronous AI Tasks
*   **Action:** Refactor `AiAnswerAssistantService` to use Spring `@Async` or a messaging queue.
*   **Flow:** Frontend requests a summary -> Backend returns a `202 Accepted` immediately -> AI processes in the background -> Backend sends a WebSocket notification with the result when finished.
*   **Benefit:** Protects the Tomcat HTTP thread pool from LLM latency spikes.

## Phase 2: Feature Expansion

### 3. Full-Text Search Engine (Elasticsearch / Meilisearch)
*   **Action:** Index all Questions, Answers, and Tags into a dedicated search engine.
*   **Benefit:** Fixes the SQL `LIKE` performance issue. Enables fuzzy matching, typo tolerance, and complex filtering (e.g., "Search for 'NullPointerException' but only in questions tagged 'Java'").

### 4. Advanced Reputation System
*   **Action:** Expand the `AnalyticsService` to track reputation points (e.g., +10 for an accepted answer, +2 for an upvote).
*   **Benefit:** Gamifies the platform, encouraging higher-quality contributions. Allows for automatic privilege escalation (e.g., users with 1000+ rep can edit tags).

### 5. Media Attachments (S3 Integration)
*   **Action:** Implement AWS S3 or MinIO integration.
*   **Flow:** Allow users to upload screenshots of stack traces or UI bugs.
*   **Benefit:** Greatly enhances the ability to debug visual or complex technical issues.

## Phase 3: Architectural Evolution

### 6. GraphQL API Transition
*   **Action:** Introduce a GraphQL schema alongside the REST API.
*   **Benefit:** Allows the React frontend to fetch exactly the fields it needs (e.g., fetching a Question, its Tags, its Author's name, and the total Answer count in a single request without over-fetching the Answer bodies).

### 7. JWT Refresh Tokens
*   **Action:** Implement short-lived Access Tokens (15 minutes) and long-lived Refresh Tokens (stored in HTTPOnly cookies).
*   **Benefit:** Resolves the token revocation limitation. Allows Admins to immediately revoke a Refresh Token, cutting off a banned user's access within 15 minutes.
