import { Link } from 'react-router-dom'
import { dashboardApi } from '../api/endpoints'
import { useFetch } from '../hooks/useFetch'
import { dDayLabel, formatDate } from '../utils/format'
import Loading from '../components/Loading'
import ErrorMessage from '../components/ErrorMessage'
import ScheduleItem from '../components/ScheduleItem'
import { SpecStatusBadge, dDayTone } from '../components/Badges'
import { Card, EmptyState, SectionHeader } from '../components/ui'

function Stat({ label, value, tone }: { label: string; value: number; tone: string }) {
  return (
    <div className="px-1">
      <p className="text-sm text-ink-500">{label}</p>
      <p className={`mt-1 text-[28px] font-bold tracking-tight ${tone}`}>
        {value}
        <span className="ml-0.5 text-base font-semibold text-ink-400">개</span>
      </p>
    </div>
  )
}

const moreLink = 'text-sm font-semibold text-ink-400 hover:text-ink-700'

export default function DashboardPage() {
  const { data, loading, error, reload } = useFetch(() => dashboardApi.get(), [])

  if (loading) return <Loading />
  if (error || !data) return <ErrorMessage message={error ?? '대시보드를 불러오지 못했어요.'} onRetry={reload} />

  const { specSummary, expiringSpecs, upcomingSchedules } = data

  // 첫 줄에 가장 중요한 정보를 한 문장으로
  const headline =
    expiringSpecs.length > 0 ? (
      <>
        곧 만료되는 스펙이 <span className="text-brand-500">{expiringSpecs.length}개</span> 있어요
      </>
    ) : upcomingSchedules.length > 0 ? (
      <>
        다가오는 시험 일정이 <span className="text-brand-500">{upcomingSchedules.length}개</span> 있어요
      </>
    ) : (
      '오늘도 스펙 관리를 시작해볼까요?'
    )

  return (
    <div className="space-y-4">
      <h1 className="mb-6 text-[26px] font-bold leading-snug tracking-tight">
        {data.nickname}님,
        <br />
        {headline}
      </h1>

      <Card>
        <div className="grid grid-cols-2 gap-y-6 sm:grid-cols-4">
          <Stat label="보유 스펙" value={specSummary.total} tone="text-ink-900" />
          <Stat label="유효 · 영구" value={specSummary.active + specSummary.permanent} tone="text-success-600" />
          <Stat label="만료 임박" value={specSummary.expiringSoon} tone="text-warn-600" />
          <Stat label="만료" value={specSummary.expired} tone="text-danger-500" />
        </div>
      </Card>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <Card>
          <SectionHeader
            title="만료 예정 스펙"
            meta="90일 이내"
            action={
              <Link to="/specs" className={moreLink}>
                관리하기 ›
              </Link>
            }
          />
          {expiringSpecs.length === 0 ? (
            <EmptyState
              title={specSummary.total === 0 ? '아직 등록한 스펙이 없어요' : '곧 만료되는 스펙이 없어요'}
              description={specSummary.total === 0 ? '스펙을 등록하면 만료 전에 알려드려요' : undefined}
            />
          ) : (
            <ul className="space-y-2">
              {expiringSpecs.map((spec) => (
                <li key={spec.specId} className="flex items-center gap-3 rounded-2xl bg-ink-50 px-4 py-3.5">
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="font-semibold">{spec.name}</span>
                      {spec.score && <span className="text-sm text-ink-500">{spec.score}</span>}
                      <SpecStatusBadge status={spec.status} label={spec.statusName} />
                    </div>
                    <p className="mt-0.5 text-sm text-ink-400">{formatDate(spec.expireDate)} 만료</p>
                  </div>
                  {spec.daysUntilExpire !== null && (
                    <span className={`ml-auto shrink-0 text-xl font-bold ${dDayTone(spec.daysUntilExpire)}`}>
                      {dDayLabel(spec.daysUntilExpire)}
                    </span>
                  )}
                </li>
              ))}
            </ul>
          )}
        </Card>

        <Card>
          <SectionHeader
            title="구독 시험 일정"
            meta={`구독 ${data.subscriptionCount}개`}
            action={
              <Link to="/exams" className={moreLink}>
                시험 찾기 ›
              </Link>
            }
          />
          {upcomingSchedules.length === 0 ? (
            <EmptyState
              title={data.subscriptionCount === 0 ? '구독한 시험이 없어요' : '다가오는 일정이 없어요'}
              description={data.subscriptionCount === 0 ? '시험을 구독하면 일정이 여기에 표시돼요' : undefined}
            />
          ) : (
            <ul className="divide-y divide-ink-50">
              {upcomingSchedules.map((schedule) => (
                <ScheduleItem key={schedule.scheduleId} schedule={schedule} showExamName />
              ))}
            </ul>
          )}
        </Card>
      </div>
    </div>
  )
}
