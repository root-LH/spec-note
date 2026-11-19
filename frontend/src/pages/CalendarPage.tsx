import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { examApi, subscriptionApi } from '../api/endpoints'
import type { ExamCategory, IsoDate, Schedule } from '../api/types'
import { useFetch } from '../hooks/useFetch'
import { CATEGORY_LABEL, todayIso, toIsoDate } from '../utils/format'
import Loading from '../components/Loading'
import ErrorMessage from '../components/ErrorMessage'

type EventKind = 'REG_START' | 'REG_END' | 'EXAM' | 'RESULT'

interface CalendarEvent {
  date: IsoDate
  kind: EventKind
  schedule: Schedule
}

const EVENT_STYLE: Record<EventKind, { label: string; className: string }> = {
  REG_START: { label: '접수시작', className: 'bg-blue-100 text-blue-700' },
  REG_END: { label: '접수마감', className: 'bg-amber-100 text-amber-700' },
  EXAM: { label: '시험', className: 'bg-red-100 text-red-700' },
  RESULT: { label: '발표', className: 'bg-green-100 text-green-700' },
}

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토']
const CATEGORIES: (ExamCategory | null)[] = [null, 'NATIONAL', 'LANGUAGE', 'PRIVATE']

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

  return (
    <div>
      <div className="flex flex-col sm:flex-row sm:items-center gap-3">
        <div className="flex items-center gap-2">
          <button onClick={() => moveMonth(-1)} className="px-2 py-1 rounded-md border border-gray-300 bg-white hover:bg-gray-50">
            ‹
          </button>
          <h1 className="text-2xl font-bold w-36 text-center">
            {year}년 {month + 1}월
          </h1>
          <button onClick={() => moveMonth(1)} className="px-2 py-1 rounded-md border border-gray-300 bg-white hover:bg-gray-50">
            ›
          </button>
          <button onClick={goToday} className="ml-1 px-3 py-1 rounded-md border border-gray-300 bg-white text-sm hover:bg-gray-50">
            오늘
          </button>
        </div>

        <div className="sm:ml-auto flex flex-wrap items-center gap-2">
          {CATEGORIES.map((c) => (
            <button
              key={c ?? 'ALL'}
              onClick={() => setCategory(c)}
              className={`px-3 py-1 rounded-full text-sm ${
                category === c ? 'bg-indigo-600 text-white' : 'bg-white border border-gray-300 text-gray-600 hover:bg-gray-50'
              }`}
            >
              {c ? CATEGORY_LABEL[c] : '전체'}
            </button>
          ))}
          <label className="ml-2 flex items-center gap-1.5 text-sm text-gray-600">
            <input type="checkbox" checked={onlySubscribed} onChange={(e) => setOnlySubscribed(e.target.checked)} />
            구독한 시험만
          </label>
        </div>
      </div>

      <div className="mt-3 flex flex-wrap gap-3 text-xs">
        {Object.values(EVENT_STYLE).map((style) => (
          <span key={style.label} className={`px-2 py-0.5 rounded ${style.className}`}>
            {style.label}
          </span>
        ))}
      </div>

      {schedules.error && (
        <div className="mt-4">
          <ErrorMessage message={schedules.error} onRetry={schedules.reload} />
        </div>
      )}

      <div className="mt-4 bg-white rounded-xl border border-gray-200 overflow-hidden">
        <div className="grid grid-cols-7 border-b border-gray-200 bg-gray-50 text-center text-xs font-medium">
          {WEEKDAYS.map((day, i) => (
            <div key={day} className={`py-2 ${i === 0 ? 'text-red-500' : i === 6 ? 'text-blue-500' : 'text-gray-500'}`}>
              {day}
            </div>
          ))}
        </div>

        {schedules.loading && !schedules.data ? (
          <Loading />
        ) : (
          <div className="grid grid-cols-7">
            {grid.map((date) => {
              const iso = toIsoDate(date)
              const inMonth = date.getMonth() === month
              const events = eventsByDate.get(iso) ?? []
              return (
                <div
                  key={iso}
                  className={`min-h-24 border-b border-r border-gray-100 p-1 ${inMonth ? '' : 'bg-gray-50/60'}`}
                >
                  <div
                    className={`text-xs mb-1 w-6 h-6 flex items-center justify-center rounded-full ${
                      iso === today ? 'bg-indigo-600 text-white font-bold' : inMonth ? 'text-gray-700' : 'text-gray-300'
                    }`}
                  >
                    {date.getDate()}
                  </div>
                  <div className="space-y-0.5">
                    {events.map((event) => (
                      <Link
                        key={`${event.schedule.scheduleId}-${event.kind}`}
                        to={`/exams/${event.schedule.examId}`}
                        title={`${event.schedule.examName} ${event.schedule.roundName} ${EVENT_STYLE[event.kind].label}`}
                        className={`block truncate rounded px-1 py-0.5 text-[11px] leading-tight hover:opacity-80 ${
                          EVENT_STYLE[event.kind].className
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
