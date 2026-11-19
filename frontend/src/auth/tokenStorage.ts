import type { TokenResponse } from '../api/types'

const ACCESS_KEY = 'specnote.accessToken'
const REFRESH_KEY = 'specnote.refreshToken'

/**
 * 토큰 저장소 (localStorage).
 * 간단하지만 XSS 공격에 노출될 수 있음 → 실서비스라면 Refresh Token은 HttpOnly 쿠키로 옮기는 것을 고려.
 */
export const tokenStorage = {
  getAccessToken: () => localStorage.getItem(ACCESS_KEY),
  getRefreshToken: () => localStorage.getItem(REFRESH_KEY),
  save: (tokens: TokenResponse) => {
    localStorage.setItem(ACCESS_KEY, tokens.accessToken)
    localStorage.setItem(REFRESH_KEY, tokens.refreshToken)
  },
  clear: () => {
    localStorage.removeItem(ACCESS_KEY)
    localStorage.removeItem(REFRESH_KEY)
  },
}
