import { Link } from 'react-router-dom'
import type { IsoDate, Schedule } from '../api/types'
import { formatDate } from '../utils/format'
import { RegistrationBadge } from './Badges'

/** "2026-12-13" → "12.13 (일)" */
function monthDay(value: IsoDate | null): string {
  return value ? formatDate(value).slice(5) : '미정'
}

/** 일정 한 줄: 회차·상태 + 접수/시험/발표 날짜 */
export default function ScheduleItem({ schedule, showExamName = false }: { schedule: Schedule; showExamName?: boolean }) {
  const title = [
    showExamName ? schedule.examName : null,
    schedule.roundName,
    schedule.stage === 'SINGLE' ? null : schedule.stageName,
  ]
    .filter(Boolean)
    .join(' ')

  const dates: [string, string][] = [
    ['접수', `${monthDay(schedule.regStart)} ~ ${monthDay(schedule.regEnd)}`],
    ['시험', monthDay(schedule.examDate)],
    ['발표', monthDay(schedule.resultDate)],
  ]

  return (
    <li className="py-4 first:pt-0 last:pb-0">
      <div className="flex flex-wrap items-center gap-2">
        {showExamName ? (
          <Link to={`/exams/${schedule.examId}`} className="text-[15px] font-semibold text-ink-900 hover:text-brand-600">
            {title}
          </Link>
        ) : (
          <span className="text-[15px] font-semibold text-ink-900">{title}</span>
        )}
        <RegistrationBadge status={schedule.registrationStatus} label={schedule.registrationStatusName} />
      </div>

      <dl className="mt-2.5 grid grid-cols-1 gap-2 sm:grid-cols-[1.4fr_1fr_1fr]">
        {dates.map(([label, value]) => (
          <div key={label} className="rounded-xl bg-ink-50 px-3.5 py-2.5">
            <dt className="text-xs text-ink-400">{label}</dt>
            <dd className="mt-0.5 text-sm font-semibold text-ink-700">{value}</dd>
          </div>
        ))}
      </dl>
    </li>
  )
}
