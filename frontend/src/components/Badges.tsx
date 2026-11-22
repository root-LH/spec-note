import type { ReactNode } from 'react'
import type { RegistrationStatus, SpecStatus } from '../api/types'

export function Badge({ className = 'bg-ink-50 text-ink-500', children }: { className?: string; children: ReactNode }) {
  return (
    <span className={`inline-flex items-center rounded-md px-2 py-0.5 text-xs font-semibold ${className}`}>
      {children}
    </span>
  )
}

const REGISTRATION_STYLE: Record<RegistrationStatus, string> = {
  OPEN: 'bg-success-50 text-success-600',
  BEFORE: 'bg-brand-50 text-brand-600',
  CLOSED: 'bg-ink-50 text-ink-400',
  UNKNOWN: 'bg-ink-50 text-ink-400',
}

export function RegistrationBadge({ status, label }: { status: RegistrationStatus; label: string }) {
  return <Badge className={REGISTRATION_STYLE[status]}>{label}</Badge>
}

const SPEC_STYLE: Record<SpecStatus, string> = {
  ACTIVE: 'bg-success-50 text-success-600',
  EXPIRING_SOON: 'bg-warn-50 text-warn-600',
  EXPIRED: 'bg-danger-50 text-danger-500',
  PERMANENT: 'bg-brand-50 text-brand-600',
}

export function SpecStatusBadge({ status, label }: { status: SpecStatus; label: string }) {
  return <Badge className={SPEC_STYLE[status]}>{label}</Badge>
}

/** 남은 일수에 따라 D-day 글자색 (7일 이내 빨강, 30일 이내 주황) */
export function dDayTone(days: number): string {
  if (days < 0 || days <= 7) return 'text-danger-500'
  if (days <= 30) return 'text-warn-600'
  return 'text-brand-500'
}
