import { useState } from 'react'
import { NavLink, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { MessageCircle, LogOut, User, PlusCircle, Zap, Menu, X } from 'lucide-react'
import NotificationDropdown from './NotificationDropdown'

export default function NavBar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false)

  function handleLogout() {
    logout()
    setIsMobileMenuOpen(false)
    navigate('/login')
  }

  function closeMenu() {
    setIsMobileMenuOpen(false)
  }

  const linkClass = ({ isActive }) => 'nav-link' + (isActive ? ' active' : '')

  return (
    <nav className="nav-bar">
      <div className="nav-inner">
        <NavLink to="/" className="nav-logo">
          <Zap size={16} strokeWidth={2} />
          DoConnect<span>AI</span>
        </NavLink>

        <div className="nav-spacer" />

        <NotificationDropdown />

        <button 
          className="mobile-menu-btn" 
          onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
          aria-label="Toggle menu"
        >
          {isMobileMenuOpen ? <X size={20} /> : <Menu size={20} />}
        </button>

        <div className={`nav-links ${isMobileMenuOpen ? 'open' : ''}`}>
          <NavLink to="/" end className={linkClass} onClick={closeMenu}>
            Feed
          </NavLink>

          <NavLink to="/ask" className={linkClass} onClick={closeMenu}>
            <PlusCircle size={14} strokeWidth={2} style={{ display: 'inline', verticalAlign: 'middle', marginRight: 3 }} />
            Ask
          </NavLink>

          <NavLink to="/chat" className={linkClass} id="nav-chat-link" onClick={closeMenu}>
            <MessageCircle size={14} strokeWidth={2} style={{ display: 'inline', verticalAlign: 'middle', marginRight: 3 }} />
            Chat
          </NavLink>

          {user?.role === 'ADMIN' && (
            <>
              <NavLink to="/analytics" className={linkClass} onClick={closeMenu}>
                Analytics
              </NavLink>
              <NavLink to="/moderation" className={linkClass} onClick={closeMenu}>
                Moderation
              </NavLink>
            </>
          )}

          <NavLink to="/profile" className={linkClass} onClick={closeMenu}>
            <User size={14} strokeWidth={2} style={{ display: 'inline', verticalAlign: 'middle', marginRight: 3 }} />
            {user?.name?.split(' ')[0] ?? 'Profile'}
          </NavLink>

          <button
            className="btn-ghost"
            onClick={handleLogout}
            style={{ padding: '0.35rem 0.75rem', fontSize: '0.82rem' }}
            id="nav-logout-btn"
          >
            <LogOut size={14} strokeWidth={2} />
            Sign out
          </button>
        </div>
      </div>
    </nav>
  )
}
