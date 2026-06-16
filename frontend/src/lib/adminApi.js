import api from './axios'

// --- AI Moderation ---

export async function checkContentModeration(text) {
  const { data } = await api.post('/ai/moderation/check', { text })
  return data
}

export async function checkQuestionModeration(questionId) {
  const { data } = await api.post(`/ai/moderation/question/${questionId}`)
  return data
}

export async function checkAnswerModeration(answerId) {
  const { data } = await api.post(`/ai/moderation/answer/${answerId}`)
  return data
}

export async function getQuestionModerationReport(questionId) {
  const { data } = await api.get(`/ai/moderation/question/${questionId}/report`)
  return data
}

export async function getAnswerModerationReport(answerId) {
  const { data } = await api.get(`/ai/moderation/answer/${answerId}/report`)
  return data
}

// --- Analytics ---

export async function getAnalyticsOverview() {
  const { data } = await api.get('/analytics/overview')
  return data
}

export async function getAnalyticsTags() {
  const { data } = await api.get('/analytics/tags')
  return data
}

export async function getAnalyticsActivity() {
  const { data } = await api.get('/analytics/activity')
  return data
}

export async function getAnalyticsSentiment() {
  const { data } = await api.get('/analytics/sentiment')
  return data
}
