import { useEffect, useState } from 'react'
import { getAnalyticsOverview, getAnalyticsTags, getAnalyticsActivity, getAnalyticsSentiment } from '../lib/adminApi'
import { Activity, Users, MessageSquare, Tag, BarChart3, TrendingUp, CheckCircle } from 'lucide-react'

function StatCard({ title, value, icon, subtitle }) {
  return (
    <div className="glass" style={{ padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-secondary)' }}>{title}</span>
        {icon}
      </div>
      <div style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-primary)', letterSpacing: '-0.02em' }}>
        {value}
      </div>
      {subtitle && <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{subtitle}</div>}
    </div>
  )
}

export default function AnalyticsDashboardPage() {
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [overview, setOverview] = useState(null)
  const [tags, setTags] = useState([])
  const [activity, setActivity] = useState([])
  const [sentiment, setSentiment] = useState(null)

  useEffect(() => {
    let cancelled = false
    async function loadData() {
      try {
        const [overviewData, tagsData, activityData, sentimentData] = await Promise.all([
          getAnalyticsOverview(),
          getAnalyticsTags(),
          getAnalyticsActivity(),
          getAnalyticsSentiment().catch(() => null) // Sentiment might be missing
        ])
        if (!cancelled) {
          setOverview(overviewData)
          setTags(tagsData || [])
          setActivity(activityData || [])
          setSentiment(sentimentData)
        }
      } catch (err) {
        if (!cancelled) setError('Failed to load analytics data.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    loadData()
    return () => { cancelled = true }
  }, [])

  return (
    <div className="page-container">
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '1.5rem' }}>
        <BarChart3 size={20} strokeWidth={2.5} color="var(--accent-hover)" />
        <h1 style={{ fontSize: '1.25rem', fontWeight: 700, letterSpacing: '-0.02em' }}>Analytics Dashboard</h1>
      </div>

      {loading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: '5rem 0' }}>
          <span className="spinner" style={{ width: 28, height: 28 }} />
        </div>
      ) : error ? (
        <div className="alert-error">{error}</div>
      ) : (
        <>

      {/* Overview Stats */}
      {overview && (
        <div className="fade-up" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginBottom: '2rem' }}>
          <StatCard title="Total Users" value={overview.totalUsers ?? 0} icon={<Users size={16} color="var(--accent-hover)" />} subtitle="Registered accounts" />
          <StatCard title="Total Questions" value={overview.totalQuestions ?? 0} icon={<MessageSquare size={16} color="var(--accent-hover)" />} subtitle="Posted questions" />
          <StatCard title="Total Answers" value={overview.totalAnswers ?? 0} icon={<CheckCircle size={16} color="var(--accent-hover)" />} subtitle="Submitted answers" />
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 280px), 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
        {/* Most Used Tags */}
        <div className="glass" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
            <Tag size={16} color="var(--accent-hover)" />
            <h3 style={{ fontSize: '1rem', fontWeight: 600 }}>Most Used Tags</h3>
          </div>
          {tags.length === 0 ? <p className="error-text">No tag data available</p> : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              {tags.slice(0, 8).map((t, i) => (
                <div key={i} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', paddingBottom: '0.5rem', borderBottom: '1px solid var(--border)' }}>
                  <span className="tag">{t.tag || t.name || t.id || 'unknown'}</span>
                  <span style={{ fontSize: '0.85rem', fontWeight: 600 }}>{t.count || t.usageCount || 0}</span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Most Active Users */}
        <div className="glass" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
            <Activity size={16} color="var(--accent-hover)" />
            <h3 style={{ fontSize: '1rem', fontWeight: 600 }}>Active Users</h3>
          </div>
          {activity.length === 0 ? <p className="error-text">No user activity data available</p> : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              {activity.slice(0, 8).map((u, i) => (
                <div key={i} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', paddingBottom: '0.5rem', borderBottom: '1px solid var(--border)' }}>
                  <span style={{ fontSize: '0.85rem', fontWeight: 500 }}>{u.username || u.name || 'Unknown User'}</span>
                  <div style={{ display: 'flex', gap: '1rem', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                    <span>{u.questionCount || 0} Qs</span>
                    <span>{u.answerCount || 0} As</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Community Sentiment */}
      {sentiment && (
        <div className="glass" style={{ padding: '1.5rem', background: 'linear-gradient(to right, rgba(124,106,255,0.05), transparent)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
            <TrendingUp size={16} color="var(--accent-hover)" />
            <h3 style={{ fontSize: '1rem', fontWeight: 600 }}>Community Sentiment & Insights</h3>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div>
              <span style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: 'var(--text-muted)', fontWeight: 600 }}>Summary</span>
              <p style={{ fontSize: '0.9rem', lineHeight: 1.6, marginTop: '0.25rem' }}>{sentiment.summary || sentiment.sentimentSummary || 'No summary available.'}</p>
            </div>
            {sentiment.insights && (
              <div>
                <span style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: 'var(--text-muted)', fontWeight: 600 }}>AI Insights</span>
                <p style={{ fontSize: '0.9rem', lineHeight: 1.6, marginTop: '0.25rem' }}>{sentiment.insights || sentiment.aiInsights}</p>
              </div>
            )}
          </div>
        </div>
      )}
        </>
      )}
    </div>
  )
}
