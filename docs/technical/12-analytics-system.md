# 12 - Analytics & Health System

## 1. Overview
To help administrators gauge the health and velocity of the platform, DoConnect AI features a dedicated Analytics System (`AnalyticsService`).

## 2. Core Metrics Collection
The `AnalyticsController` exposes endpoints that aggregate platform data in real-time using JPA custom queries.

### 2.1 Global Overview
*   **Endpoint:** `GET /api/analytics/overview`
*   **Data:** Total registered users, total questions asked, total answers provided.
*   **Usage:** A high-level pulse check of platform adoption.

### 2.2 Tag Popularity
*   **Endpoint:** `GET /api/analytics/tags`
*   **Data:** A sorted list of the most frequently used tags.
*   **Usage:** Helps admins understand which technologies the community is currently focusing on, allowing them to recruit specific experts or seed relevant content.

### 2.3 User Activity Leaderboard
*   **Endpoint:** `GET /api/analytics/activity`
*   **Data:** Aggregates `questionCount` and `answerCount` per user, sorting by `totalActivity`.
*   **Usage:** Identifies power-users. *Future Improvement:* This data structure provides the foundation for a gamified reputation or badge system.

## 3. AI-Powered Community Sentiment
Rather than relying purely on quantitative numbers, the system uses the Gemini LLM for qualitative analysis.

### 3.1 The Sentiment Pipeline
*   **Endpoint:** `GET /api/analytics/sentiment`
*   **Workflow:**
    1.  The `AnalyticsService` fetches the 10 most recent questions and the 10 most recent answers.
    2.  It concatenates and truncates these text blocks to form a single "Recent Content" string.
    3.  It constructs a prompt asking the AI to evaluate the overall mood of the platform: "Is the community frustrated, helpful, toxic, or engaged?"
    4.  The AI returns a JSON payload containing an `overallSentiment` enum (`positive`, `neutral`, `negative`), specific numerical scores, and a brief natural language `summary`.
*   **Resilience:** If the AI fails to return valid JSON, the service falls back to a regex-based string parser looking for keywords in the raw response text, ensuring the dashboard never crashes due to a hallucination.

---
*Next Document: [13-api-reference.md](13-api-reference.md)*
