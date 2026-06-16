import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../lib/axios'
import { Tag, Send, X, Sparkles, Loader2 } from 'lucide-react'
import { predictTags } from '../lib/aiApi'

export default function AskPage() {
  const navigate = useNavigate()

  const [form,    setForm]    = useState({ title: '', body: '', tagInput: '' })
  const [tags,    setTags]    = useState([])
  const [errors,  setErrors]  = useState({})
  const [apiErr,  setApiErr]  = useState('')
  const [loading, setLoading] = useState(false)
  const [predictingTags, setPredictingTags] = useState(false)
  const [predictErr, setPredictErr] = useState('')

  function handleChange(e) {
    setForm(f => ({ ...f, [e.target.name]: e.target.value }))
    setErrors(er => ({ ...er, [e.target.name]: '' }))
    setApiErr('')
  }

  function addTag(e) {
    e.preventDefault()
    const raw = form.tagInput.trim().toLowerCase()
    if (!raw) return
    if (tags.length >= 8) { setErrors(er => ({ ...er, tagInput: 'Maximum 8 tags allowed' })); return }
    if (raw.length > 80)  { setErrors(er => ({ ...er, tagInput: 'Tag must be 80 characters or fewer' })); return }
    if (tags.includes(raw)) { setForm(f => ({ ...f, tagInput: '' })); return }
    setTags(prev => [...prev, raw])
    setForm(f => ({ ...f, tagInput: '' }))
    setErrors(er => ({ ...er, tagInput: '' }))
  }

  function handleTagInputKey(e) {
    if (e.key === 'Enter' || e.key === ',') {
      e.preventDefault()
      addTag(e)
    }
  }

  function removeTag(t) {
    setTags(prev => prev.filter(x => x !== t))
  }

  function validate() {
    const errs = {}
    if (!form.title.trim())            errs.title = 'Title is required'
    if (form.title.length > 180)       errs.title = 'Title must be 180 characters or fewer'
    if (!form.body.trim())             errs.body  = 'Body is required'
    if (form.body.trim().length < 10)  errs.body  = 'Body must be at least 10 characters'
    return errs
  }

  async function handlePredictTags() {
    if (!form.title.trim() || !form.body.trim()) {
      setPredictErr('Please enter a title and description first.')
      return
    }
    setPredictingTags(true)
    setPredictErr('')
    try {
      const data = await predictTags(form.title, form.body)
      if (data && data.tags) {
        setTags(prev => {
          const combined = [...new Set([...prev, ...data.tags])]
          return combined.slice(0, 8) // max 8 tags
        })
      }
    } catch (err) {
      setPredictErr('Failed to predict tags.')
    } finally {
      setPredictingTags(false)
    }
  }

  async function handleSubmit(e) {
    e.preventDefault()
    const errs = validate()
    if (Object.keys(errs).length) { setErrors(errs); return }

    setLoading(true)
    setApiErr('')
    try {
      // POST /api/questions — body: { title, body, tags?: string[] }
      const { data } = await api.post('/questions', {
        title: form.title.trim(),
        body:  form.body.trim(),
        tags:  tags.length ? tags : undefined,
      })
      // Navigate to the newly created question detail page
      navigate(`/questions/${data.id}`)
    } catch (err) {
      const details = err.response?.data?.details
      const msg     = err.response?.data?.message
      setApiErr(details?.join('. ') ?? msg ?? 'Failed to post question. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page-container">
      <div style={{ marginBottom: '1.75rem' }}>
        <h1 style={{ fontSize: '1.25rem', fontWeight: 700, letterSpacing: '-0.02em' }}>Ask a question</h1>
        <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', marginTop: '0.15rem' }}>
          Be specific — a clear question gets a useful answer.
        </p>
      </div>

      {apiErr && <div className="alert-error" style={{ marginBottom: '1.25rem' }}>{apiErr}</div>}

      <form onSubmit={handleSubmit} id="ask-question-form" className="glass fade-up" style={{ padding: '1.75rem', display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>

        {/* Title */}
        <div>
          <label htmlFor="question-title" className="field-label">
            Question title
            <span style={{ color: 'var(--text-muted)', fontWeight: 400, marginLeft: '0.4rem' }}>max 180 chars</span>
          </label>
          <input
            id="question-title"
            name="title"
            type="text"
            className="input-pill"
            placeholder="e.g. How do I configure CORS in Spring Boot?"
            value={form.title}
            onChange={handleChange}
            maxLength={180}
            autoFocus
          />
          {errors.title && <p className="error-text">{errors.title}</p>}
          <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.3rem' }}>
            {form.title.length} / 180
          </p>
        </div>

        {/* Body */}
        <div>
          <label htmlFor="question-body" className="field-label">
            Details
            <span style={{ color: 'var(--text-muted)', fontWeight: 400, marginLeft: '0.4rem' }}>min 10 chars</span>
          </label>
          <textarea
            id="question-body"
            name="body"
            className="input-rect"
            placeholder="Describe your problem in detail. Include relevant code, error messages, or context."
            value={form.body}
            onChange={handleChange}
            style={{ minHeight: 180 }}
          />
          {errors.body && <p className="error-text">{errors.body}</p>}
        </div>

        {/* Tags */}
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.4rem' }}>
            <label className="field-label" style={{ marginBottom: 0 }}>
              Tags
              <span style={{ color: 'var(--text-muted)', fontWeight: 400, marginLeft: '0.4rem' }}>max 8 · press Enter or comma to add</span>
            </label>
            <button
              type="button"
              className="btn-ghost"
              style={{ fontSize: '0.75rem', padding: '0.25rem 0.5rem', color: 'var(--accent-hover)' }}
              onClick={handlePredictTags}
              disabled={predictingTags}
            >
              {predictingTags ? <Loader2 size={12} className="spinner" /> : <Sparkles size={12} />}
              ✨ Predict Tags
            </button>
          </div>
          {predictErr && <p className="error-text" style={{ marginBottom: '0.5rem' }}>{predictErr}</p>}

          {/* Tag chips */}
          {tags.length > 0 && (
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.4rem', marginBottom: '0.5rem' }}>
              {tags.map(t => (
                <span key={t} className="tag" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem' }}>
                  {t}
                  <button
                    type="button"
                    onClick={() => removeTag(t)}
                    id={`remove-tag-${t}`}
                    style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'inherit', display: 'flex', alignItems: 'center', padding: 0 }}
                  >
                    <X size={11} strokeWidth={2.5} />
                  </button>
                </span>
              ))}
            </div>
          )}

          <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
            <input
              id="tag-input"
              name="tagInput"
              type="text"
              className="input-pill"
              placeholder="e.g. spring-boot"
              value={form.tagInput}
              onChange={handleChange}
              onKeyDown={handleTagInputKey}
              maxLength={80}
              disabled={tags.length >= 8}
              style={{ flex: 1 }}
            />
            <button
              type="button"
              className="btn-ghost"
              onClick={addTag}
              id="add-tag-btn"
              disabled={!form.tagInput.trim() || tags.length >= 8}
            >
              <Tag size={14} strokeWidth={2} /> Add
            </button>
          </div>
          {errors.tagInput && <p className="error-text">{errors.tagInput}</p>}
        </div>

        {/* Submit */}
        <div style={{ display: 'flex', gap: '0.75rem', paddingTop: '0.5rem', borderTop: '1px solid var(--border)' }}>
          <button
            id="submit-question-btn"
            type="submit"
            className="btn-primary"
            disabled={loading}
          >
            {loading
              ? <><span className="spinner" style={{ width: 16, height: 16 }} /> Posting…</>
              : <><Send size={14} strokeWidth={2} /> Post question</>}
          </button>
          <button
            type="button"
            className="btn-ghost"
            onClick={() => navigate(-1)}
            id="cancel-ask-btn"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  )
}
