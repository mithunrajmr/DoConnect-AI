# 10 - Question & Answer Lifecycle

## 1. Overview
The primary domain of DoConnect AI revolves around the creation, enhancement, and resolution of technical questions. This lifecycle is strictly managed by the `QuestionService` and `AnswerService`, enforcing ownership rules and state transitions.

## 2. Question Lifecycle

A Question entity moves through several states during its lifespan.

### 2.1 State Definitions
*   **Status (`QuestionStatus`):**
    *   `OPEN`: Accepting answers.
    *   `SOLVED`: The author (or an admin) has accepted an answer.
    *   `CLOSED`: Manually locked (no new answers permitted).
*   **Moderation (`ModerationStatus`):**
    *   `APPROVED`: Visible to the public.
    *   `FLAGGED` / `PENDING`: Quarantined, visible only in the Admin Dashboard.

### 2.2 Creation Workflow
1.  **Client Request:** User hits `POST /api/questions`. The body contains the title, markdown body, and a list of tag strings.
2.  **Tag Resolution:** `TagService.resolveTags(request.tags())` runs. It attempts to find existing tags in the database by name. If a tag doesn't exist, it creates it.
3.  **Persistence:** The `Question` is saved with status `OPEN` and `APPROVED`.
4.  *(Optional Background Hook):* AI moderation may scan the text asynchronously.

### 2.3 Editing and Deletion
*   **Rule Enforcement:** The `QuestionService.requireOwnerOrAdmin` method is invoked on `PUT` and `DELETE` requests.
*   If the user's `userId` does not match the question's `author_id`, and their role is not `ADMIN`, an `AccessDeniedException` (HTTP 403) is thrown.

## 3. Answer Lifecycle

Answers are child entities of Questions.

### 3.1 Creation Workflow
1.  **Client Request:** User hits `POST /api/questions/{id}/answers`.
2.  **Validation:** The `AnswerService` verifies the parent Question exists and is not `CLOSED`.
3.  **Persistence:** The `Answer` is saved. It includes a boolean `ai_generated` flag if the user utilized the Gemini assistant to write it.
4.  **Notification:** The `NotificationService` is invoked to push an alert to the Question's author.

## 4. The "Accepted Answer" Transition

To prevent knowledge fragmentation and indicate a solved issue, the system allows the selection of a single correct answer.

### Workflow: `POST /api/questions/{questionId}/accept/{answerId}`
1.  **Ownership Check:** `requireOwnerOrAdmin` ensures only the question author or an administrator can invoke this.
2.  **Validation:** Ensures the `answerId` actually belongs to the `questionId`.
3.  **Reset Matrix:** The service fetches all existing answers for the question. In a single loop, it sets `accepted = false` for every answer except the target, which is set to `true`.
4.  **Question Update:** The Question's `acceptedAnswerId` denormalized field is updated, and its `status` transitions from `OPEN` to `SOLVED`.
5.  **Database Commit:** The changes are saved transactionally, ensuring data integrity.

## 5. View Tracking

The Question entity includes a `viewCount` field. 
*   When the frontend mounts the `QuestionDetailPage`, it fires `GET /api/questions/{id}?trackView=true`.
*   The `QuestionService.recordView` method invokes a custom JPA query: `UPDATE Question q SET q.viewCount = q.viewCount + 1 WHERE q.id = :id`.
*   *Performance Note:* This is executed as a direct `UPDATE` statement via `@Modifying` rather than fetching the entity, mutating it, and saving it. This prevents race conditions and Hibernate dirty-checking overhead on highly trafficked questions.

---
*Next Document: [11-admin-features.md](11-admin-features.md)*
