import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import { getErrorMessage } from '../api/client'
import { AuthForm, Field } from '../components/AuthForm'

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  // 로그인이 필요한 화면에서 넘어왔다면 로그인 후 그 화면으로 돌아감
  const from = (location.state as { from?: string } | null)?.from ?? '/'

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async () => {
    setSubmitting(true)
    setError(null)
    try {
      await login(email, password)
      navigate(from, { replace: true })
    } catch (e) {
      setError(getErrorMessage(e))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthForm
      title="로그인"
      submitLabel="로그인"
      submitting={submitting}
      error={error}
      onSubmit={handleSubmit}
      footer={
        <>
          계정이 없나요?{' '}
          <Link to="/signup" className="text-indigo-600 hover:underline">
            회원가입
          </Link>
        </>
      }
    >
      <Field label="이메일" type="email" value={email} onChange={setEmail} autoComplete="email" />
      <Field label="비밀번호" type="password" value={password} onChange={setPassword} autoComplete="current-password" />
    </AuthForm>
  )
}
