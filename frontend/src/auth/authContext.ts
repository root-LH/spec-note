import { createContext } from 'react'
import type { Member } from '../api/types'

export interface AuthContextValue {
  user: Member | null
  /** 앱을 처음 열 때 저장된 토큰으로 로그인 상태를 확인하는 중인지 */
  loading: boolean
  login: (email: string, password: string) => Promise<void>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)
