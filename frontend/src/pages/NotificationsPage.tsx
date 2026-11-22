import { useState } from 'react'
import { notificationApi } from '../api/endpoints'
import { getErrorMessage } from '../api/client'
import type { NotificationLog } from '../api/types'
import { useFetch } from '../hooks/useFetch'
import { formatDateTime } from '../utils/format'
import Loading from '../components/Loading'
import ErrorMessage from '../components/ErrorMessage'
import { Badge } from '../components/Badges'
import { Card, EmptyState, PageHeader, SectionHeader, Toggle } from '../components/ui'

const STATUS_STYLE: Record<NotificationLog['status'], { label: string; className: string }> = {
  SENT: { label: '발송 완료', className: 'bg-success-50 text-success-600' },
  PENDING: { label: '대기', className: 'bg-ink-50 text-ink-500' },
  FAILED: { label: '재시도 예정', className: 'bg-danger-50 text-danger-500' },
}

export default function NotificationsPage() {
  const setting = useFetch(() => notificationApi.setting(), [])
  const logs = useFetch(() => notificationApi.logs(), [])

  const [emailOverride, setEmailOverride] = useState<boolean | null>(null)
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState<string | null>(null)

  const emailEnabled = emailOverride ?? setting.data?.emailEnabled ?? true

  const handleToggle = async () => {
    setSaving(true)
    setSaveError(null)
    try {
      const updated = await notificationApi.updateSetting({ emailEnabled: !emailEnabled })
      setEmailOverride(updated.emailEnabled)
    } catch (e) {
      setSaveError(getErrorMessage(e))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="space-y-4">
      <PageHeader title="알림" description="시험 일정과 스펙 만료일을 놓치지 않도록 알려드려요" />

      <Card>
        <SectionHeader title="알림 설정" />
        {setting.loading && <Loading />}
        {setting.error && <ErrorMessage message={setting.error} onRetry={setting.reload} />}

        {setting.data && (
          <div className="divide-y divide-ink-50">
            <div className="flex items-center gap-4 py-4 first:pt-0">
              <div>
                <p className="text-[16px] font-semibold">이메일 알림</p>
                <p className="mt-0.5 text-sm text-ink-400">접수 시작·마감, 시험 전날, 발표일, 스펙 만료 30일·7일 전</p>
              </div>
              <div className="ml-auto">
                <Toggle checked={emailEnabled} onChange={handleToggle} disabled={saving} />
              </div>
            </div>

            <div className="flex items-center gap-4 py-4 last:pb-0">
              <div>
                <p className="text-[16px] font-semibold text-ink-400">텔레그램 알림</p>
                <p className="mt-0.5 text-sm text-ink-300">준비 중이에요</p>
              </div>
              <div className="ml-auto">
                <Toggle checked={false} onChange={() => {}} disabled />
              </div>
            </div>

            {saveError && (
              <div className="pt-4">
                <ErrorMessage message={saveError} />
              </div>
            )}
          </div>
        )}
      </Card>

      <Card>
        <SectionHeader
          title="받은 알림"
          meta="최근 30건"
          action={
            <button type="button" onClick={logs.reload} className="text-sm font-semibold text-ink-400 hover:text-ink-700">
              새로고침
            </button>
          }
        />

        {logs.loading && <Loading />}
        {logs.error && <ErrorMessage message={logs.error} onRetry={logs.reload} />}

        {logs.data && logs.data.length === 0 && (
          <EmptyState title="아직 받은 알림이 없어요" description="시험을 구독하거나 스펙을 등록하면 일정에 맞춰 알림이 와요" />
        )}

        {logs.data && logs.data.length > 0 && (
          <ul className="space-y-2">
            {logs.data.map((log) => (
              <li key={log.logId} className="flex items-center gap-3 rounded-2xl bg-ink-50 px-4 py-3.5">
                <span className="font-semibold">{log.typeName}</span>
                <Badge className={STATUS_STYLE[log.status].className}>{STATUS_STYLE[log.status].label}</Badge>
                <span className="ml-auto text-sm text-ink-400">{formatDateTime(log.sentAt ?? log.createdAt)}</span>
              </li>
            ))}
          </ul>
        )}
      </Card>
    </div>
  )
}
