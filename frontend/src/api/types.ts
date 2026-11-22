// 백엔드 응답 DTO와 1:1로 맞춘 타입

export type Role = 'USER' | 'ADMIN'
export type ExamCategory = 'NATIONAL' | 'LANGUAGE' | 'PRIVATE'
export type ExamStage = 'WRITTEN' | 'PRACTICAL' | 'SINGLE'
export type ExamSource = 'API' | 'MANUAL'
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
  source: ExamSource
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
  /** 공공데이터로 동기화된 일정 (다음 동기화 때 날짜가 갱신될 수 있음) */
  synced: boolean
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

// ===== 관리자 =====

export interface ExamRequest {
  /** 등록할 때만 사용 (수정 불가) */
  examCode?: string | null
  name: string
  category: ExamCategory
  organizer: string | null
  validityMonths: number | null
  officialUrl: string | null
}

export interface ScheduleDates {
  regStart: IsoDate | null
  regEnd: IsoDate | null
  examDate: IsoDate
  resultDate: IsoDate | null
}

export interface ScheduleRequest extends ScheduleDates {
  roundName: string
  stage: ExamStage
}

export interface ExamSyncResult {
  examsAdded: number
  catalogFailed: boolean
  examCount: number
  created: number
  updated: number
  unchanged: number
  invalid: number
  failedExams: number
}

export interface NotificationRunResult {
  sent: number
  failed: number
  skipped: number
}
