import type { ExamCategory, IsoDate } from '../api/types'

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토']

/** "2026-12-13" → Date (시간대 영향 없이 그 날짜 0시) */
function parseDate(value: IsoDate): Date {
  const [y, m, d] = value.split('-').map(Number)
  return new Date(y, m - 1, d)
}

/** "2026-12-13" → "2026.12.13 (일)" */
export function formatDate(value: IsoDate | null | undefined): string {
  if (!value) return '미정'
  const date = parseDate(value)
  return `${value.replaceAll('-', '.')} (${WEEKDAYS[date.getDay()]})`
}

/** "2026-12-13" → "12.13" */
export function formatShortDate(value: IsoDate | null | undefined): string {
  if (!value) return '미정'
  return value.slice(5).replace('-', '.')
}

/** 오늘부터 그 날짜까지 남은 일수 (지났으면 음수) */
export function daysFromToday(value: IsoDate): number {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return Math.round((parseDate(value).getTime() - today.getTime()) / 86_400_000)
}

export function dDayLabel(days: number): string {
  if (days === 0) return 'D-DAY'
  return days > 0 ? `D-${days}` : `D+${-days}`
}

/** Date → "2026-12-13" */
export function toIsoDate(date: Date): IsoDate {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

export const CATEGORY_LABEL: Record<ExamCategory, string> = {
  NATIONAL: '국가자격',
  LANGUAGE: '어학',
  PRIVATE: '민간자격',
}
