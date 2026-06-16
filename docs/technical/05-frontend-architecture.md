# 05 - Frontend Architecture

## 1. Core Technologies
The frontend is a Single Page Application (SPA) built with:
*   **React 19:** Utilizing modern hooks and concurrent features.
*   **Vite 8:** Extremely fast Hot Module Replacement (HMR) and optimized production bundling.
*   **Tailwind CSS 4:** Utility-first styling.
*   **React Router DOM v7:** Client-side routing.
*   **Axios:** HTTP client for REST requests.
*   **@stomp/stompjs:** WebSocket client for real-time messaging.

## 2. Component Hierarchy & Routing

The routing is orchestrated in `App.jsx` using `BrowserRouter`. 

### 2.1 Route Protection
We utilize wrapper components to guard routes:
*   **`ProtectedRoute.jsx`:** Checks the `AuthContext`. If `isAuthenticated` is false, it redirects to `/login`.
*   **`AdminRoute.jsx`:** Specifically checks the user role. Only allows access to `/analytics` and `/moderation` if the role is `ADMIN` or `MODERATOR`.

### 2.2 Global Layout
Authenticated users hit the `AppLayout` component, which renders the persistent `NavBar` (containing search, profile menu, and the `NotificationDropdown`) alongside the dynamic `<Routes>`.

## 3. Global State Management (Context API)

Instead of introducing heavy state libraries like Redux, DoConnect AI uses React's native Context API.

### 3.1 `AuthContext.jsx`
*   **Purpose:** Manages the user session.
*   **Mechanism:** Hydrates `token` and `user` state from `localStorage` on mount.
*   **Functions Provided:** `login(token, user)`, `logout()`, `isAuthenticated`.

### 3.2 `NotificationContext.jsx`
*   **Purpose:** Maintains the global unread notification count and the active STOMP connection for user-specific alerts.
*   **Mechanism:** Connects to `/ws/notifications` on mount (if authenticated) and subscribes to `/user/queue/notifications`.

## 4. API & Networking Layer (`/lib`)

All direct network calls are abstracted out of UI components into the `/lib` directory (`axios.js`, `aiApi.js`, `chatApi.js`, etc.). This enforces separation of concerns.

### 4.1 Axios Interceptors
`lib/axios.js` defines a singleton Axios instance.
*   **Request Interceptor:** Automatically pulls `doconnect_token` from `localStorage` and appends `Authorization: Bearer <token>` to every outbound HTTP request.
*   **Response Interceptor:** Globally catches `401 Unauthorized` responses. If a JWT expires while a user is active, the interceptor wipes the local storage and forcefully redirects `window.location.href = '/login'`, gracefully handling session expiration.

## 5. Complex State: `useChat.js` Custom Hook

Real-time chat is complex to manage within React's render cycle (especially with Strict Mode double-mounting). This is encapsulated in `hooks/useChat.js`.

### 5.1 Lifecycle
1.  **Mount:** Fetches the last 50 messages via REST (`fetchChatHistory`) to immediately populate the screen.
2.  **Connect:** Initializes `@stomp/stompjs` `Client` targeting `ws://localhost:8090/ws`.
3.  **Authentication:** Passes the JWT in the STOMP `connectHeaders: { Authorization: "Bearer ..." }`.
4.  **Subscribe:** Listens to `/topic/chat/global`.
5.  **Deduplication:** When incoming STOMP messages arrive, the hook checks `prev.some((m) => m.id === incoming.id)` before appending. This prevents UI glitches if the REST history overlaps with incoming socket messages.
6.  **Cleanup:** Calls `client.deactivate()` on component unmount.

### 5.2 Exposed State
Components using this hook receive a clean API:
`{ messages, connectionState, error, historyLoading, sendMessage, isConnected }`

## 6. Design Tradeoffs

*   **Tradeoff: `localStorage` vs `HttpOnly` Cookies**
    *   *Decision:* JWTs are stored in `localStorage`.
    *   *Why:* While `HttpOnly` cookies mitigate Cross-Site Scripting (XSS) attacks, they complicate WebSocket authentication (which relies on sending the token in STOMP headers) and Cross-Origin Resource Sharing (CORS) across microservices. We chose `localStorage` for architectural simplicity and ease of passing tokens to the separate Chat Service, trading off some theoretical XSS risk.
*   **Tradeoff: Context vs Redux**
    *   *Decision:* React Context.
    *   *Why:* The state (Auth, Notifications) changes infrequently. A complex action-reducer paradigm would add unnecessary boilerplate for a project of this scope.

---
*Next Document: [06-backend-architecture.md](06-backend-architecture.md)*
