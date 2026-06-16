# 01 - Project Overview

## 1. Introduction to DoConnect AI

DoConnect AI is a premium, AI-powered developer discussion platform designed to modernize how engineering teams and communities share knowledge. While traditional Q&A platforms (such as Stack Overflow) rely heavily on manual moderation, user tagging, and community diligence to prevent duplicate questions, DoConnect AI embeds Artificial Intelligence directly into the user workflow.

By integrating Google's `gemini-2.0-flash` model, the platform tackles knowledge fragmentation, moderation fatigue, and response latency. This is paired with a highly scalable, decoupled microservice architecture using Spring Boot for robust transaction management and a separate STOMP WebSocket service for real-time engagement.

## 2. Business Problems Solved

1. **Knowledge Fragmentation via Duplicates**
   - *Problem:* Users often ask variations of the same question without searching first, leading to fragmented answers across multiple threads.
   - *Solution:* As users draft a question, DoConnect AI analyzes the semantic intent and proactively suggests existing solved discussions, preventing duplicate submissions before they happen.
2. **Inaccurate Categorization**
   - *Problem:* Poor or inaccurate tagging limits the discoverability of questions, meaning experts never see the questions they are best equipped to answer.
   - *Solution:* The platform asynchronously generates accurate technology tags by understanding the core context of the question title and body.
3. **Moderation Bottlenecks and Toxicity**
   - *Problem:* Manual review of user-generated content is unscalable. Spam or offensive content remains visible until a human moderator intervenes.
   - *Solution:* An automated AI moderation pipeline acts as a gatekeeper, scoring content upon submission and instantly quarantining high-risk posts to a specialized Admin Dashboard.
4. **Friction in Knowledge Sharing**
   - *Problem:* Drafting a high-quality, technically accurate, and well-formatted answer from scratch is time-consuming.
   - *Solution:* AI tools integrated into the answering editor allow experts to generate initial drafts, improve their own writing (grammar/formatting), and summarize long discussions.

## 3. High-Level Core Workflows

### 3.1. User Onboarding & Discovery
Users join via a secure registration process utilizing robust client-side validation and server-side JWT issuance. Once authenticated, users access a paginated, dynamic feed displaying the most relevant questions, complete with AI-generated tags and resolution statuses (`SOLVED` / `OPEN`).

### 3.2. Knowledge Creation (Q&A)
The core loop revolves around asking and answering technical questions. During question creation, users benefit from real-time tag generation and duplicate detection. When answering, experts use an AI-assisted Markdown editor to draft, refine, and publish solutions. The author of a question can mark a specific answer as "Accepted," which updates the question state to `SOLVED` and visually prioritizes the answer.

### 3.3. Real-Time Engagement
The platform features an isolated chat microservice providing a global community chat room. This ensures users can collaborate instantly without page refreshes. Furthermore, a real-time notification engine pushes alerts (e.g., "Your question received a new answer") directly to the user's active session.

### 3.4. Administration & Analytics
Administrators have access to a privileged dashboard. Here, they review content quarantined by the AI moderation pipeline, manage user roles, and monitor platform health through AI-generated sentiment analysis and usage metrics.

## 4. Key Architectural Tenets

- **Microservice Isolation:** Real-time WebSocket traffic (Chat Service on Port 8090) is physically separated from synchronous transactional logic (Main API on Port 8080) to prevent long-lived connections from starving database connections and HTTP threads.
- **Stateless Security:** Security is strictly maintained using short-lived JWTs, allowing for horizontal scalability across any number of backend nodes.
- **Secure Inter-Service Communication:** A robust internal tokening system (`X-Internal-Token`) bridges events between the Chat Service and the Main API, ensuring notifications can be triggered without exposing internal endpoints to the public.
- **AI as a Workflow Enhancer:** Rather than acting as a standalone chatbot, AI is embedded into specific HTTP handlers and React components as a contextual tool (e.g., `AiAssistantPanel`, `TagPredictor`).

## 5. Technology Stack Summary

| Layer | Technologies Used |
| :--- | :--- |
| **Frontend** | React 19, Vite, Tailwind CSS 4, Axios, `@stomp/stompjs` |
| **Main Backend** | Java 21, Spring Boot 3.5.x, Spring Data JPA, Spring Security |
| **Chat Service** | Java 21, Spring Boot 3.5.x, Spring WebSockets (STOMP) |
| **Database** | MySQL 8.0 (Multiple Schemas: `doconnect_ai`, `doconnect_chat`) |
| **AI Integration** | Google Gemini API (`gemini-2.0-flash`) via standard REST |

---
*Next Document: [02-system-architecture.md](02-system-architecture.md)*
