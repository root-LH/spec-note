import { Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import ProtectedRoute from './components/ProtectedRoute'
import Loading from './components/Loading'
import ComingSoon from './components/ComingSoon'
import { useAuth } from './auth/useAuth'
import LoginPage from './pages/LoginPage'
import SignupPage from './pages/SignupPage'
import ExamListPage from './pages/ExamListPage'
import ExamDetailPage from './pages/ExamDetailPage'
import DashboardPage from './pages/DashboardPage'

/** 첫 화면: 로그인했으면 대시보드, 아니면 시험 찾기 */
function Home() {
  const { user, loading } = useAuth()
  if (loading) return <Loading />
  return user ? <DashboardPage /> : <Navigate to="/exams" replace />
}

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Home />} />
        <Route path="/exams" element={<ExamListPage />} />
        <Route path="/exams/:examId" element={<ExamDetailPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />

        {/* 로그인해야 들어갈 수 있는 화면 */}
        <Route element={<ProtectedRoute />}>
          <Route path="/specs" element={<ComingSoon title="내 스펙" />} />
          <Route path="/calendar" element={<ComingSoon title="시험 캘린더" />} />
          <Route path="/notifications" element={<ComingSoon title="알림" />} />
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}
