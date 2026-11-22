import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { adminApi, examApi } from '../../api/endpoints'
import { getErrorMessage } from '../../api/client'
import type { ExamRequest, ExamSyncResult, NotificationRunResult } from '../../api/types'
import { useFetch } from '../../hooks/useFetch'
import { todayIso } from '../../utils/format'
import Loading from '../../components/Loading'
import ErrorMessage from '../../components/ErrorMessage'
import ExamForm from '../../components/admin/ExamForm'
import { Badge } from '../../components/Badges'
import { Button, Card, ChevronIcon, EmptyState, PageHeader, SearchIcon, SectionHeader, TextField } from '../../components/ui'

const PAGE_SIZE = 20

/** 결과 숫자 한 칸 */
function ResultItem({ label, value, tone = 'text-ink-900' }: { label: string; value: number | string; tone?: string }) {
  return (
    <div className="rounded-xl bg-ink-50 px-3 py-2.5">
      <p className="text-xs text-ink-400">{label}</p>
      <p className={`mt-0.5 text-lg font-bold ${tone}`}>{value}</p>
    </div>
  )
}

export default function AdminPage() {
  return (
    <div className="space-y-4">
      <PageHeader title="관리자" description="시험·회차 일정 관리와 배치 수동 실행" />
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <SyncCard />
        <NotificationRunCard />
      </div>
      <ExamManageCard />
    </div>
  )
}

/** 공공데이터 동기화 수동 실행 */
function SyncCard() {
  const [running, setRunning] = useState(false)
  const [result, setResult] = useState<ExamSyncResult | null>(null)
  const [error, setError] = useState<string | null>(null)

  const handleRun = async () => {
    setRunning(true)
    setError(null)
    try {
      setResult(await adminApi.syncExams())
    } catch (e) {
      setError(getErrorMessage(e))
    } finally {
      setRunning(false)
    }
  }

  return (
    <Card>
      <SectionHeader title="공공데이터 동기화" meta="매일 03:00 자동" />
      <p className="text-sm text-ink-500">Q-Net 기사 종목과 회차 일정을 지금 바로 가져와요. 1~2분 걸릴 수 있어요.</p>
      <Button onClick={handleRun} disabled={running} className="mt-4">
        {running ? '동기화 중…' : '지금 동기화'}
      </Button>

      {error && (
        <div className="mt-4">
          <ErrorMessage message={error} />
        </div>
      )}
      {result && (
        <div className="mt-4 grid grid-cols-3 gap-2">
          <ResultItem label="새 종목" value={result.catalogFailed ? '실패' : result.examsAdded} />
          <ResultItem label="새 일정" value={result.created} tone="text-brand-500" />
          <ResultItem label="변경" value={result.updated} />
          <ResultItem label="변경 없음" value={result.unchanged} tone="text-ink-400" />
          <ResultItem label="건너뜀" value={result.invalid} tone="text-warn-600" />
          <ResultItem label="실패 종목" value={result.failedExams} tone={result.failedExams ? 'text-danger-500' : 'text-ink-400'} />
        </div>
      )}
    </Card>
  )
}

/** 알림 배치 수동 실행 (날짜를 바꿔가며 테스트 가능) */
function NotificationRunCard() {
  const [date, setDate] = useState(todayIso())
  const [running, setRunning] = useState(false)
  const [result, setResult] = useState<NotificationRunResult | null>(null)
  const [error, setError] = useState<string | null>(null)

  const handleRun = async (event: FormEvent) => {
    event.preventDefault()
    setRunning(true)
    setError(null)
    try {
      setResult(await adminApi.runNotifications(date))
    } catch (e) {
      setError(getErrorMessage(e))
    } finally {
      setRunning(false)
    }
  }

  return (
    <Card>
      <SectionHeader title="알림 배치" meta="매일 08:00 자동" />
      <p className="text-sm text-ink-500">기준일을 정해 알림을 보내요. 같은 날짜로 여러 번 실행해도 한 번만 발송돼요.</p>
      <form onSubmit={handleRun} className="mt-4 flex items-end gap-2">
        <TextField label="기준일" type="date" value={date} onChange={(e) => setDate(e.target.value)} required className="flex-1" />
        <Button type="submit" disabled={running} className="h-12">
          {running ? '실행 중…' : '실행'}
        </Button>
      </form>

      {error && (
        <div className="mt-4">
          <ErrorMessage message={error} />
        </div>
      )}
      {result && (
        <div className="mt-4 grid grid-cols-3 gap-2">
          <ResultItem label="발송" value={result.sent} tone="text-brand-500" />
          <ResultItem label="실패" value={result.failed} tone={result.failed ? 'text-danger-500' : 'text-ink-400'} />
          <ResultItem label="건너뜀" value={result.skipped} tone="text-ink-400" />
        </div>
      )}
    </Card>
  )
}

/** 시험 목록 + 새 시험 등록 */
function ExamManageCard() {
  const navigate = useNavigate()
  const [keywordInput, setKeywordInput] = useState('')
  const [keyword, setKeyword] = useState('')
  const [page, setPage] = useState(0)
  const [creating, setCreating] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)

  const exams = useFetch(
    () => examApi.search({ keyword: keyword || undefined, page, size: PAGE_SIZE }),
    [keyword, page],
  )

  const handleSearch = (event: FormEvent) => {
    event.preventDefault()
    setPage(0)
    setKeyword(keywordInput.trim())
  }

  const handleCreate = async (request: ExamRequest) => {
    setSubmitting(true)
    setFormError(null)
    try {
      const { id } = await adminApi.createExam(request)
      navigate(`/admin/exams/${id}`) // 등록 후 바로 회차 일정을 넣을 수 있게 이동
    } catch (e) {
      setFormError(getErrorMessage(e))
      setSubmitting(false)
    }
  }

  return (
    <Card>
      <SectionHeader
        title="시험 관리"
        meta={exams.data ? `${exams.data.totalElements}개` : undefined}
        action={!creating && <Button size="sm" onClick={() => setCreating(true)}>+ 시험 등록</Button>}
      />

      {creating && (
        <div className="mb-6 rounded-2xl bg-ink-50 p-5 [&_input]:bg-white [&_select]:bg-white">
          <h3 className="mb-4 font-bold">새 시험 등록</h3>
          <ExamForm
            submitting={submitting}
            error={formError}
            onSubmit={handleCreate}
            onCancel={() => {
              setCreating(false)
              setFormError(null)
            }}
          />
        </div>
      )}

      <form onSubmit={handleSearch} className="relative mb-3">
        <SearchIcon className="pointer-events-none absolute left-3.5 top-1/2 h-[18px] w-[18px] -translate-y-1/2 text-ink-400" />
        <input
          value={keywordInput}
          onChange={(e) => setKeywordInput(e.target.value)}
          placeholder="시험 이름 검색"
          className="h-11 w-full rounded-xl bg-ink-50 pl-10 pr-4 text-[15px] outline-none ring-brand-500 placeholder:text-ink-400 focus:bg-white focus:ring-2"
        />
      </form>

      {exams.loading && <Loading />}
      {exams.error && <ErrorMessage message={exams.error} onRetry={exams.reload} />}
      {exams.data && exams.data.content.length === 0 && <EmptyState title="검색 결과가 없어요" />}

      {exams.data && exams.data.content.length > 0 && (
        <>
          <ul className="divide-y divide-ink-50">
            {exams.data.content.map((exam) => (
              <li key={exam.examId}>
                <Link
                  to={`/admin/exams/${exam.examId}`}
                  className="-mx-2 flex items-center gap-3 rounded-xl px-2 py-3 transition hover:bg-ink-50"
                >
                  <div className="min-w-0 flex-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="font-semibold">{exam.name}</span>
                      <Badge>{exam.categoryName}</Badge>
                      {exam.source === 'API' ? (
                        <Badge className="bg-brand-50 text-brand-600">자동 동기화</Badge>
                      ) : (
                        <Badge className="bg-warn-50 text-warn-600">수동 관리</Badge>
                      )}
                    </div>
                    <p className="mt-0.5 text-sm text-ink-400">
                      {exam.organizer ?? '시행기관 없음'} · 유효기간 {exam.validityMonths ? `${exam.validityMonths}개월` : '영구'}
                    </p>
                  </div>
                  <ChevronIcon direction="right" className="h-4 w-4 shrink-0 text-ink-300" />
                </Link>
              </li>
            ))}
          </ul>

          {exams.data.totalPages > 1 && (
            <div className="mt-4 flex items-center justify-center gap-3">
              <Button variant="ghost" size="sm" disabled={page === 0} onClick={() => setPage(page - 1)}>
                이전
              </Button>
              <span className="text-sm font-semibold text-ink-500">
                {page + 1} / {exams.data.totalPages}
              </span>
              <Button variant="ghost" size="sm" disabled={!exams.data.hasNext} onClick={() => setPage(page + 1)}>
                다음
              </Button>
            </div>
          )}
        </>
      )}
    </Card>
  )
}
