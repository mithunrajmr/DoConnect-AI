import api from './axios'

export async function suggestAnswer(questionId) {
  const { data } = await api.post(`/ai/questions/${questionId}/suggest-answer`)
  return data
}

export async function summarizeDiscussion(questionId) {
  const { data } = await api.post(`/ai/questions/${questionId}/summarize`)
  return data
}

export async function improveDraft(text) {
  const { data } = await api.post('/ai/improve-draft', { text })
  return data
}

// --- Recommendations ---

export async function predictTags(title, description) {
  const { data } = await api.post('/ai/recommendations/predict-tags', { title, description })
  return data
}

export async function getSimilarQuestions(questionId) {
  const { data } = await api.get(`/ai/recommendations/similar/${questionId}`)
  return data
}

export async function searchQuestions(query) {
  const { data } = await api.post('/ai/recommendations/search', { query })
  return data
}
