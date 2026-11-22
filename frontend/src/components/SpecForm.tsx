import { useState, type FormEvent } from 'react'
import type { ExamSummary, Spec, SpecRequest } from '../api/types'
import { CATEGORY_LABEL, todayIso } from '../utils/format'
import ErrorMessage from './ErrorMessage'
import { Button, Card, ChipTabs, SelectField, TextField } from './ui'

const SOURCE_OPTIONS = [
  { value: false, label: '목록에서 선택' },
  { value: true, label: '직접 입력' },
]

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
    <Card className="ring-2 ring-brand-100">
      <form onSubmit={handleSubmit} className="space-y-5">
        <h2 className="text-lg font-bold">{initial ? '스펙 수정' : '스펙 추가'}</h2>

        <ChipTabs options={SOURCE_OPTIONS} value={useCustom} onChange={setUseCustom} />

        {useCustom ? (
          <TextField
            label="이름"
            value={customName}
            onChange={(e) => setCustomName(e.target.value)}
            placeholder="예: 리눅스마스터 2급"
            maxLength={100}
            required
          />
        ) : (
          <SelectField
            label="시험"
            value={examId}
            onChange={(e) => setExamId(e.target.value)}
            required
            hint={
              selectedExam
                ? `유효기간 ${selectedExam.validityMonths ? `${selectedExam.validityMonths}개월` : '영구'}`
                : undefined
            }
          >
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
          </SelectField>
        )}

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <TextField
            label="점수 / 등급"
            value={score}
            onChange={(e) => setScore(e.target.value)}
            placeholder="예: 855, IH, 1급"
            maxLength={20}
          />
          <TextField label="자격번호" value={certNumber} onChange={(e) => setCertNumber(e.target.value)} maxLength={50} />
          <TextField
            label="취득일"
            type="date"
            value={acquiredDate}
            onChange={(e) => setAcquiredDate(e.target.value)}
            max={todayIso()}
            required
          />
          <TextField
            label="만료일 (선택)"
            type="date"
            value={expireDate}
            onChange={(e) => setExpireDate(e.target.value)}
            min={acquiredDate || undefined}
            hint="비워두면 시험 유효기간으로 자동 계산돼요"
          />
        </div>

        {props.error && <ErrorMessage message={props.error} />}

        <div className="flex justify-end gap-2 pt-1">
          <Button variant="ghost" onClick={props.onCancel}>
            취소
          </Button>
          <Button type="submit" disabled={props.submitting}>
            {props.submitting ? '저장 중…' : '저장하기'}
          </Button>
        </div>
      </form>
    </Card>
  )
}
