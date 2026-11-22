import { useState, type FormEvent } from 'react'
import type { ExamCategory, ExamDetail, ExamRequest } from '../../api/types'
import { CATEGORY_LABEL } from '../../utils/format'
import ErrorMessage from '../ErrorMessage'
import { Button, SelectField, TextField } from '../ui'

/**
 * 시험 등록·수정 폼. initial이 있으면 수정 모드 (종목코드는 동기화 기준이라 수정 불가)
 */
export default function ExamForm(props: {
  initial?: ExamDetail
  submitting: boolean
  error: string | null
  onSubmit: (request: ExamRequest) => void
  onCancel: () => void
}) {
  const { initial } = props
  const [examCode, setExamCode] = useState(initial?.examCode ?? '')
  const [name, setName] = useState(initial?.name ?? '')
  const [category, setCategory] = useState<ExamCategory>(initial?.category ?? 'PRIVATE')
  const [organizer, setOrganizer] = useState(initial?.organizer ?? '')
  const [validityMonths, setValidityMonths] = useState(initial?.validityMonths ? String(initial.validityMonths) : '')
  const [officialUrl, setOfficialUrl] = useState(initial?.officialUrl ?? '')

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    props.onSubmit({
      examCode: initial ? undefined : examCode.trim() || null,
      name: name.trim(),
      category,
      organizer: organizer.trim() || null,
      validityMonths: validityMonths ? Number(validityMonths) : null,
      officialUrl: officialUrl.trim() || null,
    })
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <TextField label="시험 이름" value={name} onChange={(e) => setName(e.target.value)} maxLength={100} required />
        <SelectField label="분류" value={category} onChange={(e) => setCategory(e.target.value as ExamCategory)}>
          {(Object.keys(CATEGORY_LABEL) as ExamCategory[]).map((key) => (
            <option key={key} value={key}>
              {CATEGORY_LABEL[key]}
            </option>
          ))}
        </SelectField>
        <TextField label="시행기관" value={organizer} onChange={(e) => setOrganizer(e.target.value)} maxLength={100} />
        <TextField
          label="유효기간 (개월)"
          type="number"
          min={1}
          max={120}
          value={validityMonths}
          onChange={(e) => setValidityMonths(e.target.value)}
          placeholder="비우면 영구"
          hint="예: TOEIC 24, AWS 36"
        />
        <TextField
          label="공식 사이트"
          type="url"
          value={officialUrl}
          onChange={(e) => setOfficialUrl(e.target.value)}
          placeholder="https://"
          maxLength={300}
        />
        <TextField
          label="외부 코드 (선택)"
          value={examCode}
          onChange={(e) => setExamCode(e.target.value)}
          maxLength={20}
          disabled={!!initial}
          hint={initial ? '등록 후에는 바꿀 수 없어요' : '국가자격이면 Q-Net 종목코드를 넣으세요. 일정이 자동 동기화돼요'}
        />
      </div>

      {props.error && <ErrorMessage message={props.error} />}

      <div className="flex justify-end gap-2">
        <Button variant="ghost" onClick={props.onCancel}>
          취소
        </Button>
        <Button type="submit" disabled={props.submitting}>
          {props.submitting ? '저장 중…' : initial ? '수정하기' : '등록하기'}
        </Button>
      </div>
    </form>
  )
}
