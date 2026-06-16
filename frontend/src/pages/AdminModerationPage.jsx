import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { 
  checkContentModeration, checkQuestionModeration, checkAnswerModeration, 
  getQuestionModerationReport, getAnswerModerationReport, getAnalyticsOverview 
} from '../lib/adminApi'
import api from '../lib/axios'
import { ShieldAlert, CheckCircle, AlertTriangle, Search, Loader2, ExternalLink, MessageSquare, Activity, Hash, Edit3 } from 'lucide-react'

// --- Components ---

function ModerationResult({ result }) {
  if (!result) return null
  
  const isToxic = result.toxic || result.isToxic
  const isSpam = result.spam || result.isSpam
  const score = result.score || result.confidenceScore || 0
  const reason = result.reason || result.moderationReason
  const details = result.details || result.reportDetails

  let statusText = 'SAFE'
  let badgeClass = 'badge badge-open' // typically blue/green
  let Icon = CheckCircle
  let colorVar = 'var(--success)'

  if (isToxic) {
    statusText = 'TOXIC'
    badgeClass = 'badge badge-closed' // typically red
    Icon = ShieldAlert
    colorVar = 'var(--error)'
  } else if (isSpam) {
    statusText = 'SPAM'
    badgeClass = 'badge badge-closed'
    Icon = AlertTriangle
    colorVar = 'var(--error)'
  } else if (score > 0.5) {
    statusText = 'WARNING'
    badgeClass = 'badge badge-answered' // typically yellow
    Icon = AlertTriangle
    colorVar = 'var(--accent-hover)'
  }

  return (
    <div style={{ marginTop: '0.75rem', padding: '1rem', borderRadius: '8px', background: 'var(--bg-card)', border: `1px solid ${colorVar}` }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.75rem' }}>
        <Icon size={16} color={colorVar} />
        <span className={badgeClass}>{statusText}</span>
      </div>
      
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(120px, 1fr))', gap: '0.75rem', fontSize: '0.85rem', marginBottom: '0.5rem' }}>
        <div><strong style={{ color: 'var(--text-secondary)' }}>Toxic Score:</strong> <span style={{ color: isToxic ? 'var(--error)' : 'inherit' }}>{result.toxicScore?.toFixed(2) || score.toFixed(2)}</span></div>
        <div><strong style={{ color: 'var(--text-secondary)' }}>Spam Score:</strong> <span style={{ color: isSpam ? 'var(--error)' : 'inherit' }}>{result.spamScore?.toFixed(2) || score.toFixed(2)}</span></div>
      </div>

      {reason && (
        <div style={{ fontSize: '0.85rem', marginTop: '0.5rem' }}>
          <strong style={{ color: 'var(--text-secondary)' }}>Reason:</strong> {reason}
        </div>
      )}
      {details && (
        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.5rem', background: 'var(--bg-default)', padding: '0.5rem', borderRadius: '4px', overflowX: 'auto' }}>
          <strong style={{ color: 'var(--text-secondary)' }}>Explanation:</strong> {JSON.stringify(details)}
        </div>
      )}
    </div>
  )
}

function QuestionRow({ q, onReview }) {
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)

  async function handleAnalyze(isReport) {
    setLoading(true)
    setResult(null)
    try {
      const data = isReport ? await getQuestionModerationReport(q.id) : await checkQuestionModeration(q.id)
      setResult(data)
      if (!isReport) onReview() // Increment counter on active analysis
    } catch (err) {
      alert('Moderation check failed.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ padding: '1.25rem', borderBottom: '1px solid var(--border)' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '1rem', flexWrap: 'wrap' }}>
        <div style={{ flex: 1, minWidth: 200 }}>
          <div style={{ fontWeight: 600, fontSize: '0.95rem', color: 'var(--text-primary)', marginBottom: '0.35rem' }}>{q.title}</div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
            <span>By <strong>{q.author?.name}</strong></span>
            <span>·</span>
            <span>{new Date(q.createdAt).toLocaleDateString()}</span>
            {q.tags && q.tags.length > 0 && (
               <>
                 <span>·</span>
                 <div style={{ display: 'flex', gap: '0.25rem' }}>
                   {q.tags.slice(0, 3).map(t => <span key={t} className="tag" style={{ fontSize: '0.65rem', padding: '0.1rem 0.3rem' }}>{t}</span>)}
                 </div>
               </>
            )}
          </div>
        </div>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button className="btn-primary" style={{ padding: '0.35rem 0.75rem', fontSize: '0.8rem' }} onClick={() => handleAnalyze(false)} disabled={loading}>
            {loading ? <Loader2 size={12} className="spinner" /> : 'Analyze'}
          </button>
          <button className="btn-ghost" style={{ padding: '0.35rem 0.75rem', fontSize: '0.8rem' }} onClick={() => handleAnalyze(true)} disabled={loading}>
            Report
          </button>
          <Link to={`/questions/${q.id}`} className="btn-ghost" style={{ padding: '0.35rem 0.75rem', fontSize: '0.8rem' }} title="Open Question">
            <ExternalLink size={14} />
          </Link>
        </div>
      </div>
      <ModerationResult result={result} />
    </div>
  )
}

function AnswerRow({ a, onReview }) {
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)

  async function handleAnalyze(isReport) {
    setLoading(true)
    setResult(null)
    try {
      const data = isReport ? await getAnswerModerationReport(a.id) : await checkAnswerModeration(a.id)
      setResult(data)
      if (!isReport) onReview()
    } catch (err) {
      alert('Moderation check failed.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ padding: '1.25rem', borderBottom: '1px solid var(--border)' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '1rem', flexWrap: 'wrap' }}>
        <div style={{ flex: 1, minWidth: 200 }}>
          <div style={{ fontSize: '0.9rem', color: 'var(--text-primary)', marginBottom: '0.35rem', lineHeight: 1.5 }}>
            {a.body.length > 100 ? a.body.substring(0, 100) + '...' : a.body}
          </div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
            By <strong>{a.author?.name}</strong> · <span style={{ color: 'var(--text-muted)' }}>On: {a.questionTitle}</span>
          </div>
        </div>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button className="btn-primary" style={{ padding: '0.35rem 0.75rem', fontSize: '0.8rem' }} onClick={() => handleAnalyze(false)} disabled={loading}>
            {loading ? <Loader2 size={12} className="spinner" /> : 'Analyze'}
          </button>
          <button className="btn-ghost" style={{ padding: '0.35rem 0.75rem', fontSize: '0.8rem' }} onClick={() => handleAnalyze(true)} disabled={loading}>
            Report
          </button>
          <Link to={`/questions/${a.questionId}`} className="btn-ghost" style={{ padding: '0.35rem 0.75rem', fontSize: '0.8rem' }} title="Open Question">
            <ExternalLink size={14} />
          </Link>
        </div>
      </div>
      <ModerationResult result={result} />
    </div>
  )
}

function SummaryCard({ label, value, icon }) {
  return (
    <div className="glass" style={{ padding: '1.25rem', display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
      <div style={{ padding: '0.85rem', borderRadius: '12px', background: 'var(--accent-dim)', color: 'var(--accent-hover)' }}>
        {icon}
      </div>
      <div>
        <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 500, marginBottom: '0.1rem' }}>{label}</div>
        <div style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--text-primary)', letterSpacing: '-0.02em' }}>{value}</div>
      </div>
    </div>
  )
}

// --- Main Page ---

export default function AdminModerationPage() {
  const [loadingOverview, setLoadingOverview] = useState(true)
  const [overview, setOverview] = useState(null)
  
  const [questions, setQuestions] = useState([])
  const [answers, setAnswers] = useState([])
  
  const [questionsReviewed, setQuestionsReviewed] = useState(0)
  const [answersReviewed, setAnswersReviewed] = useState(0)

  const [textContent, setTextContent] = useState('')
  const [textLoading, setTextLoading] = useState(false)
  const [textResult, setTextResult] = useState(null)
  const [textError, setTextError] = useState('')

  useEffect(() => {
    let cancelled = false
    async function loadData() {
      try {
        const ov = await getAnalyticsOverview().catch(() => null)
        if (!cancelled && ov) setOverview(ov)

        // Load questions
        const qRes = await api.get('/questions')
        const qList = qRes.data || []
        if (!cancelled) setQuestions(qList)

        // Load answers for the top 10 questions to populate the dashboard
        const topQs = qList.slice(0, 10)
        const aPromises = topQs.map(q => api.get(`/questions/${q.id}/answers`).catch(() => ({ data: [] })))
        const aResults = await Promise.all(aPromises)
        
        let allAnswers = []
        aResults.forEach((res, idx) => {
          const ansList = res.data || []
          ansList.forEach(a => {
            a.questionTitle = topQs[idx].title
            a.questionId = topQs[idx].id
          })
          allAnswers = allAnswers.concat(ansList)
        })
        
        allAnswers.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
        if (!cancelled) setAnswers(allAnswers)

      } catch (err) {
        console.error('Failed to load moderation dashboard data', err)
      } finally {
        if (!cancelled) setLoadingOverview(false)
      }
    }
    loadData()
    return () => { cancelled = true }
  }, [])

  async function handleTextAnalysis(e) {
    e.preventDefault()
    if (!textContent.trim()) return
    setTextLoading(true)
    setTextError('')
    setTextResult(null)
    try {
      const data = await checkContentModeration(textContent)
      setTextResult(data)
    } catch (err) {
      setTextError('Failed to analyze content.')
    } finally {
      setTextLoading(false)
    }
  }

  return (
    <div className="page-container">
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '1.5rem' }}>
        <ShieldAlert size={20} strokeWidth={2.5} color="var(--error)" />
        <h1 style={{ fontSize: '1.25rem', fontWeight: 700, letterSpacing: '-0.02em' }}>Moderation Dashboard</h1>
      </div>

      {loadingOverview ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: '5rem 0' }}>
          <Loader2 size={32} className="spinner" color="var(--accent-hover)" />
        </div>
      ) : (
        <>

      {/* SECTION 5: MODERATION OVERVIEW */}
      <div className="fade-up" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem', marginBottom: '2.5rem' }}>
        <SummaryCard label="Total Questions" value={overview?.totalQuestions ?? questions.length} icon={<Hash size={20} />} />
        <SummaryCard label="Total Answers" value={overview?.totalAnswers ?? answers.length} icon={<MessageSquare size={20} />} />
        <SummaryCard label="Questions Reviewed" value={questionsReviewed} icon={<Activity size={20} />} />
        <SummaryCard label="Answers Reviewed" value={answersReviewed} icon={<Activity size={20} />} />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '2.5rem', marginBottom: '2.5rem' }}>
        
        {/* SECTION 1: QUESTION MODERATION DASHBOARD */}
        <div>
          <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '1rem', color: 'var(--text-primary)' }}>Recent Questions</h3>
          <div className="glass" style={{ padding: 0, overflow: 'hidden' }}>
            {questions.length === 0 ? (
              <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>No questions available</div>
            ) : (
              questions.slice(0, 10).map(q => (
                <QuestionRow key={q.id} q={q} onReview={() => setQuestionsReviewed(prev => prev + 1)} />
              ))
            )}
          </div>
        </div>

        {/* SECTION 2: ANSWER MODERATION DASHBOARD */}
        <div>
          <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '1rem', color: 'var(--text-primary)' }}>Recent Answers</h3>
          <div className="glass" style={{ padding: 0, overflow: 'hidden' }}>
            {answers.length === 0 ? (
              <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>No answers available</div>
            ) : (
              answers.slice(0, 10).map(a => (
                <AnswerRow key={a.id} a={a} onReview={() => setAnswersReviewed(prev => prev + 1)} />
              ))
            )}
          </div>
        </div>
        
      </div>

      {/* SECTION 4: MANUAL CONTENT ANALYSIS */}
      <div className="glass" style={{ padding: '1.5rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
          <Edit3 size={16} color="var(--accent-hover)" />
          <h3 style={{ fontSize: '1.1rem', fontWeight: 600, color: 'var(--text-primary)' }}>Manual Content Analysis</h3>
        </div>
        <form onSubmit={handleTextAnalysis} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <textarea 
            className="input-rect" 
            placeholder="Enter custom content to analyze..." 
            style={{ minHeight: '120px' }}
            value={textContent}
            onChange={e => setTextContent(e.target.value)}
          />
          <div>
            <button type="submit" className="btn-primary" disabled={textLoading || !textContent.trim()}>
              {textLoading ? <><Loader2 size={14} className="spinner" /> Analyzing...</> : <><Search size={14} /> Analyze Content</>}
            </button>
          </div>
        </form>
        {textError && <div className="error-text" style={{ marginTop: '0.75rem' }}>{textError}</div>}
        <ModerationResult result={textResult} />
      </div>
        </>
      )}

    </div>
  )
}
