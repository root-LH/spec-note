import { useState } from 'react'
import { examApi, specApi } from '../api/endpoints'
import { getErrorMessage } from '../api/client'
import type { Spec, SpecRequest } from '../api/types'
import { useFetch } from '../hooks/useFetch'
import { dDayLabel, formatDate } from '../utils/format'
import Loading from '../components/Loading'
import ErrorMessage from '../components/ErrorMessage'
import SpecForm from '../components/SpecForm'
import { SpecStatusBadge, dDayTone } from '../components/Badges'
import { Button, Card, EmptyState, PageHeader } from '../components/ui'

/** 폼 상태: 닫힘 / 새로 추가 / 특정 스펙 수정 */
type FormMode = { type: 'closed' } | { type: 'create' } | { type: 'edit'; spec: Spec }

export default function SpecsPage() {
  const specs = useFetch(() => specApi.list(), [])
  // 시험 선택 목록용 (시험이 100개를 넘으면 검색형 선택으로 바꾸는 게 좋음)
  const exams = useFetch(() => examApi.search({ size: 100 }), [])

  const [mode, setMode] = useState<FormMode>({ type: 'closed' })
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)

  const openForm = (next: FormMode) => {
    setFormError(null)
    setMode(next)
  }

  const handleSubmit = async (request: SpecRequest) => {
    setSubmitting(true)
    setFormError(null)
    try {
      if (mode.type === 'edit') {
        await specApi.update(mode.spec.specId, request)
      } else {
        await specApi.create(request)
      }
      setMode({ type: 'closed' })
      specs.reload()
    } catch (e) {
      setFormError(getErrorMessage(e))
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async (spec: Spec) => {
    if (!window.confirm(`'${spec.name}' 스펙을 삭제할까요?`)) return
    setActionError(null)
    try {
      await specApi.remove(spec.specId)
      if (mode.type === 'edit' && mode.spec.specId === spec.specId) {
        setMode({ type: 'closed' })
      }
      specs.reload()
    } catch (e) {
      setActionError(getErrorMessage(e))
    }
  }

  return (
    <div className="space-y-4">
      <PageHeader
        title="내 스펙"
        description="유효기간이 있는 스펙은 만료 30일·7일 전에 알려드려요"
        action={
          mode.type === 'closed' && (
            <Button onClick={() => openForm({ type: 'create' })}>+ 스펙 추가</Button>
          )
        }
      />

      {mode.type !== 'closed' && (
        <SpecForm
          // key를 바꿔서 다른 스펙을 수정할 때 폼 입력값이 새로 채워지게 함
          key={mode.type === 'edit' ? mode.spec.specId : 'new'}
          exams={exams.data?.content ?? []}
          initial={mode.type === 'edit' ? mode.spec : undefined}
          submitting={submitting}
          error={formError}
          onSubmit={handleSubmit}
          onCancel={() => setMode({ type: 'closed' })}
        />
      )}

      {actionError && <ErrorMessage message={actionError} />}
      {specs.loading && <Loading />}
      {specs.error && <ErrorMessage message={specs.error} onRetry={specs.reload} />}

      {specs.data && specs.data.length === 0 && mode.type === 'closed' && (
        <Card>
          <EmptyState
            title="아직 등록한 스펙이 없어요"
            description="자격증이나 어학 점수를 등록하면 만료일을 대신 챙겨드려요"
            action={<Button onClick={() => openForm({ type: 'create' })}>첫 스펙 등록하기</Button>}
          />
        </Card>
      )}

      {specs.data && specs.data.length > 0 && (
        <Card className="p-2 sm:p-3">
          <ul>
            {specs.data.map((spec) => (
              <li
                key={spec.specId}
                className="flex flex-col gap-3 rounded-2xl p-4 transition hover:bg-ink-50 sm:flex-row sm:items-center"
              >
                {/* 이름 첫 글자 아이콘 */}
                <div className="hidden h-11 w-11 shrink-0 items-center justify-center rounded-full bg-brand-50 text-[17px] font-bold text-brand-600 sm:flex">
                  {spec.name.charAt(0)}
                </div>

                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="text-[16px] font-semibold">{spec.name}</span>
                    {spec.score && <span className="text-[15px] font-semibold text-brand-500">{spec.score}</span>}
                    <SpecStatusBadge status={spec.status} label={spec.statusName} />
                  </div>
                  <p className="mt-0.5 text-sm text-ink-400">
                    {formatDate(spec.acquiredDate)} 취득
                    {' · '}
                    {spec.expireDate ? `${formatDate(spec.expireDate)} 만료` : '만료 없음'}
                    {spec.certNumber && ` · ${spec.certNumber}`}
                  </p>
                </div>

                <div className="flex items-center gap-2 sm:gap-3">
                  {spec.daysUntilExpire !== null && (
                    <span className={`mr-auto text-xl font-bold sm:mr-2 ${dDayTone(spec.daysUntilExpire)}`}>
                      {dDayLabel(spec.daysUntilExpire)}
                    </span>
                  )}
                  <Button variant="ghost" size="sm" onClick={() => openForm({ type: 'edit', spec })}>
                    수정
                  </Button>
                  <Button variant="danger" size="sm" onClick={() => handleDelete(spec)}>
                    삭제
                  </Button>
                </div>
              </li>
            ))}
          </ul>
        </Card>
      )}
    </div>
  )
}
