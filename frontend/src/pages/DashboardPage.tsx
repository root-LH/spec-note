import { Link } from 'react-router-dom'
import { dashboardApi } from '../api/endpoints'
import { useFetch } from '../hooks/useFetch'
import { dDayLabel, formatDate } from '../utils/format'
import Loading from '../components/Loading'
import ErrorMessage from '../components/ErrorMessage'
import ScheduleItem from '../components/ScheduleItem'
import { SpecStatusBadge } from '../components/Badges'

function SummaryTile({ label, value, tone }: { label: string; value: number; tone: string }) {
  return (
    <div className="bg-white rounded-xl border border-gray-200 p-4">
      <p className="text-sm text-gray-500">{label}</p>
      <p className={`mt-1 text-2xl font-bold ${tone}`}>{value}</p>
    </div>
  )
}

export default function DashboardPage() {
  const { data, loading, error, reload } = useFetch(() => dashboardApi.get(), [])

  if (loading) return <Loading />
  if (error || !data) return <ErrorMessage message={error ?? '대시보드를 불러오지 못했어요.'} onRetry={reload} />

  const { specSummary } = data

  return (
    <div>
      <h1 className="text-2xl font-bold">{data.nickname}님의 스펙 현황</h1>

      <div className="mt-6 grid grid-cols-2 sm:grid-cols-4 gap-3">
        <SummaryTile label="보유 스펙" value={specSummary.total} tone="text-gray-900" />
        <SummaryTile label="유효 · 영구" value={specSummary.active + specSummary.permanent} tone="text-green-600" />
        <SummaryTile label="만료 임박" value={specSummary.expiringSoon} tone="text-amber-600" />
        <SummaryTile label="만료" value={specSummary.expired} tone="text-red-600" />
      </div>

      <div className="mt-6 grid grid-cols-1 lg:grid-cols-2 gap-6">
        <section className="bg-white rounded-xl border border-gray-200 p-6">
          <div className="flex items-center">
            <h2 className="font-semibold">만료 예정 스펙</h2>
            <span className="ml-2 text-xs text-gray-400">90일 이내</span>
            <Link to="/specs" className="ml-auto text-sm text-indigo-600 hover:underline">
              내 스펙 관리
            </Link>
          </div>

          {data.expiringSpecs.length === 0 ? (
            <p className="mt-4 text-sm text-gray-500">
              {specSummary.total === 0 ? '아직 등록한 스펙이 없어요.' : '곧 만료되는 스펙이 없어요. 👍'}
            </p>
          ) : (
            <ul className="mt-2 divide-y divide-gray-100">
              {data.expiringSpecs.map((spec) => (
                <li key={spec.specId} className="py-3 flex items-center gap-3">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-medium">{spec.name}</span>
                      {spec.score && <span className="text-sm text-gray-500">{spec.score}</span>}
                      <SpecStatusBadge status={spec.status} label={spec.statusName} />
                    </div>
                    <p className="text-sm text-gray-500">만료일 {formatDate(spec.expireDate)}</p>
                  </div>
                  {spec.daysUntilExpire !== null && (
                    <span className="ml-auto text-lg font-bold text-amber-600">{dDayLabel(spec.daysUntilExpire)}</span>
                  )}
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="bg-white rounded-xl border border-gray-200 p-6">
          <div className="flex items-center">
            <h2 className="font-semibold">구독 시험 일정</h2>
            <span className="ml-2 text-xs text-gray-400">구독 {data.subscriptionCount}개</span>
            <Link to="/exams" className="ml-auto text-sm text-indigo-600 hover:underline">
              시험 찾기
            </Link>
          </div>

          {data.upcomingSchedules.length === 0 ? (
            <p className="mt-4 text-sm text-gray-500">
              {data.subscriptionCount === 0
                ? '구독한 시험이 없어요. 시험을 구독하면 일정이 여기에 표시돼요.'
                : '다가오는 일정이 없어요.'}
            </p>
          ) : (
            <ul className="mt-2 divide-y divide-gray-100">
              {data.upcomingSchedules.map((schedule) => (
                <ScheduleItem key={schedule.scheduleId} schedule={schedule} showExamName />
              ))}
            </ul>
          )}
        </section>
      </div>
    </div>
  )
}
