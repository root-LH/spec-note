import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import type { ErrorResponse, TokenResponse } from './types'
import { tokenStorage } from '../auth/tokenStorage'

/** 모든 API 호출에 쓰는 axios 인스턴스 */
export const api = axios.create({ baseURL: '/api' })

// 요청마다 Access Token을 헤더에 붙임
api.interceptors.request.use((config) => {
  const token = tokenStorage.getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/**
 * 토큰 재발급 요청을 하나로 묶기 위한 변수.
 * 화면에서 API 여러 개가 동시에 401을 받아도 재발급은 한 번만 호출됨.
 */
let refreshPromise: Promise<string> | null = null

async function refreshAccessToken(): Promise<string> {
  const refreshToken = tokenStorage.getRefreshToken()
  if (!refreshToken) {
    throw new Error('refresh token 없음')
  }
  // 인터셉터가 없는 기본 axios로 호출 (재발급 실패 → 다시 재발급 시도하는 무한 루프 방지)
  const { data } = await axios.post<TokenResponse>('/api/auth/refresh', { refreshToken })
  tokenStorage.save(data)
  return data.accessToken
}

type RetryableConfig = InternalAxiosRequestConfig & { _retry?: boolean }

// Access Token이 만료(A003)되면 자동으로 재발급 후 원래 요청을 한 번 더 보냄
api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ErrorResponse>) => {
    const original = error.config as RetryableConfig | undefined
    const isExpired = error.response?.status === 401 && error.response.data?.code === 'A003'

    if (original && isExpired && !original._retry) {
      original._retry = true
      try {
        refreshPromise ??= refreshAccessToken().finally(() => {
          refreshPromise = null
        })
        const newToken = await refreshPromise
        original.headers.Authorization = `Bearer ${newToken}`
        return api(original)
      } catch {
        // Refresh Token도 만료됨 → 로그아웃 처리 (AuthProvider가 이 이벤트를 받음)
        tokenStorage.clear()
        window.dispatchEvent(new Event('auth:logout'))
      }
    }
    return Promise.reject(error)
  },
)

/** 백엔드 에러 응답({code, message, errors})에서 사용자에게 보여줄 문구를 꺼냄 */
export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError<ErrorResponse>(error)) {
    if (!error.response) {
      return '서버에 연결할 수 없습니다. 백엔드가 실행 중인지 확인해주세요.'
    }
    const data = error.response.data
    if (data?.errors?.length) {
      return data.errors.map((e) => e.reason).join('\n')
    }
    if (data?.message) {
      return data.message
    }
  }
  return '알 수 없는 오류가 발생했습니다.'
}
