/**
 * Chat service API helpers.
 *
 * REST base: /api/chat (Vite proxies /api/chat → http://localhost:8090, stripping the prefix)
 * WebSocket:  ws://localhost:8090/ws  (direct — raw STOMP, no SockJS)
 *
 * Auth: the same JWT token stored under 'doconnect_token' is used for both
 * the REST Authorization header and the STOMP CONNECT frame.
 */

import axios from 'axios'

// Build the WebSocket URL from the current page's host so it always goes
// through the Vite dev-server proxy (/ws-chat → ws://localhost:8090/ws).
// This avoids the Origin mismatch that occurs when Vite binds to a
// different port than 5173 (e.g. 5174) and the chat service rejects the
// WebSocket upgrade because that port is not in its allowed-origins list.
const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
const CHAT_WS_URL = `${wsProtocol}//${window.location.host}/ws-chat`

/** Vite proxies /chat-api → http://localhost:8090/api/chat */
export const chatHttp = axios.create({
  baseURL: import.meta.env.VITE_CHAT_API_URL || '/chat-api',
  headers: { 'Content-Type': 'application/json' },
})

chatHttp.interceptors.request.use((config) => {
  const token = localStorage.getItem('doconnect_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/**
 * Fetch chat message history.
 * GET /api/chat/messages?limit=<n>
 * Returns ChatMessageResponse[] * GET /messages?limit=<n>
 */
export async function fetchChatHistory(limit = 50) {
  const { data } = await chatHttp.get('/messages', { params: { limit } })
  return data
}

export { CHAT_WS_URL }
