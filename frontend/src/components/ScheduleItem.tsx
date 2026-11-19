import { Link } from 'react-router-dom'
import type { Schedule } from '../api/types'
import { formatDate } from '../utils/format'
import { RegistrationBadge } from './Badges'

/** 일정 한 줄: 회차·상태 + 접수/시험/발표 날짜 */
export default function ScheduleItem({ schedule, showExamName = false }: { schedule: Schedule; showExamName?: boolean }) {
  const title = [
    showExamName ? schedule.examName : null,
    schedule.roundName,
    schedule.stage === 'SINGLE' ? null : schedule.stageName,
  ]
    .filter(Boolean)
    .join(' ')

  return (
    <li className="py-3">
      <div className="flex items-center gap-2">
        {showExamName ? (
          <Link to={`/exams/${schedule.examId}`} className="font-medium hover:text-indigo-600">
            {title}
          </Link>
        ) : (
          <span className="font-medium">{title}</span>
        )}
        <RegistrationBadge status={schedule.registrationStatus} label={schedule.registrationStatusName} />
      </div>
      <dl className="mt-1 grid grid-cols-1 sm:grid-cols-3 gap-x-4 text-sm text-gray-600">
        <div>
          <dt className="inline text-gray-400">접수 </dt>
          <dd className="inline">
            {formatDate(schedule.regStart)} ~ {formatDate(schedule.regEnd)}
          </dd>
        </div>
        <div>
          <dt className="inline text-gray-400">시험 </dt>
          <dd className="inline">{formatDate(schedule.examDate)}</dd>
        </div>
        <div>
          <dt className="inline text-gray-400">발표 </dt>
          <dd className="inline">{formatDate(schedule.resultDate)}</dd>
        </div>
      </dl>
    </li>
  )
}
