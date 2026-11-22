import { api } from './client'
import type {
  Dashboard,
  ExamCategory,
  ExamDetail,
  ExamRequest,
  ExamSummary,
  ExamSyncResult,
  IsoDate,
  Member,
  NotificationLog,
  NotificationRunResult,
  NotificationSetting,
  PageResponse,
  Schedule,
  ScheduleDates,
  ScheduleRequest,
  Spec,
  SpecRequest,
  Subscription,
  TokenResponse,
} from './types'

/** 백엔드 API를 함수로 정리 (화면 코드에서는 URL을 직접 쓰지 않음) */

export const authApi = {
  signup: (body: { email: string; password: string; nickname: string }) =>
    api.post<{ memberId: number }>('/auth/signup', body).then((r) => r.data),
  login: (body: { email: string; password: string }) =>
    api.post<TokenResponse>('/auth/login', body).then((r) => r.data),
  me: () => api.get<Member>('/me').then((r) => r.data),
}

export const examApi = {
  search: (params: { category?: ExamCategory; keyword?: string; page?: number; size?: number }) =>
    api.get<PageResponse<ExamSummary>>('/exams', { params }).then((r) => r.data),
  get: (examId: number) => api.get<ExamDetail>(`/exams/${examId}`).then((r) => r.data),
  schedules: (params: { from: IsoDate; to: IsoDate; category?: ExamCategory }) =>
    api.get<Schedule[]>('/schedules', { params }).then((r) => r.data),
}

export const subscriptionApi = {
  list: () => api.get<Subscription[]>('/me/subscriptions').then((r) => r.data),
  subscribe: (examId: number) => api.post<void>(`/me/subscriptions/${examId}`),
  unsubscribe: (examId: number) => api.delete<void>(`/me/subscriptions/${examId}`),
}

export const specApi = {
  list: () => api.get<Spec[]>('/me/specs').then((r) => r.data),
  create: (body: SpecRequest) => api.post<Spec>('/me/specs', body).then((r) => r.data),
  update: (specId: number, body: SpecRequest) => api.put<Spec>(`/me/specs/${specId}`, body).then((r) => r.data),
  remove: (specId: number) => api.delete<void>(`/me/specs/${specId}`),
}

export const dashboardApi = {
  get: () => api.get<Dashboard>('/me/dashboard').then((r) => r.data),
}

export const notificationApi = {
  setting: () => api.get<NotificationSetting>('/me/notification-settings').then((r) => r.data),
  updateSetting: (body: { emailEnabled: boolean }) =>
    api.put<NotificationSetting>('/me/notification-settings', body).then((r) => r.data),
  logs: () => api.get<NotificationLog[]>('/me/notifications').then((r) => r.data),
}

/** 관리자 전용 (서버에서 ADMIN 권한을 한 번 더 검사함) */
export const adminApi = {
  createExam: (body: ExamRequest) => api.post<{ id: number }>('/admin/exams', body).then((r) => r.data),
  updateExam: (examId: number, body: Omit<ExamRequest, 'examCode'>) => api.put<void>(`/admin/exams/${examId}`, body),

  schedules: (examId: number) => api.get<Schedule[]>(`/admin/exams/${examId}/schedules`).then((r) => r.data),
  addSchedule: (examId: number, body: ScheduleRequest) =>
    api.post<{ id: number }>(`/admin/exams/${examId}/schedules`, body).then((r) => r.data),
  updateSchedule: (scheduleId: number, body: ScheduleDates) =>
    api.put<Schedule>(`/admin/schedules/${scheduleId}`, body).then((r) => r.data),
  deleteSchedule: (scheduleId: number) => api.delete<void>(`/admin/schedules/${scheduleId}`),

  /** 종목 100여 개를 조회해서 1~2분 걸릴 수 있음 */
  syncExams: () => api.post<ExamSyncResult>('/admin/exams/sync').then((r) => r.data),
  runNotifications: (date?: IsoDate) =>
    api
      .post<NotificationRunResult>('/admin/notifications/run', null, { params: date ? { date } : {} })
      .then((r) => r.data),
}
