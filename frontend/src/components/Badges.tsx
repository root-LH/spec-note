import type { ReactNode } from 'react'
import type { RegistrationStatus, SpecStatus } from '../api/types'

export function Badge({ className = 'bg-gray-100 text-gray-600', children }: { className?: string; children: ReactNode }) {
  return (
    <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${className}`}>
      {children}
    </span>
  )
}

const REGISTRATION_STYLE: Record<RegistrationStatus, string> = {
  OPEN: 'bg-green-100 text-green-700',
  BEFORE: 'bg-blue-100 text-blue-700',
  CLOSED: 'bg-gray-100 text-gray-500',
  UNKNOWN: 'bg-gray-100 text-gray-500',
}

export function RegistrationBadge({ status, label }: { status: RegistrationStatus; label: string }) {
  return <Badge className={REGISTRATION_STYLE[status]}>{label}</Badge>
}

const SPEC_STYLE: Record<SpecStatus, string> = {
  ACTIVE: 'bg-green-100 text-green-700',
  EXPIRING_SOON: 'bg-amber-100 text-amber-700',
  EXPIRED: 'bg-red-100 text-red-700',
  PERMANENT: 'bg-indigo-100 text-indigo-700',
}

export function SpecStatusBadge({ status, label }: { status: SpecStatus; label: string }) {
  return <Badge className={SPEC_STYLE[status]}>{label}</Badge>
}
