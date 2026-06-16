import { useState, useEffect, useRef, useCallback } from 'react'
import { Client } from '@stomp/stompjs'
import { CHAT_WS_URL, fetchChatHistory } from '../lib/chatApi'

/**
 * Connection states mirroring STOMP lifecycle.
 */
export const ConnectionState = {
  IDLE: 'IDLE',
  CONNECTING: 'CONNECTING',
  CONNECTED: 'CONNECTED',
  DISCONNECTED: 'DISCONNECTED',
  ERROR: 'ERROR',
}

/**
 * useChat — manages the entire chat lifecycle:
 *   1. Fetches REST history on mount.
 *   2. Connects to STOMP/WebSocket using the JWT from localStorage.
 *   3. Subscribes to /topic/chat/global for real-time messages.
 *   4. Exposes sendMessage() for outbound messages.
 *   5. Handles connection errors and graceful cleanup on unmount.
 */
export function useChat() {
  const [messages,        setMessages]        = useState([])
  const [connectionState, setConnectionState] = useState(ConnectionState.IDLE)
  const [error,           setError]           = useState(null)
  const [historyLoading,  setHistoryLoading]  = useState(true)

  const clientRef    = useRef(null)
  const mountedRef   = useRef(true)

  // ── Fetch REST history ────────────────────────────────────────────
  useEffect(() => {
    mountedRef.current = true
    let cancelled = false

    async function loadHistory() {
      try {
        const history = await fetchChatHistory(50)
        if (!cancelled && mountedRef.current) {
          setMessages(history)
        }
      } catch (err) {
        if (!cancelled && mountedRef.current) {
          setError('Could not load message history.')
          console.error('[Chat] history fetch error:', err)
        }
      } finally {
        if (!cancelled && mountedRef.current) {
          setHistoryLoading(false)
        }
      }
    }

    loadHistory()
    return () => { cancelled = true }
  }, [])

  // ── STOMP connection ──────────────────────────────────────────────
  useEffect(() => {
    const token = localStorage.getItem('doconnect_token')
    if (!token) {
      setError('Authentication token missing.')
      return
    }

    setConnectionState(ConnectionState.CONNECTING)

    const client = new Client({
      brokerURL: CHAT_WS_URL,
      /**
       * The WebSocketAuthChannelInterceptor on the server reads
       * the "Authorization" header from the STOMP CONNECT frame.
       */
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,

      onConnect: () => {
        if (!mountedRef.current) return
        setConnectionState(ConnectionState.CONNECTED)
        setError(null)

        // Subscribe to the global chat topic
        client.subscribe('/topic/chat/global', (stompMessage) => {
          if (!mountedRef.current) return
          try {
            const incoming = JSON.parse(stompMessage.body)
            setMessages((prev) => {
              // Deduplicate by id — avoids double-render in React StrictMode
              if (prev.some((m) => m.id === incoming.id)) return prev
              return [...prev, incoming]
            })
          } catch (parseErr) {
            console.error('[Chat] failed to parse incoming message:', parseErr)
          }
        })
      },

      onStompError: (frame) => {
        if (!mountedRef.current) return
        console.error('[Chat] STOMP error:', frame)
        setConnectionState(ConnectionState.ERROR)
        setError('Chat server error. Attempting to reconnect…')
      },

      onWebSocketError: (evt) => {
        if (!mountedRef.current) return
        console.error('[Chat] WebSocket error:', evt)
        setConnectionState(ConnectionState.ERROR)
        setError('Cannot reach chat server. Is it running on port 8090?')
      },

      onDisconnect: () => {
        if (!mountedRef.current) return
        setConnectionState(ConnectionState.DISCONNECTED)
      },
    })

    clientRef.current = client
    client.activate()

    return () => {
      mountedRef.current = false
      client.deactivate()
    }
  }, []) // Run once on mount

  // ── Send message ──────────────────────────────────────────────────
  const sendMessage = useCallback((content) => {
    const client = clientRef.current
    if (!client || !client.connected) {
      setError('Not connected. Please wait for the connection to establish.')
      return false
    }
    if (!content || content.trim().length === 0) return false
    if (content.trim().length > 1000) {
      setError('Message must be 1000 characters or fewer.')
      return false
    }

    /**
     * Send to /app/chat.send — matches @MessageMapping("/chat.send")
     * Payload matches ChatMessageRequest: { content: string }
     */
    client.publish({
      destination: '/app/chat.send',
      body: JSON.stringify({ content: content.trim() }),
    })
    return true
  }, [])

  return {
    messages,
    connectionState,
    error,
    historyLoading,
    sendMessage,
    isConnected: connectionState === ConnectionState.CONNECTED,
  }
}
