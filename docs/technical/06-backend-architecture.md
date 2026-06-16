# 06 - Backend Architecture

## 1. Overview
The Main API (Port 8080) is built on Java 21 and Spring Boot 3.5.x. It acts as the primary monolithic transaction engine for DoConnect AI. It handles authentication, Q&A CRUD, integrations with Google Gemini, and real-time push notifications.

## 2. Structural Paradigm

The backend strictly adheres to a **Domain-Driven layered architecture**. Each feature (e.g., Question, Answer, Auth) is encapsulated in its own package.

Inside each domain package, the code is separated into three distinct layers:
1.  **Controllers (`@RestController`):** Responsible for HTTP request mapping, parameter extraction, invoking the service layer, and returning standard JSON payloads (`ResponseEntity`).
2.  **Services (`@Service`):** The core business logic layer. Responsible for managing database transactions (`@Transactional`), enforcing ownership rules (e.g., "Can user X delete question Y?"), and mapping JPA entities to Data Transfer Objects (DTOs).
3.  **Repositories (`@Repository`):** Interfaces extending Spring Data's `JpaRepository` or `ListCrudRepository` for database access.

## 3. Global Exception Handling

To ensure frontend clients receive predictable JSON error payloads, the backend utilizes an `@RestControllerAdvice` class named `ApiExceptionHandler`.

When a controller or service throws an exception (e.g., `ResourceNotFoundException`, `AccessDeniedException`, or `MethodArgumentNotValidException`), this handler intercepts it.
It converts the stack trace into a standardized `ApiError` DTO containing:
*   `timestamp`: The exact time of failure.
*   `status`: HTTP Status Code (e.g., 404).
*   `error`: Standard HTTP reason phrase.
*   `message`: Detailed error string.
*   `details`: Array of specific field validation errors (if applicable).

## 4. Key Packages & Domains

### 4.1 `com.doconnect.backend.auth`
Manages user onboarding and JWT lifecycle. Contains `AuthService` and `JwtService`. Handles BCrypt password hashing and user role assignments.

### 4.2 `com.doconnect.backend.question` & `answer`
The core application domain.
*   **QuestionService:** Handles creation, duplicate tag generation workflows, and the acceptance of answers.
*   **AnswerService:** Handles answer submissions and triggers AI-confidence scoring pipelines.

### 4.3 `com.doconnect.backend.ai`
Wraps the `gemini-2.0-flash` API.
*   `GeminiClient`: Makes the actual HTTP calls to Google's REST API using Spring's `RestClient`.
*   `AiModerationService`: Grades newly submitted questions/answers for spam or toxicity.
*   `AiAnswerAssistantService`: Generates drafts and improves formatting for users writing answers.

### 4.4 `com.doconnect.backend.notification`
Manages the internal WebSocket notification hub.
*   `NotificationRealtimeService`: Integrates with Spring's `SimpMessagingTemplate` to push STOMP messages directly to `/user/queue/notifications`.
*   `InternalNotificationController`: Receives secure HTTP POSTs from the Chat Microservice to trigger global chat notifications.

## 5. Security & Request Lifecycle

When a user requests a protected resource like `POST /api/questions`:
1.  **Filter Chain:** The request passes through `JwtAuthenticationFilter`. The Bearer token is parsed, validated, and the `AppUserDetails` (User principal) is injected into the Spring Security Context.
2.  **Controller Injection:** Spring automatically resolves `@AuthenticationPrincipal AppUserDetails principal` in the controller method signature, giving the controller direct access to the logged-in user without manual token parsing.
3.  **Service Validation:** The Controller passes the DTO and the `User` object to the Service. The Service ensures the User has permission to perform the action.
4.  **Transaction:** A database transaction is opened.
5.  **Return:** Entities are mapped back to `QuestionResponse` DTOs to avoid circular reference issues during Jackson JSON serialization.

## 6. Tradeoffs & Future State
*   **Monolithic Structure:** Grouping questions, users, and AI logic into a single monolith simplifies deployment and foreign-key constraints. However, as the AI moderation logic scales, extracting the `ai` package into its own Python or Java microservice (using Kafka/RabbitMQ) might become necessary to prevent slow LLM generation times from consuming the Tomcat HTTP thread pool.

---
*Next Document: [07-chat-microservice.md](07-chat-microservice.md)*
