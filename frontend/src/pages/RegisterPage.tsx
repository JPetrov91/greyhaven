import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { Button } from '../ui/Button'
import { ErrorState } from '../ui/ErrorState'
import { Field } from '../ui/Field'
import { AuthLanding } from './AuthLanding'

export function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    const formData = new FormData(event.currentTarget)
    const nextEmail = String(formData.get('email') ?? email).trim()
    const nextPassword = String(formData.get('password') ?? password)
    setSubmitting(true)
    try {
      await register(nextEmail, nextPassword)
      navigate('/characters', { replace: true })
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message)
      } else {
        setError('Unable to create account.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthLanding testId="register-page" heading="Begin your legend">
      <form className="auth-form" onSubmit={handleSubmit} data-testid="register-form">
        <Field label="Email">
          <span className="auth-input">
            <UserIcon />
            <input
              type="email"
              name="email"
              data-testid="register-email"
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
              type="password"
              name="password"
              data-testid="register-password"
              autoComplete="new-password"
              minLength={8}
              placeholder="Choose a password."
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </span>
        </Field>
        {error ? <ErrorState testId="register-error">{error}</ErrorState> : null}
        <Button type="submit" disabled={submitting} data-testid="register-submit">
          {submitting ? 'Creating…' : 'Create account'}
        </Button>
        <p className="auth-or">Or</p>
        <Link className="auth-cta-secondary" to="/login">
          Log in
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
