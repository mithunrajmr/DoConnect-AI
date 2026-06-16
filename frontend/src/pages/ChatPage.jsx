import { useState, useEffect, useRef } from 'react'
import { useAuth } from '../context/AuthContext'
import { useChat, ConnectionState } from '../hooks/useChat'
import {
  MessageCircle, Send, Wifi, WifiOff, Loader, AlertCircle, Users
} from 'lucide-react'

/* ── Helpers ──────────────────────────────────────────────────────── */
function formatTime(isoString) {
  if (!isoString) return ''
  const d = new Date(isoString)
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

function formatDate(isoString) {
  if (!isoString) return ''
  const d = new Date(isoString)
  const today = new Date()
  if (d.toDateString() === today.toDateString()) return 'Today'
  const yesterday = new Date(today)
  yesterday.setDate(today.getDate() - 1)
  if (d.toDateString() === yesterday.toDateString()) return 'Yesterday'
  return d.toLocaleDateString([], { month: 'short', day: 'numeric', year: 'numeric' })
}

/** Group consecutive messages by the same sender and insert date separators. */
function groupMessages(messages) {
  const groups = []
  let lastDate = null
  let lastSender = null
  let lastGroup = null

  for (const msg of messages) {
    const msgDate = new Date(msg.createdAt).toDateString()
    if (msgDate !== lastDate) {
      groups.push({ type: 'date', label: formatDate(msg.createdAt), key: `date-${msg.createdAt}` })
      lastDate = msgDate
      lastSender = null
      lastGroup = null
    }
    if (msg.senderId !== lastSender) {
      lastGroup = { type: 'group', senderId: msg.senderId, username: msg.username, messages: [] }
      groups.push(lastGroup)
      lastSender = msg.senderId
    }
    lastGroup.messages.push(msg)
  }
  return groups
}

/* ── Connection status badge ─────────────────────────────────────── */
function ConnectionBadge({ state }) {
  const configs = {
    [ConnectionState.CONNECTED]:    { icon: <Wifi    size={12} strokeWidth={2} />, label: 'Connected',   color: 'var(--success)' },
    [ConnectionState.CONNECTING]:   { icon: <Loader  size={12} strokeWidth={2} style={{ animation: 'spin 1s linear infinite' }} />, label: 'Connecting…', color: 'var(--warning)' },
    [ConnectionState.ERROR]:        { icon: <WifiOff size={12} strokeWidth={2} />, label: 'Reconnecting…', color: 'var(--danger)' },
    [ConnectionState.DISCONNECTED]: { icon: <WifiOff size={12} strokeWidth={2} />, label: 'Disconnected',  color: 'var(--text-muted)' },
    [ConnectionState.IDLE]:         { icon: <Loader  size={12} strokeWidth={2} />, label: 'Starting…',    color: 'var(--text-muted)' },
  }
  const cfg = configs[state] ?? configs[ConnectionState.IDLE]
  return (
    <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', fontSize: '0.75rem', color: cfg.color, fontWeight: 500 }}>
      {cfg.icon}
      {cfg.label}
    </span>
  )
}

/* ── Single message bubble ────────────────────────────────────────── */
function MessageBubble({ msg, isSelf }) {
  return (
    <div
      id={`chat-msg-${msg.id}`}
      style={{
        maxWidth: '72%',
        alignSelf: isSelf ? 'flex-end' : 'flex-start',
        animation: 'fadeUp 0.2s ease both',
      }}
    >
      <div
        style={{
          padding: '0.55rem 0.9rem',
          borderRadius: isSelf ? '14px 14px 4px 14px' : '14px 14px 14px 4px',
          background: isSelf ? 'var(--accent)' : 'var(--surface-1)',
          border: `1px solid ${isSelf ? 'rgba(124,106,255,0.5)' : 'var(--border)'}`,
          color: isSelf ? '#fff' : 'var(--text-primary)',
          fontSize: '0.875rem',
          lineHeight: 1.5,
          wordBreak: 'break-word',
          whiteSpace: 'pre-wrap',
        }}
      >
        {msg.content}
      </div>
      <div style={{
        fontSize: '0.7rem',
        color: 'var(--text-muted)',
        marginTop: '0.2rem',
        textAlign: isSelf ? 'right' : 'left',
        paddingLeft: isSelf ? 0 : '0.15rem',
        paddingRight: isSelf ? '0.15rem' : 0,
      }}>
        {formatTime(msg.createdAt)}
      </div>
    </div>
  )
}

/* ── Message group (same sender) ─────────────────────────────────── */
function MessageGroup({ group, currentUserId }) {
  const isSelf = group.senderId === currentUserId
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.3rem', alignItems: isSelf ? 'flex-end' : 'flex-start' }}>
      {!isSelf && (
        <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-secondary)', marginLeft: '0.15rem' }}>
          {group.username}
        </span>
      )}
      {group.messages.map((msg) => (
        <MessageBubble key={msg.id} msg={msg} isSelf={isSelf} />
      ))}
    </div>
  )
}

/* ── Date separator ──────────────────────────────────────────────── */
function DateSeparator({ label }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', margin: '0.5rem 0' }}>
      <div style={{ flex: 1, height: '1px', background: 'var(--border)' }} />
      <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)', fontWeight: 500, whiteSpace: 'nowrap' }}>{label}</span>
      <div style={{ flex: 1, height: '1px', background: 'var(--border)' }} />
    </div>
  )
}

/* ── Main ChatPage ────────────────────────────────────────────────── */
export default function ChatPage() {
  const { user } = useAuth()
  const { messages, connectionState, error, historyLoading, sendMessage, isConnected } = useChat()

  const [draft,     setDraft]     = useState('')
  const [sendError, setSendError] = useState('')
  const messagesEndRef = useRef(null)
  const textareaRef    = useRef(null)

  // Auto-scroll to bottom when new messages arrive
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  function handleSend(e) {
    e.preventDefault()
    const trimmed = draft.trim()
    if (!trimmed) return

    setSendError('')
    const ok = sendMessage(trimmed)
    if (ok) {
      setDraft('')
      textareaRef.current?.focus()
    } else {
      setSendError(error || 'Message could not be sent.')
    }
  }

  function handleKeyDown(e) {
    // Ctrl+Enter or Cmd+Enter sends; bare Enter adds newline
    if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
      handleSend(e)
    }
  }

  const groups = groupMessages(messages)
  const charsLeft = 1000 - draft.length

  return (
    <div style={{ maxWidth: 760, margin: '0 auto', display: 'flex', flexDirection: 'column', height: 'calc(100dvh - 56px)', padding: '1rem' }}>

      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem', flexShrink: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
          <div style={{ width: 36, height: 36, borderRadius: '50%', background: 'var(--accent-dim)', border: '1px solid rgba(124,106,255,0.3)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Users size={16} color="var(--accent-hover)" strokeWidth={2} />
          </div>
          <div>
            <h1 style={{ fontSize: '1rem', fontWeight: 700, letterSpacing: '-0.02em', lineHeight: 1.2 }}>Global Chat</h1>
            <div style={{ marginTop: '0.1rem' }}>
              <ConnectionBadge state={connectionState} />
            </div>
          </div>
        </div>
        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
          {messages.length} message{messages.length !== 1 ? 's' : ''}
        </span>
      </div>

      {/* Connection error banner */}
      {error && connectionState !== ConnectionState.CONNECTED && (
        <div className="alert-error" style={{ marginBottom: '0.75rem', flexShrink: 0, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <AlertCircle size={14} strokeWidth={2} style={{ flexShrink: 0 }} />
          {error}
        </div>
      )}

      {/* Message area */}
      <div
        className="glass fade-up"
        style={{
          flex: 1,
          overflow: 'hidden',
          display: 'flex',
          flexDirection: 'column',
          marginBottom: '0.75rem',
          minHeight: 0,
        }}
      >
        <div
          id="chat-message-list"
          style={{
            flex: 1,
            overflowY: 'auto',
            padding: '1.25rem',
            display: 'flex',
            flexDirection: 'column',
            gap: '0.6rem',
            scrollbarWidth: 'thin',
            scrollbarColor: 'var(--surface-2) transparent',
          }}
        >
          {/* Loading state */}
          {historyLoading && (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flex: 1, gap: '0.6rem', color: 'var(--text-muted)', fontSize: '0.875rem' }}>
              <span className="spinner" /> Loading history…
            </div>
          )}

          {/* Empty state */}
          {!historyLoading && messages.length === 0 && connectionState === ConnectionState.CONNECTED && (
            <div className="empty-state" style={{ padding: '3rem 1rem', flex: 1 }}>
              <MessageCircle size={28} strokeWidth={1.4} style={{ marginBottom: '0.75rem', color: 'var(--text-muted)' }} />
              <h3>No messages yet</h3>
              <p style={{ fontSize: '0.82rem' }}>Be the first to say something.</p>
            </div>
          )}

          {/* Messages */}
          {!historyLoading && groups.map((group) =>
            group.type === 'date'
              ? <DateSeparator key={group.key} label={group.label} />
              : <MessageGroup key={group.messages[0].id} group={group} currentUserId={user?.id} />
          )}

          {/* Scroll anchor */}
          <div ref={messagesEndRef} />
        </div>
      </div>

      {/* Compose area */}
      <div className="glass fade-up" style={{ padding: '1rem', flexShrink: 0 }}>
        {sendError && (
          <div className="alert-error" style={{ marginBottom: '0.75rem', fontSize: '0.8rem' }}>
            {sendError}
          </div>
        )}
        <form onSubmit={handleSend} id="chat-compose-form">
          <div style={{ display: 'flex', gap: '0.6rem', alignItems: 'flex-end' }}>
            <div style={{ flex: 1 }}>
              <textarea
                id="chat-message-input"
                ref={textareaRef}
                className="input-rect"
                placeholder={isConnected ? 'Write a message… (Ctrl+Enter to send)' : 'Connecting to chat…'}
                value={draft}
                onChange={(e) => { setDraft(e.target.value); setSendError('') }}
                onKeyDown={handleKeyDown}
                disabled={!isConnected}
                maxLength={1000}
                rows={2}
                style={{ minHeight: 'unset', resize: 'none', borderRadius: 'var(--radius-md)' }}
              />
              <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '0.25rem' }}>
                <span style={{ fontSize: '0.7rem', color: charsLeft < 100 ? 'var(--warning)' : 'var(--text-muted)' }}>
                  {charsLeft} / 1000
                </span>
              </div>
            </div>
            <button
              id="chat-send-btn"
              type="submit"
              className="btn-primary"
              disabled={!isConnected || !draft.trim()}
              style={{ padding: '0.65rem 1.1rem', alignSelf: 'flex-start', marginTop: '0' }}
            >
              <Send size={15} strokeWidth={2} />
              Send
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
