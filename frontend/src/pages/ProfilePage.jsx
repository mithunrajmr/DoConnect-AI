import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import api from '../lib/axios'
import { User, Shield, Calendar, MessageSquare, Edit3, Check, X } from 'lucide-react'

function formatDate(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })
}

function roleBadge(role) {
  if (role === 'ADMIN')     return 'ADMIN'
  if (role === 'MODERATOR') return 'MOD'
  return 'USER'
}

export default function ProfilePage() {
  const { user: authUser, login } = useAuth()

  const [profile,     setProfile]     = useState(null)
  const [myQuestions, setMyQuestions] = useState([])
  const [loading,     setLoading]     = useState(true)
  const [editMode,    setEditMode]    = useState(false)
  const [form,        setForm]        = useState({ name: '', password: '' })
  const [formErrors,  setFormErrors]  = useState({})
  const [saving,      setSaving]      = useState(false)
  const [saveError,   setSaveError]   = useState('')
  const [saveOk,      setSaveOk]      = useState(false)
  const [qLoading,    setQLoading]    = useState(true)

  useEffect(() => {
    let cancelled = false

    async function loadProfile() {
      try {
        // GET /api/auth/me — returns UserResponse
        const { data } = await api.get('/auth/me')
        if (!cancelled) {
          setProfile(data)
          setForm({ name: data.name, password: '' })
        }
      } catch {
        // Use cached authUser as fallback
        if (!cancelled && authUser) {
          setProfile(authUser)
          setForm({ name: authUser.name, password: '' })
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    async function loadMyQuestions() {
      try {
        // GET /api/questions — returns QuestionResponse[], filter by author.id
        const { data } = await api.get('/questions')
        if (!cancelled) {
          setMyQuestions(data.filter(q => q.author?.id === authUser?.id))
        }
      } catch {
        // Non-fatal
      } finally {
        if (!cancelled) setQLoading(false)
      }
    }

    loadProfile()
    loadMyQuestions()
    return () => { cancelled = true }
  }, [authUser?.id])

  function handleFormChange(e) {
    setForm(f => ({ ...f, [e.target.name]: e.target.value }))
    setFormErrors(er => ({ ...er, [e.target.name]: '' }))
    setSaveError('')
    setSaveOk(false)
  }

  function validateForm() {
    const errs = {}
    if (!form.name.trim())       errs.name = 'Name is required'
    if (form.name.length > 120)  errs.name = 'Name must be 120 characters or fewer'
    if (form.password && form.password.length > 0) {
      if (form.password.length < 8)  errs.password = 'Password must be at least 8 characters'
      if (form.password.length > 72) errs.password = 'Password must be 72 characters or fewer'
    }
    return errs
  }

  async function handleSave(e) {
    e.preventDefault()
    const errs = validateForm()
    if (Object.keys(errs).length) { setFormErrors(errs); return }

    setSaving(true)
    setSaveError('')
    setSaveOk(false)

    // PUT /api/auth/profile — body: { name, password? }
    const payload = { name: form.name.trim() }
    if (form.password.trim()) payload.password = form.password

    try {
      const { data } = await api.put('/auth/profile', payload)
      setProfile(data)
      // Refresh the cached token user (token stays the same, just user data updated)
      login(localStorage.getItem('doconnect_token'), data)
      setForm({ name: data.name, password: '' })
      setSaveOk(true)
      setEditMode(false)
    } catch (err) {
      const msg = err.response?.data?.message ?? 'Failed to save profile.'
      setSaveError(msg)
    } finally {
      setSaving(false)
    }
  }

  function cancelEdit() {
    setEditMode(false)
    setForm({ name: profile?.name ?? '', password: '' })
    setFormErrors({})
    setSaveError('')
    setSaveOk(false)
  }

  return (
    <div className="page-container">
      <h1 style={{ fontSize: '1.25rem', fontWeight: 700, letterSpacing: '-0.02em', marginBottom: '1.5rem' }}>
        Profile
      </h1>

      {loading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: '5rem 0' }}>
          <span className="spinner" style={{ width: 28, height: 28 }} />
        </div>
      ) : (
        <>
      {/* Identity card */}
      <div className="glass fade-up" style={{ padding: '1.75rem', marginBottom: '1.25rem' }}>
        <div style={{ display: 'flex', alignItems: 'flex-start', gap: '1.25rem', flexWrap: 'wrap' }}>
          {/* Avatar */}
          <div style={{
            width: 56, height: 56, borderRadius: '50%',
            background: 'var(--accent-dim)',
            border: '1px solid rgba(124,106,255,0.3)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            flexShrink: 0,
          }}>
            <User size={24} strokeWidth={1.8} color="var(--accent-hover)" />
          </div>

          <div style={{ flex: 1 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.2rem', flexWrap: 'wrap' }}>
              <span style={{ fontWeight: 700, fontSize: '1.05rem' }}>{profile?.name}</span>
              <span className="badge badge-role">{roleBadge(profile?.role)}</span>
            </div>
            <p style={{ fontSize: '0.84rem', color: 'var(--text-secondary)' }}>{profile?.email}</p>
            <div style={{ display: 'flex', gap: '1rem', marginTop: '0.5rem', flexWrap: 'wrap' }}>
              <span className="stat-chip">
                <Calendar size={13} strokeWidth={1.8} />
                Joined {formatDate(profile?.createdAt)}
              </span>
              <span className="stat-chip">
                <MessageSquare size={13} strokeWidth={1.8} />
                {myQuestions.length} question{myQuestions.length !== 1 ? 's' : ''}
              </span>
            </div>
          </div>

          {!editMode && (
            <button
              className="btn-ghost"
              onClick={() => setEditMode(true)}
              id="edit-profile-btn"
              style={{ flexShrink: 0 }}
            >
              <Edit3 size={14} strokeWidth={2} /> Edit
            </button>
          )}
        </div>

        {/* Edit form */}
        {editMode && (
          <div style={{ marginTop: '1.5rem', paddingTop: '1.25rem', borderTop: '1px solid var(--border)' }}>
            {saveError && <div className="alert-error" style={{ marginBottom: '1rem' }}>{saveError}</div>}
            {saveOk && (
              <div style={{ padding: '0.6rem 1rem', borderRadius: 10, background: 'rgba(52,211,153,0.08)', border: '1px solid rgba(52,211,153,0.25)', color: 'var(--success)', fontSize: '0.83rem', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                <Check size={14} strokeWidth={2} /> Profile updated successfully.
              </div>
            )}
            <form onSubmit={handleSave} id="update-profile-form" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label htmlFor="profile-name" className="field-label">Display name</label>
                <input
                  id="profile-name"
                  name="name"
                  type="text"
                  className="input-pill"
                  value={form.name}
                  onChange={handleFormChange}
                  maxLength={120}
                />
                {formErrors.name && <p className="error-text">{formErrors.name}</p>}
              </div>
              <div>
                <label htmlFor="profile-password" className="field-label">
                  New password <span style={{ color: 'var(--text-muted)', fontWeight: 400 }}>(leave blank to keep current)</span>
                </label>
                <input
                  id="profile-password"
                  name="password"
                  type="password"
                  autoComplete="new-password"
                  className="input-pill"
                  placeholder="Min 8 characters"
                  value={form.password}
                  onChange={handleFormChange}
                  maxLength={72}
                />
                {formErrors.password && <p className="error-text">{formErrors.password}</p>}
              </div>
              <div style={{ display: 'flex', gap: '0.75rem' }}>
                <button id="save-profile-btn" type="submit" className="btn-primary" disabled={saving}>
                  {saving
                    ? <><span className="spinner" style={{ width: 16, height: 16 }} /> Saving…</>
                    : <><Check size={14} strokeWidth={2} /> Save changes</>}
                </button>
                <button type="button" className="btn-ghost" onClick={cancelEdit} id="cancel-edit-btn">
                  <X size={14} strokeWidth={2} /> Cancel
                </button>
              </div>
            </form>
          </div>
        )}
      </div>

      {/* My questions */}
      <div>
        <p className="section-head">My questions ({myQuestions.length})</p>
        {qLoading && (
          <div style={{ display: 'flex', justifyContent: 'center', padding: '2rem 0' }}>
            <span className="spinner" />
          </div>
        )}
        {!qLoading && myQuestions.length === 0 && (
          <div className="empty-state glass" style={{ padding: '2.5rem' }}>
            <MessageSquare size={24} strokeWidth={1.4} style={{ marginBottom: '0.75rem', color: 'var(--text-muted)' }} />
            <h3>No questions yet</h3>
            <p style={{ fontSize: '0.82rem' }}>Questions you post will appear here.</p>
            <Link to="/ask" className="btn-primary" style={{ marginTop: '1rem', display: 'inline-flex' }}>
              Ask your first question
            </Link>
          </div>
        )}
        {!qLoading && myQuestions.length > 0 && (
          <div className="glass" style={{ overflow: 'hidden', padding: 0 }}>
            {myQuestions.map(q => (
              <Link
                key={q.id}
                to={`/questions/${q.id}`}
                className="q-card"
                id={`profile-question-${q.id}`}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  <span className={`badge ${q.status === 'OPEN' ? 'badge-open' : q.status === 'ANSWERED' ? 'badge-answered' : 'badge-closed'}`}>
                    {q.status}
                  </span>
                  <span style={{ fontSize: '0.9rem', fontWeight: 500, flex: 1 }}>{q.title}</span>
                  <span className="stat-chip" style={{ flexShrink: 0 }}>
                    <MessageSquare size={12} strokeWidth={1.8} />{q.answerCount}
                  </span>
                </div>
                {q.tags?.length > 0 && (
                  <div style={{ display: 'flex', gap: '0.3rem', marginTop: '0.5rem', flexWrap: 'wrap' }}>
                    {q.tags.map(t => <span key={t} className="tag">{t}</span>)}
                  </div>
                )}
              </Link>
            ))}
          </div>
        )}
      </div>
        </>
      )}
    </div>
  )
}
