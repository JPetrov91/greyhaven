import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { Button } from '../ui/Button'
import { ErrorState } from '../ui/ErrorState'
import { Field } from '../ui/Field'

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
      navigate('/create-character', { replace: true })
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
    <section className="auth-page" data-testid="register-page">
      <h1>Register</h1>
      <p className="muted">Create an account to begin your journey in Greyhaven.</p>
      <form className="auth-form" onSubmit={handleSubmit} data-testid="register-form">
        <Field label="Email">
          <input
            type="email"
            name="email"
            data-testid="register-email"
            autoComplete="username"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />
        </Field>
        <Field label="Password">
          <input
            type="password"
            name="password"
            data-testid="register-password"
            autoComplete="new-password"
            minLength={8}
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
          />
        </Field>
        {error ? <ErrorState testId="register-error">{error}</ErrorState> : null}
        <Button type="submit" disabled={submitting} data-testid="register-submit">
          {submitting ? 'Creating…' : 'Create account'}
        </Button>
      </form>
      <p>
        Already registered? <Link to="/login">Login</Link>
      </p>
    </section>
  )
}
