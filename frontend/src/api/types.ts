// 백엔드 응답 DTO와 1:1로 맞춘 타입

export type Role = 'USER' | 'ADMIN'
export type ExamCategory = 'NATIONAL' | 'LANGUAGE' | 'PRIVATE'
export type ExamStage = 'WRITTEN' | 'PRACTICAL' | 'SINGLE'
export type RegistrationStatus = 'BEFORE' | 'OPEN' | 'CLOSED' | 'UNKNOWN'
export type SpecStatus = 'PERMANENT' | 'ACTIVE' | 'EXPIRING_SOON' | 'EXPIRED'

/** 날짜는 "2026-12-13" 형식 문자열 */
export type IsoDate = string

export interface ErrorResponse {
  code: string
  message: string
  errors: { field: string; reason: string }[]
}

export interface TokenResponse {
  grantType: string
  accessToken: string
  accessTokenExpiresIn: number
  refreshToken: string
}

export interface Member {
  memberId: number
  email: string
  nickname: string
  role: Role
  createdAt: string
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  hasNext: boolean
}

export interface ExamSummary {
  examId: number
  name: string
  category: ExamCategory
  categoryName: string
  organizer: string | null
  validityMonths: number | null
}

export interface Schedule {
  scheduleId: number
  examId: number
  examName: string
  category: ExamCategory
  roundName: string
  stage: ExamStage
  stageName: string
  regStart: IsoDate | null
  regEnd: IsoDate | null
  examDate: IsoDate | null
  resultDate: IsoDate | null
  registrationStatus: RegistrationStatus
  registrationStatusName: string
}

export interface ExamDetail extends ExamSummary {
  examCode: string | null
  officialUrl: string | null
  upcomingSchedules: Schedule[]
}

export interface Subscription {
  examId: number
  examName: string
  category: ExamCategory
  categoryName: string
  subscribedAt: string
}

export interface Spec {
  specId: number
  examId: number | null
  name: string
  category: ExamCategory | null
  score: string | null
  certNumber: string | null
  acquiredDate: IsoDate
  expireDate: IsoDate | null
  daysUntilExpire: number | null
  status: SpecStatus
  statusName: string
}

export interface SpecRequest {
  examId: number | null
  customName: string | null
  score: string | null
  certNumber: string | null
  acquiredDate: IsoDate
  expireDate: IsoDate | null
}

export interface Dashboard {
  nickname: string
  specSummary: {
    total: number
    permanent: number
    active: number
    expiringSoon: number
    expired: number
  }
  expiringSpecs: Spec[]
  subscriptionCount: number
  upcomingSchedules: Schedule[]
}

export interface NotificationSetting {
  emailEnabled: boolean
  telegramEnabled: boolean
  telegramLinked: boolean
}

export interface NotificationLog {
  logId: number
  type: string
  typeName: string
  targetId: number
  status: 'PENDING' | 'SENT' | 'FAILED'
  sentAt: string | null
  createdAt: string
}
