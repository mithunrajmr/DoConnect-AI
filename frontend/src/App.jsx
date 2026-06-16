import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom'
import { useEffect } from 'react'
import { AuthProvider } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import NavBar from './components/NavBar'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import FeedPage from './pages/FeedPage'
import QuestionDetailPage from './pages/QuestionDetailPage'
import AskPage from './pages/AskPage'
import ProfilePage from './pages/ProfilePage'
import ChatPage from './pages/ChatPage'
import AnalyticsDashboardPage from './pages/AnalyticsDashboardPage'
import AdminModerationPage from './pages/AdminModerationPage'
import AdminRoute from './components/AdminRoute'

import { NotificationProvider } from './context/NotificationContext'

function ScrollToTop() {
  const { pathname } = useLocation()
  useEffect(() => {
    window.scrollTo(0, 0)
  }, [pathname])
  return null
}

function AppLayout() {
  return (
    <>
      <NavBar />
      <Routes>
        <Route index element={<FeedPage />} />
        <Route path="questions/:id" element={<QuestionDetailPage />} />
        <Route path="ask" element={<AskPage />} />
        <Route path="profile" element={<ProfilePage />} />
        <Route path="chat" element={<ChatPage />} />
        
        {/* Admin Protected Routes */}
        <Route element={<AdminRoute />}>
          <Route path="analytics" element={<AnalyticsDashboardPage />} />
          <Route path="moderation" element={<AdminModerationPage />} />
        </Route>
      </Routes>
    </>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <ScrollToTop />
      <AuthProvider>
        <NotificationProvider>
          <Routes>
            <Route path="/login"    element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route element={<ProtectedRoute />}>
              <Route path="/*" element={<AppLayout />} />
            </Route>
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </NotificationProvider>
      </AuthProvider>
    </BrowserRouter>
  )
}
