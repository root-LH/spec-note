import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { adminApi, examApi } from '../../api/endpoints'
import { getErrorMessage } from '../../api/client'
import type { ExamRequest, Schedule, ScheduleRequest } from '../../api/types'
import { useFetch } from '../../hooks/useFetch'
import { formatDate } from '../../utils/format'
import Loading from '../../components/Loading'
import ErrorMessage from '../../components/ErrorMessage'
import ExamForm from '../../components/admin/ExamForm'
import ScheduleForm from '../../components/admin/ScheduleForm'
import { Badge, RegistrationBadge } from '../../components/Badges'
import { Button, Card, ChevronIcon, EmptyState, SectionHeader } from '../../components/ui'

/** 회차 폼 상태: 닫힘 / 새로 추가 / 특정 회차 수정 */
type ScheduleMode = { type: 'closed' } | { type: 'create' } | { type: 'edit'; schedule: Schedule }

export default function AdminExamPage() {
  const examId = Number(useParams().examId)
  const exam = useFetch(() => examApi.get(examId), [examId])
  const schedules = useFetch(() => adminApi.schedules(examId), [examId])

  const [editingExam, setEditingExam] = useState(false)
  const [scheduleMode, setScheduleMode] = useState<ScheduleMode>({ type: 'closed' })
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)

  const handleExamSubmit = async (request: ExamRequest) => {
    setSubmitting(true)
    setFormError(null)
    try {
      // 종목코드는 수정 불가 → 나머지 값만 전송
      await adminApi.updateExam(examId, {
        name: request.name,
        category: request.category,
        organizer: request.organizer,
        validityMonths: request.validityMonths,
        officialUrl: request.officialUrl,
      })
      setEditingExam(false)
      exam.reload()
    } catch (e) {
      setFormError(getErrorMessage(e))
    } finally {
      setSubmitting(false)
    }
  }

  const handleScheduleSubmit = async (request: ScheduleRequest) => {
    setSubmitting(true)
    setFormError(null)
    try {
      if (scheduleMode.type === 'edit') {
        // 회차명·단계는 바꿀 수 없음 → 날짜만 전송
        await adminApi.updateSchedule(scheduleMode.schedule.scheduleId, {
          regStart: request.regStart,
          regEnd: request.regEnd,
          examDate: request.examDate,
          resultDate: request.resultDate,
        })
      } else {
        await adminApi.addSchedule(examId, request)
      }
      setScheduleMode({ type: 'closed' })
      schedules.reload()
    } catch (e) {
      setFormError(getErrorMessage(e))
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async (schedule: Schedule) => {
    if (!window.confirm(`'${schedule.roundName} ${schedule.stageName}' 일정을 삭제할까요?`)) return
    setActionError(null)
    try {
      await adminApi.deleteSchedule(schedule.scheduleId)
      schedules.reload()
    } catch (e) {
      setActionError(getErrorMessage(e))
    }
  }

  const openScheduleForm = (mode: ScheduleMode) => {
    setFormError(null)
    setScheduleMode(mode)
  }

  if (exam.loading) return <Loading />
  if (exam.error || !exam.data) return <ErrorMessage message={exam.error ?? '시험을 찾을 수 없어요.'} />

  const data = exam.data

  return (
    <div className="space-y-4">
      <Link to="/admin" className="inline-flex items-center gap-1 text-sm font-semibold text-ink-400 hover:text-ink-700">
        <ChevronIcon direction="left" className="h-4 w-4" />
        관리자 홈
      </Link>

      <Card>
        {editingExam ? (
          <>
            <h2 className="mb-4 text-lg font-bold">시험 정보 수정</h2>
            <ExamForm
              initial={data}
              submitting={submitting}
              error={formError}
              onSubmit={handleExamSubmit}
              onCancel={() => {
                setEditingExam(false)
                setFormError(null)
              }}
            />
          </>
        ) : (
          <div className="flex flex-col gap-4 sm:flex-row sm:items-start">
            <div>
              <div className="flex flex-wrap items-center gap-2">
                <Badge>{data.categoryName}</Badge>
                {data.source === 'API' ? (
                  <Badge className="bg-brand-50 text-brand-600">자동 동기화 · {data.examCode}</Badge>
                ) : (
                  <Badge className="bg-warn-50 text-warn-600">수동 관리</Badge>
                )}
              </div>
              <h1 className="mt-2 text-[26px] font-bold tracking-tight">{data.name}</h1>
              <p className="mt-1 text-[15px] text-ink-500">
                {data.organizer ?? '시행기관 없음'} · 유효기간 {data.validityMonths ? `${data.validityMonths}개월` : '영구'}
              </p>
              {data.officialUrl && (
                <a href={data.officialUrl} target="_blank" rel="noreferrer" className="mt-1 inline-block text-sm text-brand-500 hover:underline">
                  {data.officialUrl}
                </a>
              )}
            </div>
            <div className="flex gap-2 sm:ml-auto">
              <Link
                to={`/exams/${examId}`}
                className="inline-flex h-9 items-center rounded-xl bg-ink-50 px-3.5 text-sm font-semibold text-ink-700 hover:bg-ink-100"
              >
                사용자 화면
              </Link>
              <Button variant="ghost" size="sm" onClick={() => setEditingExam(true)}>
                정보 수정
              </Button>
            </div>
          </div>
        )}
      </Card>

      <Card>
        <SectionHeader
          title="회차 일정"
          meta={schedules.data ? `${schedules.data.length}건 · 지난 회차 포함` : undefined}
          action={
            scheduleMode.type === 'closed' && (
              <Button size="sm" onClick={() => openScheduleForm({ type: 'create' })}>
                + 회차 추가
              </Button>
            )
          }
        />

        {scheduleMode.type !== 'closed' && (
          <div className="mb-5">
            <ScheduleForm
              key={scheduleMode.type === 'edit' ? scheduleMode.schedule.scheduleId : 'new'}
              initial={scheduleMode.type === 'edit' ? scheduleMode.schedule : undefined}
              submitting={submitting}
              error={formError}
              onSubmit={handleScheduleSubmit}
              onCancel={() => setScheduleMode({ type: 'closed' })}
            />
          </div>
        )}

        {actionError && (
          <div className="mb-4">
            <ErrorMessage message={actionError} />
          </div>
        )}
        {schedules.loading && <Loading />}
        {schedules.error && <ErrorMessage message={schedules.error} onRetry={schedules.reload} />}

        {schedules.data && schedules.data.length === 0 && scheduleMode.type === 'closed' && (
          <EmptyState
            title="등록된 회차가 없어요"
            description="시행기관 공지를 확인해 회차 일정을 추가해주세요"
            action={<Button onClick={() => openScheduleForm({ type: 'create' })}>첫 회차 추가하기</Button>}
          />
        )}

        {schedules.data && schedules.data.length > 0 && (
          <ul className="divide-y divide-ink-50">
            {schedules.data.map((schedule) => (
              <li key={schedule.scheduleId} className="flex flex-col gap-3 py-4 first:pt-0 last:pb-0 sm:flex-row sm:items-center">
                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="font-semibold">
                      {schedule.roundName}
                      {schedule.stage !== 'SINGLE' && ` ${schedule.stageName}`}
                    </span>
                    <RegistrationBadge status={schedule.registrationStatus} label={schedule.registrationStatusName} />
                    {schedule.synced && <Badge className="bg-brand-50 text-brand-600">자동</Badge>}
                  </div>
                  <p className="mt-1 text-sm text-ink-500">
                    접수 {formatDate(schedule.regStart)} ~ {formatDate(schedule.regEnd)}
                  </p>
                  <p className="text-sm text-ink-500">
                    시험 {formatDate(schedule.examDate)} · 발표 {formatDate(schedule.resultDate)}
                  </p>
                </div>
                <div className="flex gap-2">
                  <Button variant="ghost" size="sm" onClick={() => openScheduleForm({ type: 'edit', schedule })}>
                    수정
                  </Button>
                  <Button variant="danger" size="sm" onClick={() => handleDelete(schedule)}>
                    삭제
                  </Button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </Card>
    </div>
  )
}
