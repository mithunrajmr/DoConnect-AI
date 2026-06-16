import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../lib/axios'
import { MessageSquare, Eye, Tag, ChevronRight, PlusCircle, Clock, Search, Loader2 } from 'lucide-react'
import { searchQuestions } from '../lib/aiApi'

function formatRelative(isoString) {
  if (!isoString) return ''
  const diff = Date.now() - new Date(isoString).getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return 'just now'
  if (mins < 60) return `${mins}m ago`
  const hrs = Math.floor(mins / 60)
  if (hrs < 24) return `${hrs}h ago`
  const days = Math.floor(hrs / 24)
  if (days < 30) return `${days}d ago`
  return new Date(isoString).toLocaleDateString()
}

function statusBadgeClass(status) {
  if (status === 'ANSWERED' || status === 'SOLVED') return 'badge badge-answered'
  if (status === 'CLOSED') return 'badge badge-closed'
  return 'badge badge-open'
}

function QuestionCard({ q }) {
  return (
    <Link to={`/questions/${q.id}`} className="q-card" id={`question-card-${q.id}`}>
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '1rem' }}>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.4rem', flexWrap: 'wrap' }}>
            <span className={statusBadgeClass(q.status)}>{q.status}</span>
            {q.tags?.map(tag => (
              <span key={tag} className="tag">{tag}</span>
            ))}
          </div>

          <h2 style={{ fontSize: '0.95rem', fontWeight: 600, lineHeight: 1.4, marginBottom: '0.35rem', color: 'var(--text-primary)' }}>
            {q.title}
          </h2>

          <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', overflow: 'hidden', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical' }}>
            {q.body}
          </p>

          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginTop: '0.6rem', flexWrap: 'wrap' }}>
            <span className="stat-chip">
              <MessageSquare size={13} strokeWidth={1.8} />
              {q.answerCount} {q.answerCount === 1 ? 'answer' : 'answers'}
            </span>
            <span className="stat-chip">
              <Eye size={13} strokeWidth={1.8} />
              {q.viewCount} views
            </span>
            <span className="stat-chip">
              <Clock size={13} strokeWidth={1.8} />
              {formatRelative(q.createdAt)}
            </span>
            <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
              by <strong style={{ color: 'var(--text-secondary)', fontWeight: 500 }}>{q.author?.name}</strong>
            </span>
          </div>
        </div>

        <ChevronRight size={16} strokeWidth={1.8} style={{ color: 'var(--text-muted)', flexShrink: 0, marginTop: '0.15rem' }} />
      </div>
    </Link>
  )
}

export default function FeedPage() {
  const [questions, setQuestions] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [searchQuery, setSearchQuery] = useState('')
  const [isSearching, setIsSearching] = useState(false)
  const [searchResults, setSearchResults] = useState(null)

  useEffect(() => {
    let cancelled = false
    async function load() {
      try {
        // GET /api/questions — returns QuestionResponse[]
        const { data } = await api.get('/questions')
        if (!cancelled) setQuestions(data)
      } catch {
        if (!cancelled) setError('Failed to load questions. Please try again.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    load()
    return () => { cancelled = true }
  }, [])

  async function handleSearch(e) {
    e.preventDefault()
    if (!searchQuery.trim()) {
      setSearchResults(null)
      return
    }
    setIsSearching(true)
    setError('')
    try {
      const data = await searchQuestions(searchQuery)
      setSearchResults(data)
    } catch (err) {
      setError('Search failed. Please try again.')
    } finally {
      setIsSearching(false)
    }
  }

  function handleClearSearch() {
    setSearchQuery('')
    setSearchResults(null)
  }

  const displayedQuestions = searchResults !== null ? searchResults : questions

  return (
    <div className="page-container">
      {/* Header row */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
        <div>
          <h1 style={{ fontSize: '1.25rem', fontWeight: 700, letterSpacing: '-0.02em' }}>Questions</h1>
          <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', marginTop: '0.15rem' }}>
            {loading ? 'Loading…' : `${questions.length} question${questions.length !== 1 ? 's' : ''}`}
          </p>
        </div>
        <Link to="/ask" className="btn-primary" id="ask-question-btn">
          <PlusCircle size={15} strokeWidth={2} />
          Ask a question
        </Link>
      </div>

      {/* AI Search Bar */}
      <form onSubmit={handleSearch} style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.5rem', alignItems: 'center' }}>
        <div style={{ position: 'relative', flex: 1 }}>
          <Search size={14} style={{ position: 'absolute', left: '1rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
          <input
            type="text"
            className="input-pill"
            style={{ paddingLeft: '2.5rem' }}
            placeholder="✨ Ask AI to find related questions..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
        <button type="submit" className="btn-primary" disabled={isSearching || !searchQuery.trim()}>
          {isSearching ? <Loader2 size={14} className="spinner" /> : 'Search'}
        </button>
        {searchResults !== null && (
          <button type="button" className="btn-ghost" onClick={handleClearSearch}>
            Clear
          </button>
        )}
      </form>

      {/* States */}
      {error && <div className="alert-error" style={{ marginBottom: '1rem' }}>{error}</div>}

      {loading && (
        <div style={{ display: 'flex', justifyContent: 'center', padding: '4rem 0' }}>
          <span className="spinner" style={{ width: 28, height: 28 }} />
        </div>
      )}

      {!loading && !error && displayedQuestions.length === 0 && (
        <div className="empty-state glass">
          <Tag size={32} strokeWidth={1.4} style={{ marginBottom: '1rem', color: 'var(--text-muted)' }} />
          <h3>{searchResults !== null ? 'No results found' : 'No questions yet'}</h3>
          <p style={{ fontSize: '0.84rem' }}>{searchResults !== null ? 'Try a different search query.' : 'Be the first — ask something interesting.'}</p>
          {searchResults === null && (
            <Link to="/ask" className="btn-primary" style={{ marginTop: '1.25rem', display: 'inline-flex' }}>
              <PlusCircle size={15} strokeWidth={2} /> Ask now
            </Link>
          )}
        </div>
      )}

      {!loading && displayedQuestions.length > 0 && (
        <div className="glass fade-up" style={{ overflow: 'hidden', padding: 0 }}>
          {displayedQuestions.map(q => <QuestionCard key={q.id} q={q} />)}
        </div>
      )}
    </div>
  )
}
