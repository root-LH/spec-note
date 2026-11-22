import { useState, type FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { examApi } from '../api/endpoints'
import type { ExamCategory } from '../api/types'
import { useFetch } from '../hooks/useFetch'
import { CATEGORY_LABEL } from '../utils/format'
import Loading from '../components/Loading'
import ErrorMessage from '../components/ErrorMessage'
import { Button, ChipTabs, EmptyState, PageHeader, SearchIcon } from '../components/ui'

const CATEGORY_OPTIONS: { value: ExamCategory | null; label: string }[] = [
  { value: null, label: '전체' },
  { value: 'NATIONAL', label: CATEGORY_LABEL.NATIONAL },
  { value: 'LANGUAGE', label: CATEGORY_LABEL.LANGUAGE },
  { value: 'PRIVATE', label: CATEGORY_LABEL.PRIVATE },
]
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
      <PageHeader title="시험 찾기" description="관심 있는 시험을 구독하면 접수 일정을 알려드려요" />

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
        <ChipTabs options={CATEGORY_OPTIONS} value={category} onChange={(c) => updateParams({ category: c })} />

        <form onSubmit={handleSearch} className="relative sm:ml-auto sm:w-64">
          <SearchIcon className="pointer-events-none absolute left-3.5 top-1/2 h-[18px] w-[18px] -translate-y-1/2 text-ink-400" />
          <input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="시험 이름 검색"
            className="h-10 w-full rounded-xl bg-white pl-10 pr-4 text-[15px] outline-none ring-brand-500 placeholder:text-ink-400 focus:ring-2"
          />
        </form>
      </div>

      <div className="mt-6">
        {loading && <Loading />}
        {error && <ErrorMessage message={error} onRetry={reload} />}

        {data && data.content.length === 0 && (
          <div className="rounded-3xl bg-white">
            <EmptyState title="조건에 맞는 시험이 없어요" description="다른 이름으로 검색해보세요" />
          </div>
        )}

        {data && data.content.length > 0 && (
          <>
            <p className="mb-3 text-sm text-ink-500">
              총 <strong className="text-ink-900">{data.totalElements}</strong>개
            </p>
            <ul className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
              {data.content.map((exam) => (
                <li key={exam.examId}>
                  <Link
                    to={`/exams/${exam.examId}`}
                    className="group flex h-full flex-col rounded-3xl bg-white p-5 transition hover:-translate-y-0.5 hover:shadow-[0_8px_24px_rgba(0,0,0,0.06)]"
                  >
                    <span className="text-[13px] font-semibold text-brand-500">{exam.categoryName}</span>
                    <h2 className="mt-1 text-[17px] font-bold text-ink-900 group-hover:text-brand-600">{exam.name}</h2>
                    <p className="mt-1 text-sm text-ink-500">{exam.organizer ?? '시행기관 정보 없음'}</p>
                    <div className="mt-auto pt-4">
                      <span className="rounded-md bg-ink-50 px-2 py-1 text-xs font-medium text-ink-500">
                        유효기간 {exam.validityMonths ? `${exam.validityMonths}개월` : '영구'}
                      </span>
                    </div>
                  </Link>
                </li>
              ))}
            </ul>

            {data.totalPages > 1 && (
              <div className="mt-8 flex items-center justify-center gap-3">
                <Button variant="ghost" size="sm" disabled={page === 0} onClick={() => updateParams({ page: page - 1 })}>
                  이전
                </Button>
                <span className="text-sm font-semibold text-ink-500">
                  {page + 1} / {data.totalPages}
                </span>
                <Button variant="ghost" size="sm" disabled={!data.hasNext} onClick={() => updateParams({ page: page + 1 })}>
                  다음
                </Button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  )
}
