import { useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import { examApi, subscriptionApi } from '../api/endpoints'
import { getErrorMessage } from '../api/client'
import { useAuth } from '../auth/useAuth'
import { useFetch } from '../hooks/useFetch'
import Loading from '../components/Loading'
import ErrorMessage from '../components/ErrorMessage'
import ScheduleItem from '../components/ScheduleItem'
import { Button, Card, ChevronIcon, EmptyState, SectionHeader } from '../components/ui'

export default function ExamDetailPage() {
  const examId = Number(useParams().examId)
  const { user } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const exam = useFetch(() => examApi.get(examId), [examId])

  // 로그인한 경우에만 구독 여부 확인
  const subscriptions = useFetch(() => (user ? subscriptionApi.list() : Promise.resolve([])), [user])
  const [subscribedOverride, setSubscribedOverride] = useState<boolean | null>(null)
  const subscribed = subscribedOverride ?? subscriptions.data?.some((s) => s.examId === examId) ?? false

  const [toggling, setToggling] = useState(false)
  const [toggleError, setToggleError] = useState<string | null>(null)

  const handleToggle = async () => {
    if (!user) {
      navigate('/login', { state: { from: location.pathname } })
      return
    }
    setToggling(true)
    setToggleError(null)
    try {
      if (subscribed) {
        await subscriptionApi.unsubscribe(examId)
      } else {
        await subscriptionApi.subscribe(examId)
      }
      setSubscribedOverride(!subscribed)
    } catch (e) {
      setToggleError(getErrorMessage(e))
    } finally {
      setToggling(false)
    }
  }

  if (exam.loading) return <Loading />
  if (exam.error || !exam.data) return <ErrorMessage message={exam.error ?? '시험을 찾을 수 없어요.'} />

  const data = exam.data

  return (
    <div className="space-y-4">
      <Link to="/exams" className="inline-flex items-center gap-1 text-sm font-semibold text-ink-400 hover:text-ink-700">
        <ChevronIcon direction="left" className="h-4 w-4" />
        시험 목록
      </Link>

      <Card className="sm:p-8">
        <span className="text-[13px] font-semibold text-brand-500">{data.categoryName}</span>
        <h1 className="mt-1 text-[28px] font-bold tracking-tight">{data.name}</h1>
        <p className="mt-1 text-[15px] text-ink-500">{data.organizer ?? '시행기관 정보 없음'}</p>

        <div className="mt-5 flex flex-wrap gap-2 text-sm">
          <span className="rounded-lg bg-ink-50 px-3 py-1.5 font-medium text-ink-600">
            유효기간 {data.validityMonths ? `${data.validityMonths}개월` : '영구'}
          </span>
          {data.officialUrl && (
            <a
              href={data.officialUrl}
              target="_blank"
              rel="noreferrer"
              className="rounded-lg bg-ink-50 px-3 py-1.5 font-medium text-ink-600 hover:bg-ink-100"
            >
              공식 사이트 ↗
            </a>
          )}
        </div>

        <div className="mt-6">
          <Button
            size="lg"
            variant={subscribed ? 'ghost' : 'primary'}
            onClick={handleToggle}
            disabled={toggling}
            className="w-full sm:w-auto sm:min-w-56"
          >
            {!user ? '로그인하고 일정 알림 받기' : subscribed ? '✓ 구독 중 · 알림을 보내드려요' : '일정 알림 구독하기'}
          </Button>
          {subscribed && <p className="mt-2 text-xs text-ink-400">다시 누르면 구독이 해제돼요</p>}
        </div>

        {toggleError && (
          <div className="mt-4">
            <ErrorMessage message={toggleError} />
          </div>
        )}
      </Card>

      <Card>
        <SectionHeader title="다가오는 일정" meta={`${data.upcomingSchedules.length}건`} />
        {data.upcomingSchedules.length === 0 ? (
          <EmptyState title="등록된 일정이 없어요" description="일정이 공고되면 여기에 표시돼요" />
        ) : (
          <ul className="divide-y divide-ink-50">
            {data.upcomingSchedules.map((schedule) => (
              <ScheduleItem key={schedule.scheduleId} schedule={schedule} />
            ))}
          </ul>
        )}
      </Card>
    </div>
  )
}
