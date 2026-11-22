import type { FormEvent, ReactNode } from 'react'
import ErrorMessage from './ErrorMessage'
import { Button, TextField } from './ui'

/** 로그인·회원가입 화면이 같이 쓰는 폼 틀 */
export function AuthForm(props: {
  title: string
  submitLabel: string
  submitting: boolean
  error: string | null
  onSubmit: () => void
  footer: ReactNode
  children: ReactNode
}) {
  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    props.onSubmit()
  }

  return (
    <div className="mx-auto mt-4 max-w-sm sm:mt-10">
      <h1 className="text-[26px] font-bold tracking-tight">{props.title}</h1>
      <p className="mt-1.5 text-[15px] text-ink-500">시험 일정과 스펙 만료일을 놓치지 마세요.</p>

      <form onSubmit={handleSubmit} className="mt-8 space-y-4 rounded-3xl bg-white p-6">
        {props.children}
        {props.error && <ErrorMessage message={props.error} />}
        <Button type="submit" size="lg" disabled={props.submitting} className="w-full">
          {props.submitting ? '처리 중…' : props.submitLabel}
        </Button>
      </form>

      <p className="mt-5 text-center text-sm text-ink-500">{props.footer}</p>
    </div>
  )
}

export function Field(props: {
  label: string
  type?: string
  value: string
  onChange: (value: string) => void
  placeholder?: string
  autoComplete?: string
}) {
  return (
    <TextField
      label={props.label}
      type={props.type ?? 'text'}
      value={props.value}
      onChange={(e) => props.onChange(e.target.value)}
      placeholder={props.placeholder}
      autoComplete={props.autoComplete}
      required
    />
  )
}
