import { useState } from 'react'
import { examApi, specApi } from '../api/endpoints'
import { getErrorMessage } from '../api/client'
import type { Spec, SpecRequest } from '../api/types'
import { useFetch } from '../hooks/useFetch'
import { dDayLabel, formatDate } from '../utils/format'
import Loading from '../components/Loading'
import ErrorMessage from '../components/ErrorMessage'
import SpecForm from '../components/SpecForm'
import { SpecStatusBadge } from '../components/Badges'

/** 폼 상태: 닫힘 / 새로 추가 / 특정 스펙 수정 */
type FormMode = { type: 'closed' } | { type: 'create' } | { type: 'edit'; spec: Spec }

const D_DAY_COLOR: Record<Spec['status'], string> = {
  ACTIVE: 'text-gray-500',
  EXPIRING_SOON: 'text-amber-600',
  EXPIRED: 'text-red-600',
  PERMANENT: 'text-gray-400',
}

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
    <div>
      <div className="flex items-center">
        <div>
          <h1 className="text-2xl font-bold">내 스펙</h1>
          <p className="mt-1 text-sm text-gray-500">유효기간이 있는 스펙은 만료 30일·7일 전에 알려드려요.</p>
        </div>
        {mode.type === 'closed' && (
          <button
            onClick={() => openForm({ type: 'create' })}
            className="ml-auto px-4 py-2 rounded-md bg-indigo-600 text-white text-sm font-medium hover:bg-indigo-700"
          >
            + 스펙 추가
          </button>
        )}
      </div>

      {mode.type !== 'closed' && (
        <div className="mt-6">
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
        </div>
      )}

      <div className="mt-6">
        {actionError && (
          <div className="mb-4">
            <ErrorMessage message={actionError} />
          </div>
        )}
        {specs.loading && <Loading />}
        {specs.error && <ErrorMessage message={specs.error} onRetry={specs.reload} />}

        {specs.data && specs.data.length === 0 && mode.type === 'closed' && (
          <div className="py-16 text-center bg-white rounded-xl border border-dashed border-gray-300">
            <p className="text-gray-500">아직 등록한 스펙이 없어요.</p>
            <button onClick={() => openForm({ type: 'create' })} className="mt-3 text-sm text-indigo-600 hover:underline">
              첫 스펙 등록하기
            </button>
          </div>
        )}

        {specs.data && specs.data.length > 0 && (
          <ul className="bg-white rounded-xl border border-gray-200 divide-y divide-gray-100">
            {specs.data.map((spec) => (
              <li key={spec.specId} className="p-4 flex flex-col sm:flex-row sm:items-center gap-3">
                <div className="flex-1">
                  <div className="flex items-center gap-2 flex-wrap">
                    <span className="font-semibold">{spec.name}</span>
                    {spec.score && <span className="text-sm text-gray-600">{spec.score}</span>}
                    <SpecStatusBadge status={spec.status} label={spec.statusName} />
                  </div>
                  <p className="mt-1 text-sm text-gray-500">
                    취득 {formatDate(spec.acquiredDate)}
                    {' · '}
                    만료 {spec.expireDate ? formatDate(spec.expireDate) : '없음'}
                    {spec.certNumber && ` · ${spec.certNumber}`}
                  </p>
                </div>

                {spec.daysUntilExpire !== null && (
                  <span className={`text-lg font-bold ${D_DAY_COLOR[spec.status]}`}>
                    {dDayLabel(spec.daysUntilExpire)}
                  </span>
                )}

                <div className="flex gap-2 text-sm">
                  <button
                    onClick={() => openForm({ type: 'edit', spec })}
                    className="px-3 py-1.5 rounded-md border border-gray-300 hover:bg-gray-50"
                  >
                    수정
                  </button>
                  <button
                    onClick={() => handleDelete(spec)}
                    className="px-3 py-1.5 rounded-md border border-red-200 text-red-600 hover:bg-red-50"
                  >
                    삭제
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}
