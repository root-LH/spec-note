import { useCallback, useEffect, useState, type DependencyList } from 'react'
import { getErrorMessage } from '../api/client'

interface FetchState<T> {
  data: T | null
  error: string | null
  loading: boolean
}

/**
 * API 호출 결과(데이터, 에러, 로딩)를 관리하는 훅.
 * deps가 바뀌면 다시 호출하고, reload()로 수동 새로고침 가능.
 *
 * 예) const { data, loading, error, reload } = useFetch(() => examApi.get(id), [id])
 */
export function useFetch<T>(fetcher: () => Promise<T>, deps: DependencyList) {
  const [state, setState] = useState<FetchState<T>>({ data: null, error: null, loading: true })
  const [version, setVersion] = useState(0)

  useEffect(() => {
    // 응답이 늦게 온 이전 요청이 최신 결과를 덮어쓰지 않도록 함
    let ignore = false
    setState((prev) => ({ ...prev, loading: true, error: null }))

    fetcher()
      .then((data) => {
        if (!ignore) setState({ data, error: null, loading: false })
      })
      .catch((error) => {
        if (!ignore) setState({ data: null, error: getErrorMessage(error), loading: false })
      })

    return () => {
      ignore = true
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...deps, version])

  const reload = useCallback(() => setVersion((v) => v + 1), [])

  return { ...state, reload }
}
