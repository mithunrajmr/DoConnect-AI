import { useState } from 'react'
import { Sparkles, Loader2, Copy, ArrowDown, Wand2, FileText, CheckCircle2 } from 'lucide-react'
import { suggestAnswer, summarizeDiscussion, improveDraft } from '../lib/aiApi'

export default function AiAssistantPanel({ questionId, draft, setDraft }) {
  const [loading, setLoading] = useState(false)
  const [activeAction, setActiveAction] = useState(null)
  const [resultType, setResultType] = useState(null) // 'answer' | 'summary' | null
  const [resultText, setResultText] = useState('')
  const [error, setError] = useState('')
  const [copied, setCopied] = useState(false)

  const handleSuggestAnswer = async () => {
    setLoading(true)
    setError('')
    setActiveAction('suggest')
    setResultType(null)
    try {
      const data = await suggestAnswer(questionId)
      setResultText(data.suggestedAnswer)
      setResultType('answer')
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to generate answer. Please try again.')
    } finally {
      setLoading(false)
      setActiveAction(null)
    }
  }

  const handleSummarize = async () => {
    setLoading(true)
    setError('')
    setActiveAction('summarize')
    setResultType(null)
    try {
      const data = await summarizeDiscussion(questionId)
      setResultText(data.summary)
      setResultType('summary')
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to summarize discussion. Please try again.')
    } finally {
      setLoading(false)
      setActiveAction(null)
    }
  }

  const handleImproveDraft = async () => {
    if (!draft.trim()) {
      setError('Please write a draft in the text area below before improving it.')
      return
    }
    setLoading(true)
    setError('')
    setActiveAction('improve')
    setResultType(null)
    try {
      const data = await improveDraft(draft)
      setDraft(data.improvedText)
      // We don't set resultText here, we just replace the textarea directly.
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to improve draft. Please try again.')
    } finally {
      setLoading(false)
      setActiveAction(null)
    }
  }

  const handleCopy = () => {
    if (!resultText) return
    navigator.clipboard.writeText(resultText)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  const handleInsert = () => {
    if (!resultText) return
    const newDraft = draft.trim() ? draft + '\n\n' + resultText : resultText
    setDraft(newDraft)
  }

  return (
    <div className="glass fade-up" style={{ padding: '1.5rem', marginBottom: '1.5rem', border: '1px solid rgba(124, 106, 255, 0.3)', background: 'linear-gradient(to bottom right, rgba(124,106,255,0.05), rgba(0,0,0,0))' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
        <Sparkles size={18} strokeWidth={2} color="var(--accent-hover)" />
        <h3 style={{ margin: 0, fontSize: '1.1rem', fontWeight: 600, color: 'var(--text-primary)' }}>✨ AI Assistant</h3>
      </div>

      {error && (
        <div className="alert-error" style={{ marginBottom: '1rem', fontSize: '0.85rem' }}>
          {error}
        </div>
      )}

      <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
        <button
          className="btn-ghost"
          style={{ background: 'var(--surface-1)', border: '1px solid var(--border)', fontSize: '0.85rem', padding: '0.5rem 0.85rem' }}
          onClick={handleSuggestAnswer}
          disabled={loading}
        >
          {loading && activeAction === 'suggest' ? <Loader2 size={14} className="spinner" /> : <Sparkles size={14} />}
          Generate Answer
        </button>

        <button
          className="btn-ghost"
          style={{ background: 'var(--surface-1)', border: '1px solid var(--border)', fontSize: '0.85rem', padding: '0.5rem 0.85rem' }}
          onClick={handleSummarize}
          disabled={loading}
        >
          {loading && activeAction === 'summarize' ? <Loader2 size={14} className="spinner" /> : <FileText size={14} />}
          Summarize Discussion
        </button>

        <button
          className="btn-ghost"
          style={{ background: 'var(--surface-1)', border: '1px solid var(--border)', fontSize: '0.85rem', padding: '0.5rem 0.85rem' }}
          onClick={handleImproveDraft}
          disabled={loading || !draft.trim()}
          title={!draft.trim() ? 'Write a draft first' : ''}
        >
          {loading && activeAction === 'improve' ? <Loader2 size={14} className="spinner" /> : <Wand2 size={14} />}
          Improve Draft
        </button>
      </div>

      {resultType && resultText && (
        <div style={{ marginTop: '1.25rem', animation: 'fadeUp 0.3s ease both' }}>
          <div style={{ padding: '1rem', background: 'var(--surface-1)', borderRadius: 'var(--radius-md)', border: '1px solid rgba(124,106,255,0.2)' }}>
            <p style={{ fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--accent-hover)', fontWeight: 700, marginBottom: '0.75rem' }}>
              {resultType === 'answer' ? 'Suggested Answer' : 'Discussion Summary'}
            </p>
            <p style={{ fontSize: '0.9rem', color: 'var(--text-primary)', whiteSpace: 'pre-wrap', lineHeight: 1.6, marginBottom: '1rem' }}>
              {resultText}
            </p>

            <div style={{ display: 'flex', gap: '0.75rem' }}>
              <button className="btn-ghost" style={{ fontSize: '0.8rem', padding: '0.4rem 0.75rem' }} onClick={handleCopy}>
                {copied ? <CheckCircle2 size={14} color="var(--success)" /> : <Copy size={14} />}
                {copied ? 'Copied' : 'Copy'}
              </button>

              {resultType === 'answer' && (
                <button className="btn-primary" style={{ fontSize: '0.8rem', padding: '0.4rem 0.75rem' }} onClick={handleInsert}>
                  <ArrowDown size={14} />
                  Insert Into Answer
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
