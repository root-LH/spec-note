import { useState, type FormEvent } from 'react'
import type { ExamStage, Schedule, ScheduleRequest } from '../../api/types'
import ErrorMessage from '../ErrorMessage'
import { Button, SelectField, TextField } from '../ui'

const STAGE_LABEL: Record<ExamStage, string> = {
  SINGLE: '단일 (필기·실기 구분 없음)',
  WRITTEN: '필기',
  PRACTICAL: '실기',
}

/**
 * 회차 일정 등록·수정 폼.
 * 수정 모드에서는 회차명·단계를 바꿀 수 없음 (중복 판단 기준이라 바꾸려면 삭제 후 재등록)
 */
export default function ScheduleForm(props: {
  initial?: Schedule
  submitting: boolean
  error: string | null
  onSubmit: (request: ScheduleRequest) => void
  onCancel: () => void
}) {
  const { initial } = props
  const [roundName, setRoundName] = useState(initial?.roundName ?? '')
  const [stage, setStage] = useState<ExamStage>(initial?.stage ?? 'SINGLE')
  const [regStart, setRegStart] = useState(initial?.regStart ?? '')
  const [regEnd, setRegEnd] = useState(initial?.regEnd ?? '')
  const [examDate, setExamDate] = useState(initial?.examDate ?? '')
  const [resultDate, setResultDate] = useState(initial?.resultDate ?? '')

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    props.onSubmit({
      roundName: roundName.trim(),
      stage,
      regStart: regStart || null,
      regEnd: regEnd || null,
      examDate,
      resultDate: resultDate || null,
    })
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4 rounded-2xl bg-ink-50 p-5">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <TextField
          label="회차명"
          value={roundName}
          onChange={(e) => setRoundName(e.target.value)}
          placeholder="예: 2027년 1회, 제60회"
          maxLength={50}
          required
          disabled={!!initial}
          className="[&_input]:bg-white"
        />
        <SelectField
          label="단계"
          value={stage}
          onChange={(e) => setStage(e.target.value as ExamStage)}
          disabled={!!initial}
          className="[&_select]:bg-white"
        >
          {(Object.keys(STAGE_LABEL) as ExamStage[]).map((key) => (
            <option key={key} value={key}>
              {STAGE_LABEL[key]}
            </option>
          ))}
        </SelectField>
        <TextField
          label="접수 시작"
          type="date"
          value={regStart}
          onChange={(e) => setRegStart(e.target.value)}
          className="[&_input]:bg-white"
        />
        <TextField
          label="접수 마감"
          type="date"
          value={regEnd}
          min={regStart || undefined}
          onChange={(e) => setRegEnd(e.target.value)}
          className="[&_input]:bg-white"
        />
        <TextField
          label="시험일"
          type="date"
          value={examDate}
          min={regEnd || undefined}
          onChange={(e) => setExamDate(e.target.value)}
          required
          className="[&_input]:bg-white"
        />
        <TextField
          label="합격 발표"
          type="date"
          value={resultDate}
          min={examDate || undefined}
          onChange={(e) => setResultDate(e.target.value)}
          className="[&_input]:bg-white"
        />
      </div>

      {initial?.synced && (
        <p className="text-sm text-warn-600">
          공공데이터로 동기화된 일정이에요. 다음 동기화 때 공식 일정으로 다시 바뀔 수 있어요.
        </p>
      )}
      {props.error && <ErrorMessage message={props.error} />}

      <div className="flex justify-end gap-2">
        <Button variant="ghost" onClick={props.onCancel} className="bg-white">
          취소
        </Button>
        <Button type="submit" disabled={props.submitting}>
          {props.submitting ? '저장 중…' : initial ? '수정하기' : '추가하기'}
        </Button>
      </div>
    </form>
  )
}
