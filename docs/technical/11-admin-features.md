# 11 - Administrator Features & Moderation

## 1. Overview
DoConnect AI includes a suite of features exclusively available to users with the `ADMIN` or `MODERATOR` role. These features are designed to maintain platform health, review flagged content, and oversee the community.

## 2. Role-Based Access Control (RBAC)
Administrator features are protected at both the frontend and backend layers.
*   **Frontend:** The `AdminRoute` component checks `user.role === 'ADMIN' || user.role === 'MODERATOR'`. If false, the route refuses to mount and redirects to the feed.
*   **Backend:** Controller endpoints and service methods are secured using Spring Security's `@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")`.

## 3. The Moderation Dashboard
Traditional forums rely on users clicking "Report" before an admin sees toxic content. DoConnect AI flips this paradigm using AI.

### 3.1 AI Pre-Filtering
When a question or answer is submitted, it is optionally passed through the `AiModerationService` (utilizing Gemini). The AI assigns a `toxic` flag, a `spam` flag, and a `score`.
*   If the score crosses a defined threshold, the entity's `moderation_status` is set to `FLAGGED` or `PENDING` instead of `APPROVED`.
*   *Result:* The content is hidden from the public feed immediately.

### 3.2 Human Review
The Admin Moderation Dashboard queries the database for all content where `moderation_status != 'APPROVED'`.
*   Admins can view the AI's provided `reason` (e.g., "Content appears potentially toxic or abusive").
*   Admins have the final say, clicking "Approve" (which publishes the post) or "Reject/Delete" (which permanently removes it).

## 4. Content Overrides
Administrators have elevated privileges within the standard Q&A flow:
1.  **Edit Any Question/Answer:** While normal users can only edit their own content, the `QuestionService` explicitly permits Admins to edit typos or formatting in *any* user's post.
2.  **Force Accept Answer:** If a question author abandons their thread, an Admin can manually mark an answer as "Accepted" to ensure the community benefits from a clearly marked solution.
3.  **Delete Anything:** Admins can delete spam posts or irrelevant tags to keep the database clean.

---
*Next Document: [12-analytics-system.md](12-analytics-system.md)*
