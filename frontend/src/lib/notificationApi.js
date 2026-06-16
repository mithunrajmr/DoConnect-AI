import api from './axios'

export const NOTIFY_WS_URL = import.meta.env.VITE_NOTIFY_WS_URL

export async function fetchNotifications() {
  const { data } = await api.get('/notifications')
  return data
}

export async function fetchUnreadCount() {
  const { data } = await api.get('/notifications/unread-count')
  return data
}

export async function markAsRead(id) {
  const { data } = await api.put(`/notifications/${id}/read`)
  return data
}

export async function markAllAsRead() {
  const { data } = await api.put('/notifications/read-all')
  return data
}
