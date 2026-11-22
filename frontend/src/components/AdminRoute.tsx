import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import Loading from './Loading'

/**
 * 관리자만 들어갈 수 있는 화면.
 * 화면을 숨기는 건 편의일 뿐이고, 실제 권한 검사는 서버(/api/admin/**)가 함.
 */
export default function AdminRoute() {
  const { user, loading } = useAuth()
  const location = useLocation()

  if (loading) return <Loading />
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  if (user.role !== 'ADMIN') return <Navigate to="/" replace />
  return <Outlet />
}
