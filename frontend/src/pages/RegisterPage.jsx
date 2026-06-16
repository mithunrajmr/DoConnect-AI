import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import api from '../lib/axios'
import { Zap, Eye, EyeOff } from 'lucide-react'

export default function RegisterPage() {
  const { login } = useAuth()
  const navigate  = useNavigate()

  const [form,    setForm]    = useState({ name: '', email: '', password: '' })
  const [errors,  setErrors]  = useState({})
  const [apiError,setApiError]= useState('')
  const [loading, setLoading] = useState(false)
  const [showPw,  setShowPw]  = useState(false)

  function handleChange(e) {
    setForm(f => ({ ...f, [e.target.name]: e.target.value }))
    setErrors(er => ({ ...er, [e.target.name]: '' }))
    setApiError('')
  }

  function validate() {
    const errs = {}
    if (!form.name.trim())              errs.name     = 'Name is required'
    if (form.name.length > 120)         errs.name     = 'Name must be 120 characters or fewer'
    if (!form.email.trim())             errs.email    = 'Email is required'
    if (!form.password)                 errs.password = 'Password is required'
    if (form.password.length < 8)       errs.password = 'Password must be at least 8 characters'
    if (form.password.length > 72)      errs.password = 'Password must be 72 characters or fewer'
    return errs
  }

  async function handleSubmit(e) {
    e.preventDefault()
    const errs = validate()
    if (Object.keys(errs).length) { setErrors(errs); return }

    setLoading(true)
    setApiError('')
    try {
      // POST /api/auth/register — body: { name, email, password }
      const { data } = await api.post('/auth/register', {
        name:     form.name,
        email:    form.email,
        password: form.password,
      })
      // AuthResponse: { token: string, user: UserResponse }
      login(data.token, data.user)
      navigate('/', { replace: true })
    } catch (err) {
      const serverMsg = err.response?.data?.message
      const details   = err.response?.data?.details
      setApiError(details?.join('. ') ?? serverMsg ?? 'Registration failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-container">
      <div className="glass fade-up" style={{ width: '100%', maxWidth: 420, padding: '2.5rem' }}>
        {/* Logo */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '2rem' }}>
          <Zap size={18} color="var(--accent)" strokeWidth={2.5} />
          <span style={{ fontWeight: 700, fontSize: '1rem', letterSpacing: '-0.02em' }}>
            DoConnect<span style={{ color: 'var(--accent)' }}>AI</span>
          </span>
        </div>

        <h1 style={{ fontSize: '1.45rem', fontWeight: 700, letterSpacing: '-0.03em', marginBottom: '0.3rem' }}>
          Create your account
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginBottom: '2rem' }}>
          Join the community — start asking and answering
        </p>

        {apiError && <div className="alert-error" style={{ marginBottom: '1.25rem' }}>{apiError}</div>}

        <form onSubmit={handleSubmit} id="register-form" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div>
            <label htmlFor="register-name" className="field-label">Full name</label>
            <input
              id="register-name"
              name="name"
              type="text"
              autoComplete="name"
              required
              className="input-pill"
              placeholder="Jane Smith"
              value={form.name}
              onChange={handleChange}
              maxLength={120}
            />
            {errors.name && <p className="error-text">{errors.name}</p>}
          </div>

          <div>
            <label htmlFor="register-email" className="field-label">Email address</label>
            <input
              id="register-email"
              name="email"
              type="email"
              autoComplete="email"
              required
              className="input-pill"
              placeholder="you@example.com"
              value={form.email}
              onChange={handleChange}
              maxLength={160}
            />
            {errors.email && <p className="error-text">{errors.email}</p>}
          </div>

          <div>
            <label htmlFor="register-password" className="field-label">Password</label>
            <div style={{ position: 'relative' }}>
              <input
                id="register-password"
                name="password"
                type={showPw ? 'text' : 'password'}
                autoComplete="new-password"
                required
                className="input-pill"
                placeholder="Minimum 8 characters"
                style={{ paddingRight: '2.8rem' }}
                value={form.password}
                onChange={handleChange}
                maxLength={72}
              />
              <button
                type="button"
                id="register-toggle-pw"
                onClick={() => setShowPw(v => !v)}
                style={{
                  position: 'absolute', right: '0.9rem', top: '50%', transform: 'translateY(-50%)',
                  background: 'none', border: 'none', cursor: 'pointer',
                  color: 'var(--text-muted)', display: 'flex', alignItems: 'center',
                }}
              >
                {showPw ? <EyeOff size={15} strokeWidth={1.8} /> : <Eye size={15} strokeWidth={1.8} />}
              </button>
            </div>
            {errors.password && <p className="error-text">{errors.password}</p>}
          </div>

          <button
            id="register-submit-btn"
            type="submit"
            className="btn-primary"
            style={{ width: '100%', justifyContent: 'center', marginTop: '0.5rem' }}
            disabled={loading}
          >
            {loading
              ? <><span className="spinner" style={{ width: 16, height: 16 }} /> Creating account…</>
              : 'Create account'}
          </button>
        </form>

        <hr className="divider" />
        <p style={{ textAlign: 'center', fontSize: '0.84rem', color: 'var(--text-secondary)' }}>
          Already have an account?{' '}
          <Link to="/login" style={{ color: 'var(--accent)', fontWeight: 500, textDecoration: 'none' }}>
            Sign in
          </Link>
        </p>
      </div>
    </div>
  )
}
