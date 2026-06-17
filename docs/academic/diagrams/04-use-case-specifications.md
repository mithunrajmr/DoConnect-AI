# Detailed Use Case Specifications

### Explanation
This document outlines the strict tabular specification of the core "Ask Question" use case.

### Source Code References
- `QuestionController.java`, `RecommendationController.java`, `Question.java`

| Use Case ID | UC-01 |
| :--- | :--- |
| **Use Case Name** | Ask a Question with AI Assistance |
| **Primary Actor** | Authenticated User |
| **Description** | Allows a user to post a technical question. The user can optionally use AI to find duplicates and generate tags. |
| **Pre-conditions** | User must be logged in with a valid JWT. |
| **Post-conditions** | Question is saved in the database with status OPEN and is visible in the global feed. |
| **Main Flow** | 1. User clicks "Ask Question".<br>2. User enters Title and Body.<br>3. User clicks "Submit".<br>4. System saves the Question.<br>5. System redirects to Question Detail view. |
| **Alternate Flow A (Tags)** | 2a. User clicks "Predict Tags".<br>2b. System calls `POST /api/ai/recommendations/predict-tags`.<br>2c. System populates tag fields. |
| **Exceptions** | - Title/Body empty (Validation Error).<br>- Token Expired (401 Unauthorized). |
