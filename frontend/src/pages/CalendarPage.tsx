import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { examApi, subscriptionApi } from '../api/endpoints'
import type { ExamCategory, IsoDate, Schedule } from '../api/types'
import { useFetch } from '../hooks/useFetch'
import { CATEGORY_LABEL, todayIso, toIsoDate } from '../utils/format'
import Loading from '../components/Loading'
import ErrorMessage from '../components/ErrorMessage'
import { Button, ChevronIcon, ChipTabs } from '../components/ui'

type EventKind = 'REG_START' | 'REG_END' | 'EXAM' | 'RESULT'

interface CalendarEvent {
  date: IsoDate
  kind: EventKind
  schedule: Schedule
}

const EVENT_STYLE: Record<EventKind, { label: string; chip: string; dot: string }> = {
  REG_START: { label: '접수시작', chip: 'bg-brand-50 text-brand-600', dot: 'bg-brand-500' },
  REG_END: { label: '접수마감', chip: 'bg-warn-50 text-warn-600', dot: 'bg-warn-600' },
  EXAM: { label: '시험', chip: 'bg-danger-50 text-danger-500', dot: 'bg-danger-500' },
  RESULT: { label: '발표', chip: 'bg-success-50 text-success-600', dot: 'bg-success-600' },
}

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토']

const CATEGORY_OPTIONS: { value: ExamCategory | null; label: string }[] = [
  { value: null, label: '전체' },
  { value: 'NATIONAL', label: CATEGORY_LABEL.NATIONAL },
  { value: 'LANGUAGE', label: CATEGORY_LABEL.LANGUAGE },
  { value: 'PRIVATE', label: CATEGORY_LABEL.PRIVATE },
]

/** 달력에 그릴 6주(42일) 날짜 목록. 첫 칸은 그 달 1일이 속한 주의 일요일 */
function buildMonthGrid(year: number, month: number): Date[] {
  const first = new Date(year, month, 1)
  const start = new Date(year, month, 1 - first.getDay())
  return Array.from({ length: 42 }, (_, i) => new Date(start.getFullYear(), start.getMonth(), start.getDate() + i))
}

/** 일정 하나 → 날짜별 이벤트 최대 4개 (접수 시작·마감, 시험, 발표) */
function toEvents(schedule: Schedule): CalendarEvent[] {
  const pairs: [IsoDate | null, EventKind][] = [
    [schedule.regStart, 'REG_START'],
    [schedule.regEnd, 'REG_END'],
    [schedule.examDate, 'EXAM'],
    [schedule.resultDate, 'RESULT'],
  ]
  return pairs
    .filter((pair): pair is [IsoDate, EventKind] => pair[0] !== null)
    .map(([date, kind]) => ({ date, kind, schedule }))
}

export default function CalendarPage() {
  const now = new Date()
  const [year, setYear] = useState(now.getFullYear())
  const [month, setMonth] = useState(now.getMonth()) // 0 = 1월
  const [category, setCategory] = useState<ExamCategory | null>(null)
  const [onlySubscribed, setOnlySubscribed] = useState(false)

  const grid = useMemo(() => buildMonthGrid(year, month), [year, month])
  const from = toIsoDate(grid[0])
  const to = toIsoDate(grid[grid.length - 1])

  // 화면에 보이는 6주 범위만 조회 (서버 제한 93일 이내)
  const schedules = useFetch(
    () => examApi.schedules({ from, to, category: category ?? undefined }),
    [from, to, category],
  )
  const subscriptions = useFetch(() => subscriptionApi.list(), [])

  // 날짜별로 이벤트 묶기: { "2026-12-13": [이벤트, ...] }
  const eventsByDate = useMemo(() => {
    const subscribedIds = new Set(subscriptions.data?.map((s) => s.examId))
    const map = new Map<IsoDate, CalendarEvent[]>()
    for (const schedule of schedules.data ?? []) {
      if (onlySubscribed && !subscribedIds.has(schedule.examId)) continue
      for (const event of toEvents(schedule)) {
        map.set(event.date, [...(map.get(event.date) ?? []), event])
      }
    }
    return map
  }, [schedules.data, subscriptions.data, onlySubscribed])

  const moveMonth = (diff: number) => {
    const next = new Date(year, month + diff, 1)
    setYear(next.getFullYear())
    setMonth(next.getMonth())
  }

  const goToday = () => {
    setYear(now.getFullYear())
    setMonth(now.getMonth())
  }

  const today = todayIso()
  const iconButton =
    'inline-flex h-9 w-9 items-center justify-center rounded-full bg-white text-ink-600 transition hover:bg-ink-100'

  return (
    <div className="space-y-4">
      <div className="mb-2 flex items-center gap-2">
        <h1 className="text-[26px] font-bold tracking-tight">
          {year}년 {month + 1}월
        </h1>
        <div className="ml-auto flex items-center gap-1.5">
          <button type="button" onClick={() => moveMonth(-1)} className={iconButton} aria-label="이전 달">
            <ChevronIcon direction="left" className="h-5 w-5" />
          </button>
          <Button variant="ghost" size="sm" onClick={goToday} className="bg-white">
            오늘
          </Button>
          <button type="button" onClick={() => moveMonth(1)} className={iconButton} aria-label="다음 달">
            <ChevronIcon direction="right" className="h-5 w-5" />
          </button>
        </div>
      </div>

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
        <ChipTabs options={CATEGORY_OPTIONS} value={category} onChange={setCategory} />
        <label className="flex cursor-pointer items-center gap-2 text-sm font-semibold text-ink-600 sm:ml-auto">
          <input
            type="checkbox"
            checked={onlySubscribed}
            onChange={(e) => setOnlySubscribed(e.target.checked)}
            className="h-4 w-4 accent-brand-500"
          />
          구독한 시험만
        </label>
      </div>

      <div className="flex flex-wrap gap-4 text-xs font-medium text-ink-500">
        {Object.values(EVENT_STYLE).map((style) => (
          <span key={style.label} className="flex items-center gap-1.5">
            <span className={`h-2 w-2 rounded-full ${style.dot}`} />
            {style.label}
          </span>
        ))}
      </div>

      {schedules.error && <ErrorMessage message={schedules.error} onRetry={schedules.reload} />}

      <div className="overflow-hidden rounded-3xl bg-white">
        <div className="grid grid-cols-7 border-b border-ink-50 text-center text-xs font-semibold">
          {WEEKDAYS.map((day, i) => (
            <div key={day} className={`py-3 ${i === 0 ? 'text-danger-500' : i === 6 ? 'text-brand-500' : 'text-ink-400'}`}>
              {day}
            </div>
          ))}
        </div>

        {schedules.loading && !schedules.data ? (
          <Loading />
        ) : (
          <div className="grid grid-cols-7">
            {grid.map((date, index) => {
              const iso = toIsoDate(date)
              const inMonth = date.getMonth() === month
              const events = eventsByDate.get(iso) ?? []
              const isLastColumn = index % 7 === 6
              return (
                <div
                  key={iso}
                  className={`min-h-24 border-b border-ink-50 p-1.5 sm:min-h-28 ${isLastColumn ? '' : 'border-r'} ${
                    inMonth ? '' : 'bg-ink-50/50'
                  }`}
                >
                  <div
                    className={`mb-1 flex h-7 w-7 items-center justify-center rounded-full text-[13px] font-semibold ${
                      iso === today ? 'bg-brand-500 text-white' : inMonth ? 'text-ink-700' : 'text-ink-300'
                    }`}
                  >
                    {date.getDate()}
                  </div>
                  <div className="space-y-1">
                    {events.map((event) => (
                      <Link
                        key={`${event.schedule.scheduleId}-${event.kind}`}
                        to={`/exams/${event.schedule.examId}`}
                        title={`${event.schedule.examName} ${event.schedule.roundName} ${EVENT_STYLE[event.kind].label}`}
                        className={`block truncate rounded-md px-1.5 py-0.5 text-[11px] font-semibold leading-tight transition hover:opacity-75 ${
                          EVENT_STYLE[event.kind].chip
                        }`}
                      >
                        {event.schedule.examName} {EVENT_STYLE[event.kind].label}
                      </Link>
                    ))}
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </div>
    </div>
  )
}
