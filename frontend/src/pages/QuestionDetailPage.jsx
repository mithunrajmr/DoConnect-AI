import { useEffect, useState } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import api from '../lib/axios'
import {
  ArrowLeft, MessageSquare, Eye, CheckCircle2,
  Cpu, Clock, Send, Trash2, Tag, Link as LinkIcon
} from 'lucide-react'
import AiAssistantPanel from '../components/AiAssistantPanel'
import { getSimilarQuestions } from '../lib/aiApi'

function formatRelative(iso) {
  if (!iso) return ''
  const diff = Date.now() - new Date(iso).getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return 'just now'
  if (mins < 60) return `${mins}m ago`
  const hrs = Math.floor(mins / 60)
  if (hrs < 24) return `${hrs}h ago`
  return `${Math.floor(hrs / 24)}d ago`
}

function AnswerCard({ answer, currentUser, questionOwnerId, onDelete, onAccept }) {
  const isAdmin = currentUser?.role === 'ADMIN'
  const isOwner = currentUser?.id === answer.author?.id || isAdmin
  const canAccept = !answer.accepted && (currentUser?.id === questionOwnerId || isAdmin)

  return (
    <div className={`answer-card fade-up ${answer.accepted ? 'accepted' : ''}`} id={`answer-${answer.id}`}>
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '1rem' }}>
        <div style={{ flex: 1 }}>
          {answer.accepted && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', marginBottom: '0.6rem', color: 'var(--success)', fontSize: '0.78rem', fontWeight: 600 }}>
              <CheckCircle2 size={14} strokeWidth={2} /> Accepted Answer
            </div>
          )}
          {!answer.accepted && canAccept && (
            <button
              type="button"
              className="btn-ghost"
              style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem', marginBottom: '0.6rem', color: 'var(--text-secondary)', fontSize: '0.78rem', fontWeight: 600, border: '1px solid var(--border)', borderRadius: '999px', padding: '0.3rem 0.7rem' }}
              onClick={() => onAccept(answer.id)}
            >
              <CheckCircle2 size={14} strokeWidth={2} /> Accept Answer
            </button>
          )}
          {answer.aiGenerated && (
            <div style={{ marginBottom: '0.6rem' }}>
              <span className="ai-badge">
                <Cpu size={11} strokeWidth={2} /> AI generated
                {answer.aiConfidence != null && ` · ${Math.round(Number(answer.aiConfidence) * 100)}% confidence`}
              </span>
            </div>
          )}

          <p style={{ fontSize: '0.9rem', lineHeight: 1.7, color: 'var(--text-primary)', whiteSpace: 'pre-wrap' }}>
            {answer.body}
          </p>
        </div>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '0.9rem', paddingTop: '0.75rem', borderTop: '1px solid var(--border)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
            <strong style={{ color: 'var(--text-secondary)' }}>{answer.author?.name}</strong>
          </span>
          <span className="stat-chip"><Clock size={12} strokeWidth={1.8} />{formatRelative(answer.createdAt)}</span>
        </div>
        {isOwner && (
          <button
            className="btn-danger"
            onClick={() => onDelete(answer.id)}
            id={`delete-answer-${answer.id}`}
          >
            <Trash2 size={12} strokeWidth={2} /> Delete
          </button>
        )}
      </div>
    </div>
  )
}

export default function QuestionDetailPage() {
  const { id } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()

  const [question, setQuestion] = useState(null)
  const [answers, setAnswers] = useState([])
  const [similarQuestions, setSimilarQuestions] = useState([])
  const [body, setBody] = useState('')
  const [qLoading, setQLoading] = useState(true)
  const [aLoading, setALoading] = useState(true)
  const [posting, setPosting] = useState(false)
  const [error, setError] = useState('')
  const [postError, setPostError] = useState('')

  useEffect(() => {
    let cancelled = false

    async function loadQuestion() {
      try {
        const viewKey = `viewed_q_${id}`
        const alreadyViewed = sessionStorage.getItem(viewKey)
        const trackView = !alreadyViewed

        // GET /api/questions/:id?trackView=true/false
        const { data } = await api.get(`/questions/${id}`, {
          params: { trackView }
        })

        // Record that we have viewed it this session so we don't increment on refresh
        if (trackView) {
          sessionStorage.setItem(viewKey, 'true')
        }

        if (!cancelled) setQuestion(data)
      } catch {
        if (!cancelled) setError('Question not found or could not be loaded.')
      } finally {
        if (!cancelled) setQLoading(false)
      }
    }

    async function loadAnswers() {
      try {
        // GET /api/questions/:questionId/answers
        const { data } = await api.get(`/questions/${id}/answers`)
        if (!cancelled) setAnswers(data)
      } catch {
        // Non-fatal — question still shows
      } finally {
        if (!cancelled) setALoading(false)
      }
    }

    async function loadSimilar() {
      try {
        const data = await getSimilarQuestions(id)
        if (!cancelled) setSimilarQuestions(data || [])
      } catch {
        // Non-fatal
      }
    }

    loadQuestion()
    loadAnswers()
    loadSimilar()

    // Lightweight polling: refresh answers every 10 seconds
    const intervalId = setInterval(() => {
      if (!cancelled) loadAnswers()
    }, 10000)

    return () => {
      cancelled = true
      clearInterval(intervalId)
    }
  }, [id])

  async function submitAnswer(e) {
    e.preventDefault()
    if (!body.trim() || body.trim().length < 5) {
      setPostError('Answer must be at least 5 characters.')
      return
    }
    setPosting(true)
    setPostError('')
    try {
      // POST /api/questions/:questionId/answers — body: { body }
      const { data } = await api.post(`/questions/${id}/answers`, { body: body.trim() })
      setAnswers(prev => [...prev, data])
      setBody('')
    } catch (err) {
      const msg = err.response?.data?.message ?? 'Failed to post answer.'
      setPostError(msg)
    } finally {
      setPosting(false)
    }
  }

  async function deleteAnswer(answerId) {
    if (!window.confirm('Delete this answer?')) return
    try {
      // DELETE /api/answers/:id
      await api.delete(`/answers/${answerId}`)
      setAnswers(prev => prev.filter(a => a.id !== answerId))
    } catch {
      alert('Could not delete the answer.')
    }
  }

  async function acceptAnswer(answerId) {
    try {
      await api.post(`/questions/${id}/accept/${answerId}`)
      setAnswers(prev => prev.map(a => ({
        ...a,
        accepted: a.id === answerId
      })))
      setQuestion(prev => prev ? { ...prev, status: 'SOLVED', acceptedAnswerId: answerId } : prev)
    } catch {
      alert('Could not accept answer. Please try again.')
    }
  }

  async function deleteQuestion() {
    if (!window.confirm('Delete this question and all its answers?')) return
    try {
      // DELETE /api/questions/:id
      await api.delete(`/questions/${id}`)
      navigate('/')
    } catch {
      alert('Could not delete the question.')
    }
  }

  const isAdmin = user?.role === 'ADMIN'
  const isAuthor = user?.id === question?.author?.id || isAdmin

  return (
    <div className="page-container">
      {/* Back */}
      <Link to="/" className="btn-ghost" style={{ marginBottom: '1.5rem', display: 'inline-flex' }} id="back-to-feed">
        <ArrowLeft size={14} strokeWidth={1.8} /> Feed
      </Link>

      {qLoading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: '5rem 0' }}>
          <span className="spinner" style={{ width: 28, height: 28 }} />
        </div>
      ) : error || !question ? (
        <div className="alert-error">{error || 'Question not found.'}</div>
      ) : (
        <>
          {/* Question */}
          <div className="glass fade-up" style={{ padding: '1.75rem', marginBottom: '1.5rem' }}>
        <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '1rem' }}>
          <div style={{ flex: 1 }}>
            {/* Tags & status */}
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.4rem', marginBottom: '0.85rem' }}>
              <span className={`badge ${question.status === 'OPEN' ? 'badge-open' : question.status === 'SOLVED' ? 'badge-answered' : question.status === 'ANSWERED' ? 'badge-answered' : 'badge-closed'}`}>
                {question.status}
              </span>
              {question.tags?.map(t => <span key={t} className="tag">{t}</span>)}
            </div>

            <h1 style={{ fontSize: '1.3rem', fontWeight: 700, letterSpacing: '-0.02em', lineHeight: 1.35, marginBottom: '1rem' }}>
              {question.title}
            </h1>

            {question.aiSummary && (
              <div style={{ padding: '0.75rem 1rem', borderRadius: 10, background: 'var(--accent-dim)', border: '1px solid rgba(124,106,255,0.2)', marginBottom: '1rem' }}>
                <p style={{ fontSize: '0.78rem', fontWeight: 600, color: 'var(--accent-hover)', marginBottom: '0.25rem', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                  <Cpu size={13} strokeWidth={2} /> AI Summary
                </p>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', lineHeight: 1.6 }}>{question.aiSummary}</p>
              </div>
            )}

            <p style={{ fontSize: '0.9rem', lineHeight: 1.75, color: 'var(--text-primary)', whiteSpace: 'pre-wrap' }}>
              {question.body}
            </p>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '1.25rem', paddingTop: '1rem', borderTop: '1px solid var(--border)', flexWrap: 'wrap', gap: '0.75rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
            <span className="stat-chip"><MessageSquare size={13} strokeWidth={1.8} />{answers.length} answers</span>
            <span className="stat-chip"><Eye size={13} strokeWidth={1.8} />{question.viewCount} views</span>
            <span className="stat-chip"><Clock size={13} strokeWidth={1.8} />{formatRelative(question.createdAt)}</span>
            <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
              by <strong style={{ color: 'var(--text-secondary)' }}>{question.author?.name}</strong>
            </span>
          </div>
          {isAuthor && (
            <button className="btn-danger" onClick={deleteQuestion} id="delete-question-btn">
              <Trash2 size={12} strokeWidth={2} /> Delete question
            </button>
          )}
        </div>
      </div>

      {/* Similar Questions */}
      {similarQuestions.length > 0 && (
        <div style={{ marginBottom: '1.5rem' }}>
          <p className="section-head" style={{ marginBottom: '0.75rem', fontSize: '0.85rem' }}>
            ✨ Similar Questions
          </p>
          <div className="glass fade-up" style={{ padding: '1rem', display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
            {similarQuestions.map(sq => (
              <Link
                key={sq.id}
                to={`/questions/${sq.id}`}
                style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-primary)', textDecoration: 'none', fontSize: '0.85rem' }}
                className="hover-opacity"
              >
                <LinkIcon size={12} style={{ color: 'var(--accent-hover)' }} />
                {sq.title}
              </Link>
            ))}
          </div>
        </div>
      )}

      {/* Answers */}
      <div style={{ marginBottom: '1.5rem' }}>
        <p className="section-head">
          {aLoading ? 'Loading answers…' : `${answers.length} Answer${answers.length !== 1 ? 's' : ''}`}
        </p>

        {aLoading && (
          <div style={{ display: 'flex', justifyContent: 'center', padding: '2rem 0' }}>
            <span className="spinner" />
          </div>
        )}

        {!aLoading && answers.length === 0 && (
          <div className="empty-state glass" style={{ padding: '2.5rem' }}>
            <Tag size={24} strokeWidth={1.4} style={{ marginBottom: '0.75rem', color: 'var(--text-muted)' }} />
            <h3>No answers yet</h3>
            <p style={{ fontSize: '0.82rem' }}>Be the first to answer this question below.</p>
          </div>
        )}

        <div className="fade-up" style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          {[...answers]
            .sort((a, b) => {
              if (a.accepted && !b.accepted) return -1
              if (!a.accepted && b.accepted) return 1
              return 0
            })
            .map(a => (
              <AnswerCard
                key={a.id}
                answer={a}
                currentUser={user}
                questionOwnerId={question.author?.id}
                onDelete={deleteAnswer}
                onAccept={acceptAnswer}
              />
          ))}
        </div>
      </div>

      {/* AI Assistant */}
      <AiAssistantPanel questionId={id} draft={body} setDraft={setBody} />

      {/* Post answer form */}
      <div className="glass fade-up" style={{ padding: '1.5rem' }}>
        <p className="section-head" style={{ marginBottom: '1rem' }}>Your answer</p>
        {postError && <div className="alert-error" style={{ marginBottom: '1rem' }}>{postError}</div>}
        <form onSubmit={submitAnswer} id="post-answer-form">
          <textarea
            id="answer-body"
            className="input-rect"
            placeholder="Write your answer… (minimum 5 characters)"
            value={body}
            onChange={e => { setBody(e.target.value); setPostError('') }}
            style={{ minHeight: 140, marginBottom: '1rem' }}
          />
          <button
            id="submit-answer-btn"
            type="submit"
            className="btn-primary"
            disabled={posting || body.trim().length < 5}
          >
            {posting
              ? <><span className="spinner" style={{ width: 16, height: 16 }} /> Posting…</>
              : <><Send size={14} strokeWidth={2} /> Post answer</>}
          </button>
        </form>
      </div>
        </>
      )}
    </div>
  )
}
