# 13 - REST API Reference

All endpoints (unless marked otherwise) require an `Authorization: Bearer <token>` header. The base path for the Main API is `http://localhost:8080/api` and the base path for Chat is `http://localhost:8090/api` (for history).

## 1. Authentication (`/api/auth`)

*   `POST /register`
    *   **Auth Required:** No
    *   **Body:** `{"name": "...", "email": "...", "password": "..."}`
    *   **Returns:** `201 Created` with JWT.
*   `POST /login`
    *   **Auth Required:** No
    *   **Body:** `{"email": "...", "password": "..."}`
    *   **Returns:** `200 OK` with JWT.
*   `GET /me`
    *   **Returns:** `200 OK` User profile details.

## 2. Questions (`/api/questions`)

*   `GET /`
    *   **Returns:** `200 OK` List of latest questions.
*   `GET /{id}?trackView=true`
    *   **Returns:** `200 OK` Detailed question object. Optional query param increments view count.
*   `POST /`
    *   **Body:** `{"title": "...", "body": "...", "tags": ["react", "java"]}`
    *   **Returns:** `201 Created`
*   `PUT /{id}`
    *   **Body:** `{"title": "...", "body": "...", "tags": []}`
    *   **Constraint:** Must be author or ADMIN.
*   `DELETE /{id}`
    *   **Constraint:** Must be author or ADMIN.
*   `POST /{questionId}/accept/{answerId}`
    *   **Returns:** `200 OK` Updates question status to SOLVED.
    *   **Constraint:** Must be author or ADMIN.

## 3. Answers (`/api/answers` & `/api/questions/{id}/answers`)

*   `GET /api/questions/{questionId}/answers`
    *   **Returns:** `200 OK` List of answers for a question.
*   `POST /api/questions/{questionId}/answers`
    *   **Body:** `{"body": "...", "aiGenerated": boolean, "aiConfidence": number}`
    *   **Returns:** `201 Created`
*   `PUT /api/answers/{id}`
    *   **Body:** `{"body": "..."}`
*   `DELETE /api/answers/{id}`

## 4. Notifications (`/api/notifications`)

*   `GET /`
    *   **Returns:** `200 OK` Current user's notification history.
*   `GET /unread-count`
    *   **Returns:** `200 OK` `{"unreadCount": 5}`
*   `PUT /{id}/read`
    *   **Returns:** `200 OK` Marks specific notification as read.
*   `PUT /read-all`
    *   **Returns:** `200 OK` Resets unread count to 0.

## 5. Analytics (`/api/analytics`)

*All analytics endpoints require `ADMIN` or `MODERATOR` roles.*
*   `GET /overview`
*   `GET /tags`
*   `GET /activity`
*   `GET /sentiment`

## 6. AI Integrations (`/api/ai`)

*   `POST /questions/{questionId}/suggest-answer`
    *   **Returns:** `200 OK` AI drafted answer.
*   `POST /questions/{questionId}/summarize`
    *   **Returns:** `200 OK` AI thread summary.
*   `POST /improve-draft`
    *   **Body:** `{"text": "..."}`
*   `POST /moderation/check`
    *   **Body:** `{"content": "..."}`
    *   **Returns:** `200 OK` `{"toxic": false, "spam": false, "score": 0.1}`

## 7. Internal Service Endpoints

*   `POST /internal/notifications/chat-message` (Main API)
    *   **Auth Required:** `X-Internal-Token` header. No JWT.
    *   **Caller:** Chat Microservice.
    *   **Purpose:** Triggers a push notification to users outside the chat room.

## 8. Chat History (Port 8090)

*   `GET /api/chat/history?limit=50`
    *   **Returns:** `200 OK` Array of past ChatMessages.

---
*Next Document: [14-websocket-reference.md](14-websocket-reference.md)*
