import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import { authApi } from '../api/endpoints'
import { getErrorMessage } from '../api/client'
import { AuthForm, Field } from '../components/AuthForm'

export default function SignupPage() {
  const { login } = useAuth()
  const navigate = useNavigate()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [nickname, setNickname] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async () => {
    setSubmitting(true)
    setError(null)
    try {
      await authApi.signup({ email, password, nickname })
      await login(email, password) // 가입 후 바로 로그인
      navigate('/', { replace: true })
    } catch (e) {
      setError(getErrorMessage(e))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthForm
      title="회원가입"
      submitLabel="가입하기"
      submitting={submitting}
      error={error}
      onSubmit={handleSubmit}
      footer={
        <>
          이미 계정이 있나요?{' '}
          <Link to="/login" className="text-indigo-600 hover:underline">
            로그인
          </Link>
        </>
      }
    >
      <Field label="이메일" type="email" value={email} onChange={setEmail} autoComplete="email" />
      <Field
        label="비밀번호"
        type="password"
        value={password}
        onChange={setPassword}
        placeholder="8자 이상"
        autoComplete="new-password"
      />
      <Field label="닉네임" value={nickname} onChange={setNickname} placeholder="2~30자" />
    </AuthForm>
  )
}
