import type { FormEvent, ReactNode } from 'react'
import ErrorMessage from './ErrorMessage'

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
    <div className="max-w-sm mx-auto mt-8">
      <h1 className="text-2xl font-bold text-center">{props.title}</h1>
      <form onSubmit={handleSubmit} className="mt-8 space-y-4 bg-white p-6 rounded-xl border border-gray-200">
        {props.children}
        {props.error && <ErrorMessage message={props.error} />}
        <button
          type="submit"
          disabled={props.submitting}
          className="w-full py-2.5 rounded-md bg-indigo-600 text-white font-medium hover:bg-indigo-700 disabled:opacity-50"
        >
          {props.submitting ? '처리 중…' : props.submitLabel}
        </button>
      </form>
      <p className="mt-4 text-center text-sm text-gray-500">{props.footer}</p>
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
    <label className="block">
      <span className="text-sm font-medium text-gray-700">{props.label}</span>
      <input
        type={props.type ?? 'text'}
        value={props.value}
        onChange={(e) => props.onChange(e.target.value)}
        placeholder={props.placeholder}
        autoComplete={props.autoComplete}
        required
        className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
      />
    </label>
  )
}
