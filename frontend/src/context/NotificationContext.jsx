import { createContext, useContext, useState, useEffect, useRef, useCallback } from 'react'
import { createPortal } from 'react-dom'
import { Client } from '@stomp/stompjs'
import { 
  fetchNotifications, fetchUnreadCount, markAsRead, markAllAsRead, NOTIFY_WS_URL 
} from '../lib/notificationApi'
import { useAuth } from './AuthContext'
import { BellRing, X } from 'lucide-react'

const NotificationContext = createContext(null)

// ── Toast popup — injected directly into document.body via portal ─────
// All styling is hardcoded inline — no CSS class dependencies.
function ToastItem({ toast, onRemove }) {
  return (
    <div style={{
      display: 'flex',
      alignItems: 'flex-start',
      gap: '0.75rem',
      width: '320px',
      padding: '14px 16px',
      borderRadius: '14px',
      background: '#1a1a2e',
      border: '1px solid #9580ff',
      boxShadow: '0 0 0 1px rgba(149,128,255,0.15), 0 16px 48px rgba(0,0,0,0.8)',
      backdropFilter: 'blur(20px)',
      WebkitBackdropFilter: 'blur(20px)',
    }}>
      {/* Purple bell avatar */}
      <div style={{
        width: 36, height: 36, borderRadius: '50%',
        background: 'linear-gradient(135deg, #7c6aff, #9580ff)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        flexShrink: 0, boxShadow: '0 0 12px rgba(149,128,255,0.4)'
      }}>
        <BellRing size={16} color="#fff" />
      </div>

      {/* Text */}
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: '0.8rem', fontWeight: 700, color: '#9580ff', marginBottom: '3px', letterSpacing: '0.03em' }}>
          DoConnect AI
        </div>
        <div style={{ fontSize: '0.875rem', color: '#e8e8e8', lineHeight: 1.45 }}>
          {toast.message}
        </div>
      </div>

      {/* Close */}
      <button
        onClick={() => onRemove(toast.id)}
        style={{ background: 'none', border: 'none', color: '#666', cursor: 'pointer', padding: '2px', marginTop: '2px', flexShrink: 0, lineHeight: 0 }}
      >
        <X size={14} />
      </button>
    </div>
  )
}

function ToastPortal({ toasts, onRemove }) {
  // Render nothing when empty — but keep the portal alive to avoid remount costs
  return createPortal(
    <div style={{
      position: 'fixed',
      bottom: '24px',
      right: '24px',
      zIndex: 2147483647, // max possible z-index
      display: 'flex',
      flexDirection: 'column',
      gap: '10px',
      alignItems: 'flex-end',
      // NO pointer-events none here — we want clicks to work
    }}>
      {toasts.map((t, i) => (
        <div
          key={t.id}
          style={{
            // Slide-in done via CSS transform trick without keyframe dependency
            opacity: 1,
            transform: 'translateX(0)',
            transition: 'opacity 0.3s, transform 0.3s',
            animationFillMode: 'both',
          }}
        >
          <ToastItem toast={t} onRemove={onRemove} />
        </div>
      ))}
    </div>,
    document.body
  )
}

// ── Provider ─────────────────────────────────────────────────────────
export function NotificationProvider({ children }) {
  const { isAuthenticated } = useAuth()

  const [notifications, setNotifications] = useState([])
  const [unreadCount,   setUnreadCount]   = useState(0)
  const [toasts,        setToasts]        = useState([])

  const stompActiveRef = useRef(false)

  // ── Request browser notification permission once on login ─────────
  useEffect(() => {
    if (!isAuthenticated) return
    if ('Notification' in window && Notification.permission === 'default') {
      Notification.requestPermission()
    }
  }, [isAuthenticated])

  // ── Initial REST load ─────────────────────────────────────────────
  useEffect(() => {
    if (!isAuthenticated) return
    let cancelled = false

    async function loadInitial() {
      try {
        const [nots, countRes] = await Promise.all([
          fetchNotifications(),
          fetchUnreadCount(),
        ])
        if (cancelled) return
        setNotifications(nots)
        setUnreadCount(countRes?.unreadCount ?? countRes ?? 0)
      } catch (err) {
        console.error('[Notifications] initial load failed:', err)
      }
    }

    loadInitial()
    return () => { cancelled = true }
  }, [isAuthenticated])

  // ── STOMP WebSocket ───────────────────────────────────────────────
  useEffect(() => {
    if (!isAuthenticated) return

    const token = localStorage.getItem('doconnect_token')
    if (!token) return

    stompActiveRef.current = true

    const client = new Client({
      brokerURL: NOTIFY_WS_URL,
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,

      onConnect: () => {
        if (!stompActiveRef.current) {
          client.deactivate()
          return
        }

        client.subscribe('/user/queue/notifications', (frame) => {
          if (!stompActiveRef.current) return
          try {
            const notif = JSON.parse(frame.body)

            setNotifications(prev => [notif, ...prev])
            setUnreadCount(prev => prev + 1)

            // ── Native OS notification ────────────────────────────
            if ('Notification' in window && Notification.permission === 'granted') {
              const n = new Notification('DoConnect AI', {
                body: notif.message,
                icon: '/favicon.svg',
                tag: `dc-${notif.id ?? Date.now()}`,
              })
              n.onclick = () => { window.focus(); n.close() }
              setTimeout(() => n.close(), 6000)
            }

            // ── In-app toast popup ────────────────────────────────
            const toastId = `${Date.now()}-${Math.random()}`
            setToasts(prev => [...prev, { id: toastId, ...notif }])
            setTimeout(() => {
              setToasts(prev => prev.filter(t => t.id !== toastId))
            }, 5000)

          } catch (e) {
            console.error('[Notifications] parse error:', e)
          }
        })

        client.subscribe('/user/queue/notifications/unread-count', (frame) => {
          if (!stompActiveRef.current) return
          try {
            const c = JSON.parse(frame.body)
            setUnreadCount(c?.unreadCount ?? c ?? 0)
          } catch {}
        })
      },

      onStompError: (f) => {
        console.error('[Notifications] STOMP error:', f.headers, f.body)
      },
      onWebSocketError: (e) => {
        console.error('[Notifications] WebSocket error:', e)
      },
      onWebSocketClose: (e) => {
        if (e.code !== 1000) {
          console.warn('[Notifications] WebSocket closed — code:', e.code, '| reason:', e.reason || '(no reason)')
        }
      },
      onDisconnect: () => {},
    })

    client.activate()
    return () => {
      stompActiveRef.current = false
      client.deactivate()
    }
  }, [isAuthenticated])

  // ── Actions ───────────────────────────────────────────────────────
  const handleMarkAsRead = useCallback(async (id) => {
    try {
      await markAsRead(id)
      setNotifications(prev => prev.map(n => n.id === id ? { ...n, read: true } : n))
      setUnreadCount(prev => Math.max(0, prev - 1))
    } catch (err) {
      console.error('[Notifications] mark read failed:', err)
    }
  }, [])

  const handleMarkAllAsRead = useCallback(async () => {
    try {
      await markAllAsRead()
      setNotifications(prev => prev.map(n => ({ ...n, read: true })))
      setUnreadCount(0)
    } catch (err) {
      console.error('[Notifications] mark all read failed:', err)
    }
  }, [])

  const removeToast = useCallback((id) => {
    setToasts(prev => prev.filter(t => t.id !== id))
  }, [])

  return (
    <NotificationContext.Provider value={{
      notifications,
      unreadCount,
      markAsRead:    handleMarkAsRead,
      markAllAsRead: handleMarkAllAsRead,
    }}>
      {children}
      <ToastPortal toasts={toasts} onRemove={removeToast} />
    </NotificationContext.Provider>
  )
}

// eslint-disable-next-line react-refresh/only-export-components
export function useNotifications() {
  const ctx = useContext(NotificationContext)
  if (!ctx) throw new Error('useNotifications must be used inside <NotificationProvider>')
  return ctx
}
