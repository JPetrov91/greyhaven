import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { Button } from '../ui/Button'
import { ErrorState } from '../ui/ErrorState'
import { Field } from '../ui/Field'
import { AuthLanding } from './AuthLanding'

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [forgotHint, setForgotHint] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setForgotHint(false)
    const formData = new FormData(event.currentTarget)
    const nextEmail = String(formData.get('email') ?? email).trim()
    const nextPassword = String(formData.get('password') ?? password)
    setSubmitting(true)
    try {
      const me = await login(nextEmail, nextPassword)
      navigate(me.hasCharacter ? '/game' : '/create-character', { replace: true })
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message)
      } else {
        setError('Unable to sign in.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthLanding testId="login-page" heading="Welcome back, adventurer">
      <form className="auth-form" onSubmit={handleSubmit} data-testid="login-form">
        <Field label="Email">
          <span className="auth-input">
            <UserIcon />
            <input
              type="email"
              name="email"
              data-testid="login-email"
              autoComplete="username"
              placeholder="Enter your email."
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
            />
          </span>
        </Field>
        <Field label="Password">
          <span className="auth-input">
            <LockIcon />
            <input
              type={showPassword ? 'text' : 'password'}
              name="password"
              data-testid="login-password"
              autoComplete="current-password"
              placeholder="Enter your password."
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
            <button
              type="button"
              className="auth-password-toggle"
              aria-label={showPassword ? 'Hide password' : 'Show password'}
              onClick={() => setShowPassword((current) => !current)}
            >
              {showPassword ? <EyeOffIcon /> : <EyeIcon />}
            </button>
          </span>
        </Field>
        <div className="auth-forgot-row">
          <button
            type="button"
            className="auth-forgot"
            data-testid="login-forgot-password"
            onClick={() => setForgotHint(true)}
          >
            Forgot password?
          </button>
        </div>
        {forgotHint ? (
          <p className="auth-forgot-note" data-testid="login-forgot-password-note">
            Password recovery is not available in this MVP. Sign in with your registered email.
          </p>
        ) : null}
        {error ? <ErrorState testId="login-error">{error}</ErrorState> : null}
        <Button type="submit" disabled={submitting} data-testid="login-submit">
          {submitting ? 'Signing in…' : 'Log in'}
        </Button>
        <p className="auth-or">Or</p>
        <Link className="auth-cta-secondary" to="/register">
          Create account
        </Link>
      </form>
    </AuthLanding>
  )
}

function UserIcon() {
  return (
    <svg className="auth-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" aria-hidden="true">
      <circle cx="12" cy="8" r="3.2" />
      <path d="M5 19c1.4-3.2 4-5 7-5s5.6 1.8 7 5" />
    </svg>
  )
}

function LockIcon() {
  return (
    <svg className="auth-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" aria-hidden="true">
      <rect x="6" y="11" width="12" height="9" rx="1.5" />
      <path d="M8.5 11V8.5a3.5 3.5 0 0 1 7 0V11" />
    </svg>
  )
}

function EyeIcon() {
  return (
    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="1.7" aria-hidden="true">
      <path d="M3 12s3.5-6 9-6 9 6 9 6-3.5 6-9 6-9-6-9-6Z" />
      <circle cx="12" cy="12" r="2.4" />
    </svg>
  )
}

function EyeOffIcon() {
  return (
    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="1.7" aria-hidden="true">
      <path d="M4 5 20 19" />
      <path d="M10.5 7.2A8.7 8.7 0 0 1 12 7c5.5 0 9 6 9 6a14.5 14.5 0 0 1-3.2 3.6" />
      <path d="M6.6 9.2C4.8 10.5 3 13 3 13s3.5 6 9 6c1.2 0 2.3-.3 3.3-.7" />
    </svg>
  )
}
