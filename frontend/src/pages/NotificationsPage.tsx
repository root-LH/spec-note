import { useState } from 'react'
import { notificationApi } from '../api/endpoints'
import { getErrorMessage } from '../api/client'
import type { NotificationLog } from '../api/types'
import { useFetch } from '../hooks/useFetch'
import { formatDateTime } from '../utils/format'
import Loading from '../components/Loading'
import ErrorMessage from '../components/ErrorMessage'
import { Badge } from '../components/Badges'

const STATUS_STYLE: Record<NotificationLog['status'], { label: string; className: string }> = {
  SENT: { label: '발송 완료', className: 'bg-green-100 text-green-700' },
  PENDING: { label: '대기', className: 'bg-gray-100 text-gray-600' },
  FAILED: { label: '실패 (재시도 예정)', className: 'bg-red-100 text-red-700' },
}

export default function NotificationsPage() {
  const setting = useFetch(() => notificationApi.setting(), [])
  const logs = useFetch(() => notificationApi.logs(), [])

  const [emailOverride, setEmailOverride] = useState<boolean | null>(null)
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState<string | null>(null)

  const emailEnabled = emailOverride ?? setting.data?.emailEnabled ?? true

  const handleToggle = async () => {
    const next = !emailEnabled
    setSaving(true)
    setSaveError(null)
    try {
      const updated = await notificationApi.updateSetting({ emailEnabled: next })
      setEmailOverride(updated.emailEnabled)
    } catch (e) {
      setSaveError(getErrorMessage(e))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <h1 className="text-2xl font-bold">알림</h1>

      <section className="mt-6 bg-white rounded-xl border border-gray-200 p-6">
        <h2 className="font-semibold">알림 설정</h2>
        {setting.loading && <Loading />}
        {setting.error && <ErrorMessage message={setting.error} onRetry={setting.reload} />}

        {setting.data && (
          <div className="mt-4 space-y-4">
            <div className="flex items-center">
              <div>
                <p className="font-medium">이메일 알림</p>
                <p className="text-sm text-gray-500">접수 시작·마감, 시험 전날, 발표일, 스펙 만료 30일·7일 전</p>
              </div>
              {/* 토글 스위치 */}
              <button
                role="switch"
                aria-checked={emailEnabled}
                onClick={handleToggle}
                disabled={saving}
                className={`ml-auto relative inline-flex h-6 w-11 items-center rounded-full transition disabled:opacity-50 ${
                  emailEnabled ? 'bg-indigo-600' : 'bg-gray-300'
                }`}
              >
                <span
                  className={`inline-block h-5 w-5 rounded-full bg-white shadow transition ${
                    emailEnabled ? 'translate-x-5' : 'translate-x-0.5'
                  }`}
                />
              </button>
            </div>

            <div className="flex items-center text-gray-400">
              <div>
                <p className="font-medium">텔레그램 알림</p>
                <p className="text-sm">준비 중이에요</p>
              </div>
            </div>

            {saveError && <ErrorMessage message={saveError} />}
          </div>
        )}
      </section>

      <section className="mt-6 bg-white rounded-xl border border-gray-200 p-6">
        <div className="flex items-center">
          <h2 className="font-semibold">받은 알림</h2>
          <span className="ml-2 text-xs text-gray-400">최근 30건</span>
          <button onClick={logs.reload} className="ml-auto text-sm text-gray-500 hover:text-gray-900">
            새로고침
          </button>
        </div>

        {logs.loading && <Loading />}
        {logs.error && <ErrorMessage message={logs.error} onRetry={logs.reload} />}

        {logs.data && logs.data.length === 0 && (
          <p className="mt-4 text-sm text-gray-500">
            아직 받은 알림이 없어요. 시험을 구독하거나 스펙을 등록하면 일정에 맞춰 알림이 와요.
          </p>
        )}

        {logs.data && logs.data.length > 0 && (
          <ul className="mt-2 divide-y divide-gray-100">
            {logs.data.map((log) => (
              <li key={log.logId} className="py-3 flex items-center gap-3">
                <span className="font-medium">{log.typeName}</span>
                <Badge className={STATUS_STYLE[log.status].className}>{STATUS_STYLE[log.status].label}</Badge>
                <span className="ml-auto text-sm text-gray-500">{formatDateTime(log.sentAt ?? log.createdAt)}</span>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  )
}
