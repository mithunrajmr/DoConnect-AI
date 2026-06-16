import { useState, useRef, useEffect } from 'react'
import { useNotifications } from '../context/NotificationContext'
import { Bell, BellRing, Check, CheckCircle2, Clock, Inbox } from 'lucide-react'

function formatRelative(isoString) {
  if (!isoString) return ''
  const diff = Date.now() - new Date(isoString).getTime()
  const mins  = Math.floor(diff / 60000)
  if (mins < 1)   return 'just now'
  if (mins < 60)  return `${mins}m ago`
  const hrs = Math.floor(mins / 60)
  if (hrs  < 24)  return `${hrs}h ago`
  const days = Math.floor(hrs  / 24)
  if (days < 30)  return `${days}d ago`
  return new Date(isoString).toLocaleDateString()
}

export default function NotificationDropdown() {
  const { notifications, unreadCount, markAsRead, markAllAsRead } = useNotifications()
  const [isOpen, setIsOpen] = useState(false)
  const dropdownRef = useRef(null)
  const hasUnread = unreadCount > 0

  useEffect(() => {
    function handleClickOutside(e) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setIsOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  return (
    <div style={{ position: 'relative' }} ref={dropdownRef}>

      {/* ── Bell button ── */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        aria-label={`Notifications${hasUnread ? ` (${unreadCount} unread)` : ''}`}
        style={{
          position: 'relative',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          width: '34px',
          height: '34px',
          borderRadius: '50%',
          border: hasUnread ? '1.5px solid rgba(149,128,255,0.6)' : '1.5px solid transparent',
          background: hasUnread
            ? 'rgba(124,106,255,0.15)'
            : 'rgba(255,255,255,0.05)',
          cursor: 'pointer',
          transition: 'all 0.2s',
          boxShadow: hasUnread ? '0 0 12px rgba(149,128,255,0.35)' : 'none',
          color: hasUnread ? '#9580ff' : '#aaa',
        }}
      >
        {/* Animated bell when unread */}
        {hasUnread
          ? <BellRing size={16} strokeWidth={2} />
          : <Bell size={16} strokeWidth={2} />
        }

        {/* Unread badge */}
        {hasUnread && (
          <span style={{
            position: 'absolute',
            top: '-4px',
            right: '-4px',
            minWidth: '18px',
            height: '18px',
            padding: '0 4px',
            background: '#ef4444',
            color: '#fff',
            fontSize: '0.65rem',
            fontWeight: 800,
            borderRadius: '999px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            lineHeight: 1,
            boxShadow: '0 0 0 2px #090909',
            letterSpacing: '-0.02em',
          }}>
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>

      {/* ── Dropdown panel ── */}
      {isOpen && (
        <div style={{
          position: 'absolute',
          top: 'calc(100% + 10px)',
          right: '0',
          width: 'calc(100vw - 24px)',
          maxWidth: '340px',
          maxHeight: 'min(440px, calc(100vh - 80px))',
          display: 'flex',
          flexDirection: 'column',
          borderRadius: '16px',
          background: '#131320',
          border: '1px solid rgba(149,128,255,0.25)',
          boxShadow: '0 20px 60px rgba(0,0,0,0.7), 0 0 0 1px rgba(149,128,255,0.08)',
          zIndex: 10000,
          overflow: 'hidden',
          backdropFilter: 'blur(20px)',
          WebkitBackdropFilter: 'blur(20px)',
        }}>

          {/* Header */}
          <div style={{
            padding: '14px 16px',
            borderBottom: '1px solid rgba(255,255,255,0.06)',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            background: 'rgba(149,128,255,0.05)',
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <BellRing size={15} color="#9580ff" strokeWidth={2} />
              <span style={{ fontSize: '0.9rem', fontWeight: 700, color: '#e8e8e8' }}>Notifications</span>
              {hasUnread && (
                <span style={{
                  fontSize: '0.65rem', fontWeight: 700, background: '#9580ff',
                  color: '#fff', borderRadius: '999px', padding: '1px 7px'
                }}>{unreadCount}</span>
              )}
            </div>
            {hasUnread && (
              <button
                onClick={markAllAsRead}
                style={{
                  display: 'flex', alignItems: 'center', gap: '4px',
                  background: 'none', border: '1px solid rgba(149,128,255,0.3)',
                  color: '#9580ff', fontSize: '0.72rem', fontWeight: 600,
                  borderRadius: '999px', padding: '6px 12px', cursor: 'pointer',
                  whiteSpace: 'nowrap', minHeight: '32px'
                }}
              >
                <CheckCircle2 size={12} />
                Mark all read
              </button>
            )}
          </div>

          {/* List */}
          <div style={{ overflowY: 'auto', flex: 1 }}>
            {notifications.length === 0 ? (
              <div style={{
                padding: '3rem 1rem', textAlign: 'center',
                color: '#555', display: 'flex', flexDirection: 'column',
                alignItems: 'center', gap: '0.5rem'
              }}>
                <Inbox size={28} strokeWidth={1.5} />
                <span style={{ fontSize: '0.85rem' }}>No notifications yet.</span>
              </div>
            ) : (
              notifications.map(n => (
                <div key={n.id} style={{
                  padding: '12px 16px',
                  borderBottom: '1px solid rgba(255,255,255,0.05)',
                  background: n.read ? 'transparent' : 'rgba(124,106,255,0.07)',
                  display: 'flex',
                  gap: '10px',
                  alignItems: 'flex-start',
                  transition: 'background 0.15s',
                }}>
                  {/* Unread dot */}
                  {!n.read && (
                    <div style={{
                      width: 7, height: 7, borderRadius: '50%',
                      background: '#9580ff', flexShrink: 0, marginTop: '6px',
                      boxShadow: '0 0 6px rgba(149,128,255,0.6)'
                    }} />
                  )}

                  <div style={{ flex: 1, paddingLeft: n.read ? '17px' : 0 }}>
                    <div style={{ fontSize: '0.85rem', color: '#e0e0e0', lineHeight: 1.45, marginBottom: '4px' }}>
                      {n.message}
                    </div>
                    <div style={{ fontSize: '0.7rem', color: '#555', display: 'flex', alignItems: 'center', gap: '4px' }}>
                      <Clock size={10} /> {formatRelative(n.createdAt)}
                    </div>
                  </div>

                  {!n.read && (
                    <button
                      onClick={() => markAsRead(n.id)}
                      title="Mark as read"
                      style={{
                        background: 'none', border: '1px solid rgba(149,128,255,0.25)',
                        color: '#9580ff', cursor: 'pointer', padding: '8px',
                        borderRadius: '6px', flexShrink: 0, minWidth: '32px', minHeight: '32px',
                        display: 'flex', alignItems: 'center', justifyContent: 'center'
                      }}
                    >
                      <Check size={12} />
                    </button>
                  )}
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  )
}
