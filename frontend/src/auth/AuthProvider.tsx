import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { AuthContext } from './authContext'
import { tokenStorage } from './tokenStorage'
import { authApi } from '../api/endpoints'
import type { Member } from '../api/types'

/** 로그인 상태를 앱 전체에 공유 */
export default function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<Member | null>(null)
  const [loading, setLoading] = useState(() => tokenStorage.getAccessToken() !== null)

  // 새로고침해도 로그인이 유지되도록, 저장된 토큰이 있으면 내 정보를 다시 불러옴
  useEffect(() => {
    if (!tokenStorage.getAccessToken()) return
    authApi
      .me()
      .then(setUser)
      .catch(() => tokenStorage.clear())
      .finally(() => setLoading(false))
  }, [])

  // 토큰 재발급까지 실패하면 client.ts가 보내는 이벤트
  useEffect(() => {
    const handleLogout = () => setUser(null)
    window.addEventListener('auth:logout', handleLogout)
    return () => window.removeEventListener('auth:logout', handleLogout)
  }, [])

  const login = useCallback(async (email: string, password: string) => {
    const tokens = await authApi.login({ email, password })
    tokenStorage.save(tokens)
    setUser(await authApi.me())
  }, [])

  const logout = useCallback(() => {
    tokenStorage.clear()
    setUser(null)
  }, [])

  const value = useMemo(() => ({ user, loading, login, logout }), [user, loading, login, logout])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
