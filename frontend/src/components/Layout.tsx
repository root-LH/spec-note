import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import { Button } from './ui'

const navClass = ({ isActive }: { isActive: boolean }) =>
  `shrink-0 rounded-full px-3.5 py-2 text-[15px] font-semibold transition ${
    isActive ? 'bg-ink-50 text-ink-900' : 'text-ink-400 hover:text-ink-900'
  }`

export default function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/exams')
  }

  return (
    <div className="flex min-h-screen flex-col">
      {/* 스크롤해도 위에 붙어 있는 반투명 헤더 */}
      <header className="sticky top-0 z-20 border-b border-ink-100 bg-white/85 backdrop-blur">
        <div className="mx-auto flex h-16 max-w-5xl items-center gap-4 px-5">
          <Link to="/" className="shrink-0 text-xl font-extrabold tracking-tight text-brand-500">
            SpecNote
          </Link>

          <nav className="flex min-w-0 flex-1 gap-1 overflow-x-auto">
            {user && (
              <NavLink to="/" end className={navClass}>
                홈
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

          <div className="flex shrink-0 items-center gap-2">
            {user ? (
              <>
                <span className="hidden text-sm text-ink-500 sm:inline">{user.nickname}님</span>
                <Button variant="ghost" size="sm" onClick={handleLogout}>
                  로그아웃
                </Button>
              </>
            ) : (
              <>
                <Link to="/login" className="px-2 text-sm font-semibold text-ink-600 hover:text-ink-900">
                  로그인
                </Link>
                <Link
                  to="/signup"
                  className="inline-flex h-9 items-center rounded-xl bg-brand-500 px-3.5 text-sm font-semibold text-white hover:bg-brand-600"
                >
                  회원가입
                </Link>
              </>
            )}
          </div>
        </div>
      </header>

      <main className="mx-auto w-full max-w-5xl flex-1 px-5 py-8 sm:py-10">
        <Outlet />
      </main>

      <footer className="py-8 text-center text-xs text-ink-400">SpecNote · 자격증·어학 스펙 관리와 시험 접수 알림</footer>
    </div>
  )
}
