# 16 - Testing Guide

## 1. Overview
The decoupled nature of DoConnect AI requires specific testing strategies for the Frontend, the Main API, and the Chat Microservice. The backend and chat-service currently have a foundational test suite implemented covering core business logic and API integrations.

### 1.1 How to Run Tests
The project uses Maven. You can execute the test suite using the following commands:

**For the Backend:**
```bash
cd backend
mvnw test
```

**For the Chat Service:**
```bash
cd chat-service
mvnw test
```

Alternatively, you can run tests directly from your IDE (IntelliJ, Eclipse, VS Code) by clicking the Play icon next to the test classes or `@Test` methods.

## 2. Backend Testing Strategy (Spring Boot)

Currently implemented tests use the following frameworks and strategies:

### 2.1 Unit Testing (Service Layer)
*   **Framework:** JUnit 5 + Mockito.
*   **Focus:** The business logic inside `@Service` classes.
*   **Example Scenarios:**
    *   `QuestionService`: Mock the `UserRepository` and `QuestionRepository`. Verify that `requireOwnerOrAdmin` throws an `AccessDeniedException` if a normal user tries to edit someone else's question.
    *   `JwtService`: Assert that `isTokenValid()` correctly returns false for an expired token or a token signed with a different secret.

### 2.2 Integration Testing (Controller Layer)
*   **Framework:** `@WebMvcTest` with `MockMvc`.
*   **Focus:** API endpoint routing, DTO validation (`@Valid`), and exception mapping via `ApiExceptionHandler`.
*   **Example Scenarios:**
    *   `POST /api/questions`: Pass an empty title. Assert that the API returns a `400 Bad Request` with the correct validation error array.

### 2.3 Data Layer Testing
*   **Framework:** `@DataJpaTest` with H2 in-memory database or Testcontainers (MySQL).
*   **Focus:** Custom JPQL queries and cascading deletes.
*   **Example Scenarios:**
    *   Ensure that deleting a `Question` successfully deletes all associated `Answer` rows.

## 3. Frontend Testing Strategy (React)

### 3.1 Unit & Component Testing
*   **Framework:** Vitest + React Testing Library.
*   **Focus:** UI rendering and custom hooks.
*   **Example Scenarios:**
    *   `AiAssistantPanel`: Mock the `aiApi.js` call. Verify that clicking "Suggest Answer" shows a loading spinner, and then renders the markdown correctly once the mock resolves.
    *   `useChat`: Mock the Stomp client. Verify that incoming messages are correctly appended to the state without duplicating IDs.

### 3.2 End-to-End (E2E) Testing
*   **Framework:** Playwright or Cypress.
*   **Focus:** Critical user flows.
*   **Example Scenarios:**
    *   Register -> Login -> Ask a Question -> Auto-Tag Generation -> Submit.

## 4. Manual Testing WebSockets
Testing WebSockets programmatically can be complex. For manual verification during development, use a tool like **Postman** (which supports raw WebSockets and Socket.IO) or a dedicated STOMP client extension. Remember to attach the `Authorization` header in the connect frame, not the HTTP handshake.

## 5. Testing Logging Output

To verify that the minimal high-value logging strategy is working correctly during development:

### 5.1 Local Console Verification
*   **Run the Services:** Start both the `backend` and `chat-service` locally (e.g., using your IDE or `./mvnw spring-boot:run`).
*   **Trigger INFO Events:** Use the frontend or Postman to register a new user, log in, create a question, or post a global chat message. Observe the console for the structured `[INFO]` logs (e.g., `Question created. questionId=X, userId=Y`).
*   **Trigger WARN/ERROR Events:** Attempt to modify a question you don't own, provide an invalid email/password during login, or simulate an AI failure. Verify that the appropriate `[WARN]` and `[ERROR]` logs appear without leaking sensitive data.

### 5.2 Automated Log Verification
*   In integration tests (`@SpringBootTest`), you can utilize Spring Boot's `OutputCaptureExtension` to assert that specific business rules emit the correct log messages.

---
*Next Document: [17-deployment-guide.md](17-deployment-guide.md)*
