import type { ButtonHTMLAttributes, InputHTMLAttributes, ReactNode, SelectHTMLAttributes } from 'react'

/*
 * 공통 UI 컴포넌트.
 * 화면마다 Tailwind 클래스를 반복해서 쓰지 않고, 디자인을 바꿀 때 이 파일만 고치면 되도록 모아둠.
 */

// ===== 카드 =====

export function Card({ className = '', children }: { className?: string; children: ReactNode }) {
  return <div className={`rounded-3xl bg-white p-6 ${className}`}>{children}</div>
}

// ===== 버튼 =====

type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger'
type ButtonSize = 'sm' | 'md' | 'lg'

const BUTTON_VARIANT: Record<ButtonVariant, string> = {
  primary: 'bg-brand-500 text-white hover:bg-brand-600',
  secondary: 'bg-brand-50 text-brand-600 hover:bg-brand-100',
  ghost: 'bg-ink-50 text-ink-700 hover:bg-ink-100',
  danger: 'bg-danger-50 text-danger-500 hover:bg-danger-100',
}

const BUTTON_SIZE: Record<ButtonSize, string> = {
  sm: 'h-9 rounded-xl px-3.5 text-sm',
  md: 'h-11 rounded-xl px-5 text-[15px]',
  lg: 'h-14 rounded-2xl px-6 text-base',
}

export function Button({
  variant = 'primary',
  size = 'md',
  type = 'button', // form 안에서 실수로 submit 되지 않도록 기본값을 button으로
  className = '',
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & { variant?: ButtonVariant; size?: ButtonSize }) {
  return (
    <button
      type={type}
      className={`inline-flex items-center justify-center gap-1.5 font-semibold transition active:scale-[0.98] disabled:pointer-events-none disabled:opacity-40 ${BUTTON_VARIANT[variant]} ${BUTTON_SIZE[size]} ${className}`}
      {...props}
    />
  )
}

// ===== 제목 =====

export function PageHeader({ title, description, action }: { title: ReactNode; description?: ReactNode; action?: ReactNode }) {
  return (
    <div className="mb-6 flex items-end gap-4">
      <div>
        <h1 className="text-[26px] font-bold leading-snug tracking-tight text-ink-900">{title}</h1>
        {description && <p className="mt-1.5 text-[15px] text-ink-500">{description}</p>}
      </div>
      {action && <div className="ml-auto shrink-0">{action}</div>}
    </div>
  )
}

export function SectionHeader({ title, meta, action }: { title: string; meta?: string; action?: ReactNode }) {
  return (
    <div className="mb-4 flex items-center gap-2">
      <h2 className="text-lg font-bold text-ink-900">{title}</h2>
      {meta && <span className="text-sm text-ink-400">{meta}</span>}
      {action && <div className="ml-auto">{action}</div>}
    </div>
  )
}

// ===== 탭(칩) =====

export function ChipTabs<T>({
  options,
  value,
  onChange,
}: {
  options: { value: T; label: string }[]
  value: T
  onChange: (value: T) => void
}) {
  return (
    <div className="flex gap-2 overflow-x-auto">
      {options.map((option) => (
        <button
          key={String(option.value)}
          type="button"
          onClick={() => onChange(option.value)}
          className={`h-9 shrink-0 rounded-full px-4 text-sm font-semibold transition ${
            option.value === value ? 'bg-ink-900 text-white' : 'bg-white text-ink-600 hover:bg-ink-100'
          }`}
        >
          {option.label}
        </button>
      ))}
    </div>
  )
}

// ===== 입력 =====

const FIELD_CLASS =
  'mt-1.5 h-12 w-full rounded-xl bg-ink-50 px-4 text-[15px] text-ink-900 outline-none ring-brand-500 transition placeholder:text-ink-400 focus:bg-white focus:ring-2'

export function TextField({
  label,
  hint,
  className = '',
  ...props
}: InputHTMLAttributes<HTMLInputElement> & { label: string; hint?: string }) {
  return (
    <label className={`block ${className}`}>
      <span className="text-[13px] font-medium text-ink-600">{label}</span>
      <input className={FIELD_CLASS} {...props} />
      {hint && <span className="mt-1.5 block text-xs text-ink-400">{hint}</span>}
    </label>
  )
}

export function SelectField({
  label,
  hint,
  className = '',
  children,
  ...props
}: SelectHTMLAttributes<HTMLSelectElement> & { label: string; hint?: string }) {
  return (
    <label className={`block ${className}`}>
      <span className="text-[13px] font-medium text-ink-600">{label}</span>
      <select className={FIELD_CLASS} {...props}>
        {children}
      </select>
      {hint && <span className="mt-1.5 block text-xs text-ink-400">{hint}</span>}
    </label>
  )
}

// ===== 토글 스위치 =====

export function Toggle({ checked, onChange, disabled }: { checked: boolean; onChange: () => void; disabled?: boolean }) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      onClick={onChange}
      disabled={disabled}
      className={`relative inline-flex h-7 w-12 shrink-0 items-center rounded-full transition disabled:opacity-40 ${
        checked ? 'bg-brand-500' : 'bg-ink-200'
      }`}
    >
      <span
        className={`inline-block h-6 w-6 rounded-full bg-white shadow-sm transition ${
          checked ? 'translate-x-[22px]' : 'translate-x-0.5'
        }`}
      />
    </button>
  )
}

// ===== 빈 상태 =====

export function EmptyState({ title, description, action }: { title: string; description?: string; action?: ReactNode }) {
  return (
    <div className="py-10 text-center">
      <p className="text-[15px] font-semibold text-ink-700">{title}</p>
      {description && <p className="mt-1 text-sm text-ink-400">{description}</p>}
      {action && <div className="mt-4">{action}</div>}
    </div>
  )
}

// ===== 아이콘 =====

export function SearchIcon({ className = '' }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2} className={className} aria-hidden>
      <circle cx="11" cy="11" r="7" />
      <path d="m20 20-3.5-3.5" strokeLinecap="round" />
    </svg>
  )
}

export function ChevronIcon({ direction, className = '' }: { direction: 'left' | 'right'; className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2.2} className={className} aria-hidden>
      <path
        d={direction === 'left' ? 'm15 18-6-6 6-6' : 'm9 18 6-6-6-6'}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}
