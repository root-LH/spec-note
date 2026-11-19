import { useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import { examApi, subscriptionApi } from '../api/endpoints'
import { getErrorMessage } from '../api/client'
import { useAuth } from '../auth/useAuth'
import { useFetch } from '../hooks/useFetch'
import Loading from '../components/Loading'
import ErrorMessage from '../components/ErrorMessage'
import ScheduleItem from '../components/ScheduleItem'
import { Badge } from '../components/Badges'

export default function ExamDetailPage() {
  const examId = Number(useParams().examId)
  const { user } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const exam = useFetch(() => examApi.get(examId), [examId])

  // 로그인한 경우에만 구독 여부 확인
  const subscriptions = useFetch(
    () => (user ? subscriptionApi.list() : Promise.resolve([])),
    [user],
  )
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
    <div>
      <Link to="/exams" className="text-sm text-gray-500 hover:text-gray-900">
        ← 시험 목록
      </Link>

      <div className="mt-4 bg-white rounded-xl border border-gray-200 p-6">
        <div className="flex flex-col sm:flex-row sm:items-start gap-4">
          <div>
            <Badge>{data.categoryName}</Badge>
            <h1 className="mt-2 text-2xl font-bold">{data.name}</h1>
            <p className="mt-1 text-sm text-gray-500">{data.organizer ?? '시행기관 정보 없음'}</p>
            <p className="mt-3 text-sm text-gray-600">
              유효기간 <strong>{data.validityMonths ? `${data.validityMonths}개월` : '영구'}</strong>
              {data.officialUrl && (
                <>
                  {' · '}
                  <a href={data.officialUrl} target="_blank" rel="noreferrer" className="text-indigo-600 hover:underline">
                    공식 사이트
                  </a>
                </>
              )}
            </p>
          </div>

          <div className="sm:ml-auto">
            <button
              onClick={handleToggle}
              disabled={toggling}
              className={`px-4 py-2 rounded-md text-sm font-medium disabled:opacity-50 ${
                subscribed
                  ? 'bg-white border border-gray-300 text-gray-700 hover:bg-gray-50'
                  : 'bg-indigo-600 text-white hover:bg-indigo-700'
              }`}
            >
              {!user ? '로그인하고 구독하기' : subscribed ? '✓ 구독 중' : '+ 일정 알림 구독'}
            </button>
            {subscribed && <p className="mt-1 text-xs text-gray-400 text-right">다시 누르면 구독 해제</p>}
          </div>
        </div>
        {toggleError && (
          <div className="mt-4">
            <ErrorMessage message={toggleError} />
          </div>
        )}
      </div>

      <section className="mt-6 bg-white rounded-xl border border-gray-200 p-6">
        <h2 className="font-semibold">다가오는 일정</h2>
        {data.upcomingSchedules.length === 0 ? (
          <p className="mt-4 text-sm text-gray-500">등록된 일정이 없어요.</p>
        ) : (
          <ul className="mt-2 divide-y divide-gray-100">
            {data.upcomingSchedules.map((schedule) => (
              <ScheduleItem key={schedule.scheduleId} schedule={schedule} />
            ))}
          </ul>
        )}
      </section>
    </div>
  )
}
