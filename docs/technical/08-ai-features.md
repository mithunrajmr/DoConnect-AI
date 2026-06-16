# 08 - AI Features & Integration

## 1. Overview
DoConnect AI integrates the Google `gemini-2.0-flash` model to solve specific workflow frictions rather than providing a generic chatbot. The AI acts as a backend service, invoked synchronously via REST from the React frontend.

## 2. Gemini Client Wrapping
The integration is abstracted behind the `GeminiClient` class, which uses Spring's `RestClient`.
It constructs a JSON payload conforming to the Google Gemini REST API standard (`{"contents": [{"parts": [{"text": "prompt"}]}]}`) and handles HTTP timeouts. If the Gemini API returns a `401 Unauthorized` or `502 Bad Gateway`, the backend's `ApiExceptionHandler` translates it into a structured `ApiError` for graceful frontend degradation.

## 3. Feature: Content Moderation

To combat toxicity and spam at scale, DoConnect AI uses Gemini as an automated content evaluator.

### 3.1 The Moderation Pipeline (`AiModerationService`)
1.  **Trigger:** Can be invoked explicitly (`/api/ai/moderation/check`) or implicitly as part of a post-creation workflow.
2.  **Prompt Engineering:** The backend constructs a prompt instructing the LLM to act as a professional moderator. It explicitly requests a JSON response containing `{"toxic": boolean, "spam": boolean, "score": number, "reason": string}`.
3.  **Parsing & Resilience:** The service uses Jackson's `ObjectMapper` to parse the JSON returned inside the Markdown code fences (`` ```json ... ``` ``). If the LLM hallucinates an invalid format, a `catch` block safely falls back to a primitive string-matching heuristic (checking for the words "toxic" or "spam").

## 4. Feature: Answer Drafting & Assistance

Experts are assisted by the `AiAnswerAssistantService`.

### 4.1 Suggest Answer Workflow
*   **Endpoint:** `POST /api/ai/questions/{questionId}/suggest-answer`
*   **Workflow:** The backend pulls the full text of the question, its tags, and *all existing answers*. It constructs a prompt telling the LLM to draft a response that complements (rather than repeats) the existing answers.
*   **Value:** Prevents writer's block by providing a highly contextual starting draft.

### 4.2 Improve Draft Workflow
*   **Endpoint:** `POST /api/ai/improve-draft`
*   **Workflow:** The user submits a raw, potentially poorly formatted block of text. The AI is prompted to "Improve grammar and clarity while preserving the original meaning... Do not add unsupported claims."

### 4.3 Discussion Summarization
*   **Endpoint:** `POST /api/ai/questions/{questionId}/summarize`
*   **Workflow:** For long threads, the AI ingests the question and all answers to produce a brief paragraph highlighting the core issue and the consensus solution, saving future readers from scrolling.

## 5. Feature: Auto-Tagging & Semantic Duplicates
*(Note: As implemented in the frontend AskPage and backend RecommendationService).*
*   **Auto-Tagging:** As a user types a question title/body, a debounced call sends the text to the AI to predict the best technology tags (e.g., "React", "Spring Boot"), improving discoverability without relying on user accuracy.
*   **Duplicate Detection:** Analyzes the semantic meaning of the drafted question against existing records to suggest similar, already-solved questions before submission.

## 6. Limitations & Future Work
*   **Synchronous Blocking:** Currently, HTTP threads block while waiting for Gemini to respond. Under heavy load, this could exhaust the Tomcat connection pool.
*   **Future Improvement:** Migrate heavy generation tasks (like Summarization) to asynchronous background workers (e.g., Spring `@Async` or RabbitMQ) and notify the user via WebSockets when the AI is finished.

---
*Next Document: [09-notification-system.md](09-notification-system.md)*
