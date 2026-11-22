import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { examApi, subscriptionApi } from '../api/endpoints'
import type { ExamCategory, IsoDate, Schedule } from '../api/types'
import { useFetch } from '../hooks/useFetch'
import { CATEGORY_LABEL, formatDate, todayIso, toIsoDate } from '../utils/format'
import Loading from '../components/Loading'
import ErrorMessage from '../components/ErrorMessage'
import { Button, Card, ChevronIcon, ChipTabs, EmptyState } from '../components/ui'

type EventKind = 'REG_START' | 'REG_END' | 'EXAM' | 'RESULT'

interface CalendarEvent {
  date: IsoDate
  kind: EventKind
  schedule: Schedule
  subscribed: boolean
}

/** 순서 = 한 날짜 안에서 보여주는 순서 */
const EVENT_KINDS: EventKind[] = ['REG_START', 'REG_END', 'EXAM', 'RESULT']

const EVENT_STYLE: Record<EventKind, { label: string; chip: string; dot: string }> = {
  REG_START: { label: '접수시작', chip: 'bg-brand-50 text-brand-600', dot: 'bg-brand-500' },
  REG_END: { label: '접수마감', chip: 'bg-warn-50 text-warn-600', dot: 'bg-warn-600' },
  EXAM: { label: '시험', chip: 'bg-danger-50 text-danger-500', dot: 'bg-danger-500' },
  RESULT: { label: '발표', chip: 'bg-success-50 text-success-600', dot: 'bg-success-600' },
}

/** 달력 칸 하나에 이름까지 보여줄 최대 개수 (나머지는 "+N개 더") */
const MAX_VISIBLE_IN_CELL = 2

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
function toEvents(schedule: Schedule, subscribed: boolean): CalendarEvent[] {
  const pairs: [IsoDate | null, EventKind][] = [
    [schedule.regStart, 'REG_START'],
    [schedule.regEnd, 'REG_END'],
    [schedule.examDate, 'EXAM'],
    [schedule.resultDate, 'RESULT'],
  ]
  return pairs
    .filter((pair): pair is [IsoDate, EventKind] => pair[0] !== null)
    .map(([date, kind]) => ({ date, kind, schedule, subscribed }))
}

/** 구독한 시험 먼저 → 종류 순서 → 이름 순 */
function compareEvents(a: CalendarEvent, b: CalendarEvent): number {
  if (a.subscribed !== b.subscribed) return a.subscribed ? -1 : 1
  const kindDiff = EVENT_KINDS.indexOf(a.kind) - EVENT_KINDS.indexOf(b.kind)
  if (kindDiff !== 0) return kindDiff
  return a.schedule.examName.localeCompare(b.schedule.examName, 'ko')
}

function eventTitle(event: CalendarEvent): string {
  const { schedule } = event
  const stage = schedule.stage === 'SINGLE' ? '' : ` ${schedule.stageName}`
  return `${schedule.roundName}${stage}`
}

export default function CalendarPage() {
  const now = new Date()
  const [year, setYear] = useState(now.getFullYear())
  const [month, setMonth] = useState(now.getMonth()) // 0 = 1월
  const [category, setCategory] = useState<ExamCategory | null>(null)
  const [onlySubscribed, setOnlySubscribed] = useState(false)
  const [selectedDate, setSelectedDate] = useState<IsoDate>(todayIso())

  const grid = useMemo(() => buildMonthGrid(year, month), [year, month])
  const from = toIsoDate(grid[0])
  const to = toIsoDate(grid[grid.length - 1])

  // 화면에 보이는 6주 범위만 조회 (서버 제한 93일 이내)
  const schedules = useFetch(
    () => examApi.schedules({ from, to, category: category ?? undefined }),
    [from, to, category],
  )
  const subscriptions = useFetch(() => subscriptionApi.list(), [])

  // 날짜별로 이벤트 묶기: { "2026-12-13": [이벤트, ...] } (정렬까지 미리 해둠)
  const eventsByDate = useMemo(() => {
    const subscribedIds = new Set(subscriptions.data?.map((s) => s.examId))
    const map = new Map<IsoDate, CalendarEvent[]>()
    for (const schedule of schedules.data ?? []) {
      const subscribed = subscribedIds.has(schedule.examId)
      if (onlySubscribed && !subscribed) continue
      for (const event of toEvents(schedule, subscribed)) {
        const list = map.get(event.date)
        if (list) list.push(event)
        else map.set(event.date, [event])
      }
    }
    map.forEach((list) => list.sort(compareEvents))
    return map
  }, [schedules.data, subscriptions.data, onlySubscribed])

  const selectedEvents = eventsByDate.get(selectedDate) ?? []

  const moveMonth = (diff: number) => {
    const next = new Date(year, month + diff, 1)
    setYear(next.getFullYear())
    setMonth(next.getMonth())
    setSelectedDate(toIsoDate(next)) // 달을 넘기면 그 달 1일 선택
  }

  const goToday = () => {
    setYear(now.getFullYear())
    setMonth(now.getMonth())
    setSelectedDate(todayIso())
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
        {EVENT_KINDS.map((kind) => (
          <span key={kind} className="flex items-center gap-1.5">
            <span className={`h-2 w-2 rounded-full ${EVENT_STYLE[kind].dot}`} />
            {EVENT_STYLE[kind].label}
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
              const visible = events.slice(0, MAX_VISIBLE_IN_CELL)
              const hiddenCount = events.length - visible.length
              const kindsOfDay = EVENT_KINDS.filter((kind) => events.some((e) => e.kind === kind))
              const isSelected = iso === selectedDate

              return (
                <button
                  type="button"
                  key={iso}
                  onClick={() => setSelectedDate(iso)}
                  className={`relative flex min-h-20 flex-col items-stretch border-b border-ink-50 p-1.5 text-left transition sm:min-h-28 ${
                    index % 7 === 6 ? '' : 'border-r'
                  } ${inMonth ? 'hover:bg-ink-50/60' : 'bg-ink-50/50'} ${
                    isSelected ? 'z-10 ring-2 ring-inset ring-brand-500' : ''
                  }`}
                >
                  <span
                    className={`mb-1 flex h-7 w-7 items-center justify-center rounded-full text-[13px] font-semibold ${
                      iso === today ? 'bg-brand-500 text-white' : inMonth ? 'text-ink-700' : 'text-ink-300'
                    }`}
                  >
                    {date.getDate()}
                  </span>

                  {/* 모바일: 종류별 색 점 + 개수 */}
                  {events.length > 0 && (
                    <span className="flex flex-wrap items-center gap-0.5 px-1 sm:hidden">
                      {kindsOfDay.map((kind) => (
                        <span key={kind} className={`h-1.5 w-1.5 rounded-full ${EVENT_STYLE[kind].dot}`} />
                      ))}
                      <span className="ml-0.5 text-[10px] font-semibold text-ink-400">{events.length}</span>
                    </span>
                  )}

                  {/* PC: 최대 2개까지 이름 표시 + 나머지 개수 */}
                  <span className="hidden space-y-1 sm:block">
                    {visible.map((event) => (
                      <span
                        key={`${event.schedule.scheduleId}-${event.kind}`}
                        className={`block truncate rounded-md px-1.5 py-0.5 text-[11px] font-semibold leading-tight ${
                          EVENT_STYLE[event.kind].chip
                        }`}
                      >
                        {event.subscribed && '★ '}
                        {event.schedule.examName} {EVENT_STYLE[event.kind].label}
                      </span>
                    ))}
                    {hiddenCount > 0 && (
                      <span className="block px-1.5 text-[11px] font-semibold text-ink-400">+{hiddenCount}개 더</span>
                    )}
                  </span>
                </button>
              )
            })}
          </div>
        )}
      </div>

      <DayDetail date={selectedDate} events={selectedEvents} />
    </div>
  )
}

/** 선택한 날짜의 일정 전체를 종류별로 묶어서 표시 */
function DayDetail({ date, events }: { date: IsoDate; events: CalendarEvent[] }) {
  return (
    <Card>
      <div className="mb-4 flex items-baseline gap-2">
        <h2 className="text-lg font-bold">{formatDate(date)}</h2>
        <span className="text-sm text-ink-400">일정 {events.length}건</span>
      </div>

      {events.length === 0 ? (
        <EmptyState title="이 날은 일정이 없어요" description="달력에서 다른 날짜를 눌러보세요" />
      ) : (
        <div className="space-y-6">
          {EVENT_KINDS.map((kind) => {
            const group = events.filter((event) => event.kind === kind)
            if (group.length === 0) return null
            return (
              <section key={kind}>
                <h3 className="mb-2 flex items-center gap-2 text-sm font-bold text-ink-700">
                  <span className={`h-2 w-2 rounded-full ${EVENT_STYLE[kind].dot}`} />
                  {EVENT_STYLE[kind].label}
                  <span className="font-semibold text-ink-400">{group.length}</span>
                </h3>
                {/* 많을 때도 한눈에 보이도록 여러 칸으로 배치 */}
                <ul className="grid grid-cols-1 gap-1.5 sm:grid-cols-2 lg:grid-cols-3">
                  {group.map((event) => (
                    <li key={`${event.schedule.scheduleId}-${event.kind}`}>
                      <Link
                        to={`/exams/${event.schedule.examId}`}
                        className="flex items-center gap-2 rounded-xl bg-ink-50 px-3.5 py-2.5 transition hover:bg-ink-100"
                      >
                        <span className="min-w-0 flex-1">
                          <span className="block truncate text-sm font-semibold text-ink-900">
                            {event.subscribed && <span className="text-brand-500">★ </span>}
                            {event.schedule.examName}
                          </span>
                          <span className="block truncate text-xs text-ink-400">{eventTitle(event)}</span>
                        </span>
                        <ChevronIcon direction="right" className="h-4 w-4 shrink-0 text-ink-300" />
                      </Link>
                    </li>
                  ))}
                </ul>
              </section>
            )
          })}
        </div>
      )}
    </Card>
  )
}
