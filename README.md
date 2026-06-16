# DoConnect AI

<div align="center">
  <h3>A Premium, AI-Powered Developer Discussion Platform</h3>
  <p>Built with React, Spring Boot, WebSockets, and Google Gemini.</p>

  <!-- Badges -->
  <p>
    <img src="https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black" alt="React 19" />
    <img src="https://img.shields.io/badge/Vite-8-646CFF?logo=vite&logoColor=white" alt="Vite" />
    <img src="https://img.shields.io/badge/Spring_Boot-3-6DB33F?logo=spring-boot&logoColor=white" alt="Spring Boot" />
    <img src="https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white" alt="MySQL" />
    <img src="https://img.shields.io/badge/WebSockets_STOMP-000000?logo=socket.io&logoColor=white" alt="WebSockets STOMP" />
    <img src="https://img.shields.io/badge/Google_Gemini-AI-4285F4?logo=google&logoColor=white" alt="Google Gemini" />
    <img src="https://img.shields.io/badge/JWT_Auth-000000?logo=json-web-tokens&logoColor=white" alt="JWT" />
    <img src="https://img.shields.io/badge/Tailwind_CSS-4-38B2AC?logo=tailwind-css&logoColor=white" alt="Tailwind CSS" />
  </p>
</div>

---

## 1. Architecture Highlights

For a quick technical overview, here is the core technology stack driving the platform:

- **React SPA Frontend** (Vite, Tailwind CSS 4)
- **Spring Boot Main Backend** (Core business logic, Port 8080)
- **Dedicated Chat Microservice** (Spring Boot STOMP WebSockets, Port 8090)
- **Real-Time Notifications** (WebSocket events bridged across services)
- **AI-Powered Features** (Google Gemini REST API integration)
- **MySQL Persistence** (Shared database instance)
- **JWT Authentication** (Stateless, secured across REST and WS layers)

---

## 2. Why This Project Matters

DoConnect AI is not just another standard CRUD application; it is a practical demonstration of integrating Large Language Models (LLMs) into active user workflows. 

While many applications treat AI as an isolated chatbot, DoConnect AI weaves the `gemini-2.0-flash` model seamlessly into the developer's journey. By automating content moderation, enforcing semantic duplicate detection, and providing intelligent drafting assistance, the platform solves real-world community building issues: knowledge fragmentation and moderation fatigue. Coupled with a decoupled microservice architecture handling high-throughput WebSockets, this project represents the intersection of robust enterprise engineering and practical AI utility.

---

## 3. Problem Statement

Traditional developer forums and Q&A platforms suffer from several persistent issues:
- **Duplicate Questions:** Users frequently ask questions that have already been answered, fragmenting knowledge.
- **Poor Tagging & Discoverability:** Inaccurate categorization makes it hard for experts to find questions they can answer.
- **Low-Quality Posts:** Grammatical errors and unclear formatting slow down the resolution process.
- **Moderation Bottlenecks:** Manual review of spam or inappropriate content scales poorly.
- **Slow Responses:** Drafting comprehensive technical answers from scratch is a high-friction task.

**The Solution:** DoConnect AI brings the LLM directly to the user's keystrokes. AI acts as an invisible co-pilot during question creation, answer drafting, and platform administration, ensuring high-quality, easily discoverable knowledge.

---

## 4. Key Technical Achievements

- **Deep AI Workflow Integration:** Semantic duplicate detection, automated tag generation, answer drafting, and toxicity analysis.
- **Microservice Isolation:** Separation of the high-throughput STOMP WebSocket chat domain from the primary REST API to protect core business transactions.
- **Real-Time Ecosystem:** Instant in-app notifications and global chat using stateful WebSocket connections.
- **Secure Inter-Service Communication:** Protected internal REST clients bridging the microservices using cryptographically secure tokens.
- **Full-Stack Polish:** Responsive, accessible React SPA coupled with robust Java Spring Data JPA transactions.

---

## 5. Technical Architecture

DoConnect AI employs a scalable architecture, splitting concerns between synchronous business logic and asynchronous real-time communication.

```mermaid
graph TD
  subgraph Client Tier
    Frontend[Frontend SPA<br/>React 19 / Vite / Tailwind]
  end

  subgraph Service Tier
    Backend[Main Backend API<br/>Spring Boot 3 - Port 8080]
    Chat[Chat Microservice<br/>Spring Boot 3 - Port 8090]
  end

  subgraph Data & External APIs
    DB[(Shared Database<br/>MySQL 8+)]
    Gemini[Google Gemini API<br/>gemini-2.0-flash]
  end

  %% Client to Services
  Frontend -- REST / HTTP --> Backend
  Frontend -- STOMP / WS --> Backend
  Frontend -- REST / HTTP --> Chat
  Frontend -- STOMP / WS --> Chat

  %% Service to DB
  Backend -- JDBC --> DB
  Chat -- JDBC --> DB

  %% Service to External/Internal
  Backend -- REST / HTTP --> Gemini
  Chat -- "POST /internal/notifications<br/>(Secured via X-Internal-Token)" --> Backend
```

*(Note: The `chat-service` manages WebSockets but delegates broad notifications by POSTing to the main backend's internal endpoint, cleanly decoupling stateful chat from core API concerns).*

---

## 6. Engineering Challenges & Solutions

Developing DoConnect AI required solving several non-trivial architectural challenges:

### Challenge 1: Isolating Real-Time Traffic
- **Problem:** WebSockets for global chat create high-frequency, long-lived connections that consume threads and memory, potentially starving the core REST API (which processes heavy DB transactions and AI calls).
- **Solution:** Extracted the chat functionality into a dedicated `chat-service` microservice using Spring WebSockets.
- **Outcome:** Independent scaling. The core backend remains highly available for complex queries, while the chat service efficiently handles high-throughput messaging.

### Challenge 2: Secure Inter-Service Communication
- **Problem:** The decoupled `chat-service` must trigger global notifications on the main backend when new messages arrive, without exposing internal notification endpoints to the public internet.
- **Solution:** Implemented a secure internal `RestClient` (`NotificationClient`) in the `chat-service` that injects a shared, cryptographically secure `X-Internal-Token` header. The main backend validates this token via an interceptor before broadcasting the WebSocket notification.
- **Outcome:** Secure, real-time notification bridging between microservices without compromising the public API surface.

### Challenge 3: Scalable Content Moderation
- **Problem:** Manual moderation of questions and answers is unscalable and allows toxic content to remain visible until human review occurs.
- **Solution:** Engineered an automated, pre-publish moderation pipeline using the Google Gemini API.
- **Outcome:** As content is submitted, the AI assesses risk (Safe, Spam, Toxic, Off-Topic) and assigns a confidence score. Flagged content is instantly quarantined to the Admin Dashboard, massively reducing manual workload and protecting the community 24/7.

---

## 7. Complete User Journey

The following walkthrough demonstrates the platform's core workflow.

### Step 1: Onboarding & Security
![Create Account](docs/screenshots/create_account.png)
<br/>
![Password Requirements](docs/screenshots/showing_password_requirement.png)

- **Goal:** Securely join the community.
- **Implementation:** React-driven client-side validation enforces strict password complexity, reducing login friction while maintaining security standards. Authentication is handled via stateless JWTs.

### Step 2: Discovery
![Home Feed](docs/screenshots/home_feed.png)

- **Goal:** Discover active discussions.
- **Implementation:** A paginated, dynamic feed displaying view counts, answer counts, dynamic AI tags, and `SOLVED` status at a glance.

### Step 3: Knowledge Creation & AI Duplicate Detection
![Create Ask Question](docs/screenshots/create_ask_question.png)
<br/>
![AI Similar Question Detection](docs/screenshots/ai_similar_question_suggestion.png)

- **Goal:** Ask a technical question.
- **AI Value:** As the user drafts their question, the AI cross-references the input against the database to detect semantic duplicates. This prevents knowledge fragmentation by redirecting users to existing, solved discussions before they even hit "Submit."

### Step 4: AI Tag Generation
![AI Tag Suggestion Loading](docs/screenshots/create_question_filled_ai_tag_suggestion_loading-2.png)
<br/>
![AI Tag Suggestions Generated](docs/screenshots/create_question_filled_ai_tag_suggested-3.png)

- **Goal:** Accurately categorize the question.
- **AI Value:** The backend asynchronously sends the title and description to Gemini, which analyzes the semantic context to generate the most relevant technology tags. This ensures questions are routed to the right experts without relying on manual user tagging.

### Step 5: Community Discussion
![Published Question](docs/screenshots/question_view.png)

- **Goal:** Track community responses.
- **Implementation:** The published view tracks engagement analytics (views), manages status, and organizes the discussion timeline.

---

## 8. AI-Assisted Answering Experience

![Answer AI Features](docs/screenshots/answer_ai_features.png)

DoConnect AI supercharges the answering experience by providing experts with a suite of AI tools directly in the editor.

### AI Suggested Answers
![AI Suggested Answers](docs/screenshots/ai_generate_suggested_answer.png)
- **Value:** The AI reads the original question and generates a technically sound draft response. Experts can use this as a foundation to overcome "writer's block," editing it for their specific context to publish faster.

### AI Draft Improvement
**Before:**
![AI Improve Draft Before](docs/screenshots/ai_imrpove_draft_before.png)
<br/>
**After:**
![AI Improve Draft After](docs/screenshots/ai_imrpove_draft_after.png)
- **Value:** Takes a rough, unformatted thought and refines the grammar, tone, and formatting (e.g., adding markdown code blocks). This elevates platform quality, allowing non-native speakers or developers in a rush to contribute highly polished answers.

### AI Discussion Summaries
![AI Discussion Summaries](docs/screenshots/ai_question_and_answer_summary.png)
- **Value:** Condenses lengthy, multi-answer threads into a brief summary of the problem and consensus solution. Saves immense time for future readers.

---

## 9. Accepted Answer Workflow

![Admin/Owner Accepted Answer](docs/screenshots/admin_owner_accepted_answer_for_question.png)
<br/>
![Owner/Admin Accepted Answer 2](docs/screenshots/owner_admin_accepted_answer_for_question.png)

- **Goal:** Identify the correct solution.
- **Implementation:** The question author (or an admin) can mark a specific answer as "Accepted." This transitions the question status to `SOLVED` and visually highlights the answer, drastically improving knowledge scannability for future visitors.

---

## 10. Real-Time Features

### Notification System
![New Message Popup](docs/screenshots/new_message_popup_notification-inapp.png)
<br/>
![Notification Panel](docs/screenshots/notification_panel.png)

- **Goal:** Keep users updated without page refreshes.
- **Implementation:** The Main Backend maintains a persistent STOMP WebSocket connection. When an interaction occurs, the backend pushes an event to the client's specific `/user/queue/notifications` topic.

### Global Community Chat
![Global Chat](docs/screenshots/global_chat.png)

- **Goal:** Foster real-time collaboration outside of strict Q&A threads.
- **Implementation:** Hosted on the decoupled `chat-service` (Port 8090), utilizing STOMP over WebSockets for low-latency, high-throughput messaging.

---

## 11. AI Moderation & Analytics

### Moderation Pipeline
![Moderation Safe](docs/screenshots/admin_moderation_test_question_safe.png)
<br/>
![Moderation Spam](docs/screenshots/admin_moderation_test_question_spam.png)
<br/>
![Moderation Dashboard](docs/screenshots/admin_moderation.png)

- **Implementation:** AI acts as the first line of defense. Flagged content is queued in the Admin Moderation Dashboard for human review, protecting the community from spam or toxicity.

### Analytics Dashboard
![Analytics Dashboard](docs/screenshots/admin_analytics.png)
<br/>
![Sentiment Analysis](docs/screenshots/admin_community_sentiment_analysis_ai.png)

- **Implementation:** Aggregates platform metrics (active users, total questions). The AI analyzes recent platform activity to generate a natural language "Community Sentiment" report, providing admins with actionable insights.

---

## 12. Repository Structure

```text
doconnect-ai/
├── backend/                 # Core Spring Boot monolith (Port 8080)
│   ├── src/main/java/com/doconnect/backend
│   │   ├── ai/              # Gemini integration, moderation, tagging
│   │   ├── analytics/       # System metrics and sentiment analysis
│   │   ├── answer/          # Answer CRUD and AI drafting
│   │   ├── auth/            # JWT-based security and filters
│   │   ├── notification/    # WebSocket handlers and dispatchers
│   │   └── question/        # Question CRUD and duplicate detection
│   └── src/main/resources   # application.properties (.env configurations)
├── chat-service/            # Spring Boot WebSockets microservice (Port 8090)
│   ├── src/main/java/com/doconnect/chatservice
│   │   ├── chat/            # STOMP controllers, persistence, inter-service RestClient
│   │   └── security/        # JWT validation and WebSocket auth interceptors
│   └── src/main/resources   # application.properties
└── frontend/                # React 19 SPA (Port 5173)
    ├── src/
    │   ├── components/      # Reusable UI elements (AiAssistantPanel, NavBar)
    │   ├── context/         # React Context (AuthContext, NotificationContext)
    │   ├── hooks/           # Custom hooks (useChat)
    │   ├── lib/             # Axios instances and API wrappers
    │   └── pages/           # Route views (Feed, Ask, Chat, Admin)
    └── vite.config.js
```

---

## 13. Setup Instructions

### Prerequisites
- **Java 21**
- **Node.js 20+**
- **MySQL 8+**

### 1. Database Setup
Ensure your local MySQL instance is running and create the necessary schemas:
```sql
CREATE DATABASE doconnect_ai;
CREATE DATABASE doconnect_chat;
```

### 2. Environment Variables
Configure the `.env` files for each component. Refer to the `.env.example` files in each directory.

**Crucial Variables:**
- `JWT_SECRET`: Must be identical in both `backend` and `chat-service` (Min 32 chars).
- `NOTIFICATION_INTERNAL_TOKEN`: Shared secret for service-to-service communication.
- `GEMINI_API_KEY`: Required in the `backend` for all AI features to function.

### 3. Startup Order

**A. Start the Main Backend:**
```bash
cd backend
./mvnw spring-boot:run
```

**B. Start the Chat Microservice:**
```bash
cd chat-service
./mvnw spring-boot:run
```

**C. Start the Frontend:**
```bash
cd frontend
npm install
npm run dev
```
The application will be accessible at `http://localhost:5173`.

---

## 14. Developer

**Name:** Mithun Raj M R  
**Portfolio:** [mithunrajmr.netlify.app](https://mithunrajmr.netlify.app/)

I built DoConnect AI to explore how modern AI capabilities can be integrated into real-world community platforms using Spring Boot, React, WebSockets, and microservice-based architectures. As a software engineer focused on building resilient, full-stack systems, I developed DoConnect AI to demonstrate how modern architectures (Spring Boot microservices, React SPA) can integrate directly with cutting-edge AI (Google Gemini). My engineering philosophy prioritizes bridging the gap between high-level AI capabilities and tangible user value—moving beyond simple chat interfaces to embed intelligent features (like real-time moderation and semantic deduplication) deeply into the product's core workflows.

---

## 15. Future Improvements

1. **Email Integration:** Send offline notifications for accepted answers and direct replies using Spring Mail.
2. **Advanced Search:** Integrate Elasticsearch for typo-tolerant, full-text searching across questions and answers.
3. **Private Messaging:** Extend the chat microservice to support direct user-to-user DMs.
4. **Reputation System:** Implement a gamified point system to reward highly-rated answers.
5. **Dockerization:** Create a `docker-compose.yml` to instantly spin up the frontend, backends, and database simultaneously.
