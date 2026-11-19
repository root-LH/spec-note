import { useState, type FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { examApi } from '../api/endpoints'
import type { ExamCategory } from '../api/types'
import { useFetch } from '../hooks/useFetch'
import { CATEGORY_LABEL } from '../utils/format'
import Loading from '../components/Loading'
import ErrorMessage from '../components/ErrorMessage'
import { Badge } from '../components/Badges'

const CATEGORIES: (ExamCategory | null)[] = [null, 'NATIONAL', 'LANGUAGE', 'PRIVATE']
const PAGE_SIZE = 12

export default function ExamListPage() {
  // 검색 조건을 URL에 저장 → 새로고침·뒤로가기해도 유지되고 링크로 공유 가능
  const [searchParams, setSearchParams] = useSearchParams()
  const category = (searchParams.get('category') as ExamCategory | null) ?? null
  const keyword = searchParams.get('keyword') ?? ''
  const page = Number(searchParams.get('page') ?? 0)

  const [input, setInput] = useState(keyword)

  const { data, loading, error, reload } = useFetch(
    () => examApi.search({ category: category ?? undefined, keyword: keyword || undefined, page, size: PAGE_SIZE }),
    [category, keyword, page],
  )

  const updateParams = (next: { category?: ExamCategory | null; keyword?: string; page?: number }) => {
    const params = new URLSearchParams()
    const c = next.category !== undefined ? next.category : category
    const k = next.keyword !== undefined ? next.keyword : keyword
    const p = next.page ?? 0
    if (c) params.set('category', c)
    if (k) params.set('keyword', k)
    if (p > 0) params.set('page', String(p))
    setSearchParams(params)
  }

  const handleSearch = (event: FormEvent) => {
    event.preventDefault()
    updateParams({ keyword: input.trim() })
  }

  return (
    <div>
      <h1 className="text-2xl font-bold">시험 찾기</h1>
      <p className="mt-1 text-sm text-gray-500">관심 있는 시험을 구독하면 접수 일정을 알려드려요.</p>

      <div className="mt-6 flex flex-col sm:flex-row sm:items-center gap-3">
        <div className="flex gap-1">
          {CATEGORIES.map((c) => (
            <button
              key={c ?? 'ALL'}
              onClick={() => updateParams({ category: c })}
              className={`px-3 py-1.5 rounded-full text-sm ${
                category === c ? 'bg-indigo-600 text-white' : 'bg-white border border-gray-300 text-gray-600 hover:bg-gray-50'
              }`}
            >
              {c ? CATEGORY_LABEL[c] : '전체'}
            </button>
          ))}
        </div>

        <form onSubmit={handleSearch} className="sm:ml-auto flex gap-2">
          <input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="시험 이름 검색"
            className="w-full sm:w-56 rounded-md border border-gray-300 px-3 py-1.5 text-sm focus:border-indigo-500 focus:outline-none"
          />
          <button className="px-3 py-1.5 rounded-md bg-gray-800 text-white text-sm hover:bg-gray-900">검색</button>
        </form>
      </div>

      <div className="mt-6">
        {loading && <Loading />}
        {error && <ErrorMessage message={error} onRetry={reload} />}

        {data && data.content.length === 0 && (
          <p className="py-12 text-center text-sm text-gray-500">조건에 맞는 시험이 없어요.</p>
        )}

        {data && data.content.length > 0 && (
          <>
            <ul className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
              {data.content.map((exam) => (
                <li key={exam.examId}>
                  <Link
                    to={`/exams/${exam.examId}`}
                    className="block h-full bg-white rounded-xl border border-gray-200 p-4 hover:border-indigo-400 hover:shadow-sm transition"
                  >
                    <Badge>{exam.categoryName}</Badge>
                    <h2 className="mt-2 font-semibold">{exam.name}</h2>
                    <p className="mt-1 text-sm text-gray-500">{exam.organizer ?? '시행기관 정보 없음'}</p>
                    <p className="mt-2 text-xs text-gray-400">
                      유효기간 {exam.validityMonths ? `${exam.validityMonths}개월` : '영구'}
                    </p>
                  </Link>
                </li>
              ))}
            </ul>

            {data.totalPages > 1 && (
              <div className="mt-6 flex items-center justify-center gap-3 text-sm">
                <button
                  disabled={page === 0}
                  onClick={() => updateParams({ page: page - 1 })}
                  className="px-3 py-1.5 rounded-md border border-gray-300 bg-white disabled:opacity-40"
                >
                  이전
                </button>
                <span className="text-gray-500">
                  {page + 1} / {data.totalPages}
                </span>
                <button
                  disabled={!data.hasNext}
                  onClick={() => updateParams({ page: page + 1 })}
                  className="px-3 py-1.5 rounded-md border border-gray-300 bg-white disabled:opacity-40"
                >
                  다음
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  )
}
