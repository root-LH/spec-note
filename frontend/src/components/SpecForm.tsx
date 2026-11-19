import { useState, type FormEvent } from 'react'
import type { ExamSummary, Spec, SpecRequest } from '../api/types'
import { CATEGORY_LABEL, todayIso } from '../utils/format'
import ErrorMessage from './ErrorMessage'

const inputClass =
  'mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500'

/**
 * 스펙 등록·수정 폼.
 * initial이 있으면 수정 모드, 없으면 등록 모드.
 */
export default function SpecForm(props: {
  exams: ExamSummary[]
  initial?: Spec
  submitting: boolean
  error: string | null
  onSubmit: (request: SpecRequest) => void
  onCancel: () => void
}) {
  const { initial } = props
  const [useCustom, setUseCustom] = useState(initial ? initial.examId === null : false)
  const [examId, setExamId] = useState<string>(initial?.examId ? String(initial.examId) : '')
  const [customName, setCustomName] = useState(initial && initial.examId === null ? initial.name : '')
  const [score, setScore] = useState(initial?.score ?? '')
  const [certNumber, setCertNumber] = useState(initial?.certNumber ?? '')
  const [acquiredDate, setAcquiredDate] = useState(initial?.acquiredDate ?? '')
  const [expireDate, setExpireDate] = useState(initial?.expireDate ?? '')

  const selectedExam = props.exams.find((exam) => String(exam.examId) === examId)

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    props.onSubmit({
      examId: useCustom || !examId ? null : Number(examId),
      customName: useCustom ? customName.trim() : null,
      score: score.trim() || null,
      certNumber: certNumber.trim() || null,
      acquiredDate,
      expireDate: expireDate || null, // 비우면 서버가 유효기간으로 자동 계산
    })
  }

  return (
    <form onSubmit={handleSubmit} className="bg-white rounded-xl border border-indigo-200 p-6 space-y-4">
      <h2 className="font-semibold">{initial ? '스펙 수정' : '스펙 추가'}</h2>

      <div className="flex gap-4 text-sm">
        <label className="flex items-center gap-2">
          <input type="radio" checked={!useCustom} onChange={() => setUseCustom(false)} />
          목록에서 선택
        </label>
        <label className="flex items-center gap-2">
          <input type="radio" checked={useCustom} onChange={() => setUseCustom(true)} />
          직접 입력
        </label>
      </div>

      {useCustom ? (
        <label className="block">
          <span className="text-sm font-medium text-gray-700">이름</span>
          <input
            value={customName}
            onChange={(e) => setCustomName(e.target.value)}
            placeholder="예: 리눅스마스터 2급"
            maxLength={100}
            required
            className={inputClass}
          />
        </label>
      ) : (
        <label className="block">
          <span className="text-sm font-medium text-gray-700">시험</span>
          <select value={examId} onChange={(e) => setExamId(e.target.value)} required className={inputClass}>
            <option value="">선택하세요</option>
            {(['NATIONAL', 'LANGUAGE', 'PRIVATE'] as const).map((category) => (
              <optgroup key={category} label={CATEGORY_LABEL[category]}>
                {props.exams
                  .filter((exam) => exam.category === category)
                  .map((exam) => (
                    <option key={exam.examId} value={exam.examId}>
                      {exam.name}
                    </option>
                  ))}
              </optgroup>
            ))}
          </select>
          {selectedExam && (
            <span className="mt-1 block text-xs text-gray-400">
              유효기간 {selectedExam.validityMonths ? `${selectedExam.validityMonths}개월` : '영구'}
            </span>
          )}
        </label>
      )}

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <label className="block">
          <span className="text-sm font-medium text-gray-700">점수 / 등급</span>
          <input
            value={score}
            onChange={(e) => setScore(e.target.value)}
            placeholder="예: 855, IH, 1급"
            maxLength={20}
            className={inputClass}
          />
        </label>
        <label className="block">
          <span className="text-sm font-medium text-gray-700">자격번호</span>
          <input
            value={certNumber}
            onChange={(e) => setCertNumber(e.target.value)}
            maxLength={50}
            className={inputClass}
          />
        </label>
        <label className="block">
          <span className="text-sm font-medium text-gray-700">취득일</span>
          <input
            type="date"
            value={acquiredDate}
            onChange={(e) => setAcquiredDate(e.target.value)}
            max={todayIso()}
            required
            className={inputClass}
          />
        </label>
        <label className="block">
          <span className="text-sm font-medium text-gray-700">만료일 (선택)</span>
          <input
            type="date"
            value={expireDate}
            onChange={(e) => setExpireDate(e.target.value)}
            min={acquiredDate || undefined}
            className={inputClass}
          />
          <span className="mt-1 block text-xs text-gray-400">비워두면 시험 유효기간으로 자동 계산돼요</span>
        </label>
      </div>

      {props.error && <ErrorMessage message={props.error} />}

      <div className="flex justify-end gap-2">
        <button
          type="button"
          onClick={props.onCancel}
          className="px-4 py-2 rounded-md border border-gray-300 text-sm hover:bg-gray-50"
        >
          취소
        </button>
        <button
          type="submit"
          disabled={props.submitting}
          className="px-4 py-2 rounded-md bg-indigo-600 text-white text-sm font-medium hover:bg-indigo-700 disabled:opacity-50"
        >
          {props.submitting ? '저장 중…' : '저장'}
        </button>
      </div>
    </form>
  )
}
