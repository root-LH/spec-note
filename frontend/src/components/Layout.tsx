import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

const navClass = ({ isActive }: { isActive: boolean }) =>
  `px-3 py-2 rounded-md text-sm font-medium ${
    isActive ? 'bg-indigo-50 text-indigo-700' : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
  }`

export default function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/exams')
  }

  return (
    <div className="min-h-screen flex flex-col">
      <header className="bg-white border-b border-gray-200">
        <div className="max-w-5xl mx-auto px-4 h-14 flex items-center gap-6">
          <Link to="/" className="text-lg font-bold text-indigo-600">
            SpecNote
          </Link>

          <nav className="flex items-center gap-1">
            {user && (
              <NavLink to="/" end className={navClass}>
                대시보드
              </NavLink>
            )}
            <NavLink to="/exams" className={navClass}>
              시험 찾기
            </NavLink>
            {user && (
              <>
                <NavLink to="/calendar" className={navClass}>
                  캘린더
                </NavLink>
                <NavLink to="/specs" className={navClass}>
                  내 스펙
                </NavLink>
                <NavLink to="/notifications" className={navClass}>
                  알림
                </NavLink>
              </>
            )}
          </nav>

          <div className="ml-auto flex items-center gap-3 text-sm">
            {user ? (
              <>
                <span className="text-gray-600">{user.nickname}님</span>
                <button onClick={handleLogout} className="text-gray-500 hover:text-gray-900">
                  로그아웃
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="text-gray-600 hover:text-gray-900">
                  로그인
                </Link>
                <Link to="/signup" className="px-3 py-1.5 rounded-md bg-indigo-600 text-white hover:bg-indigo-700">
                  회원가입
                </Link>
              </>
            )}
          </div>
        </div>
      </header>

      <main className="flex-1 max-w-5xl w-full mx-auto px-4 py-8">
        <Outlet />
      </main>

      <footer className="py-6 text-center text-xs text-gray-400">
        SpecNote · 자격증·어학 스펙 관리와 시험 접수 알림
      </footer>
    </div>
  )
}
