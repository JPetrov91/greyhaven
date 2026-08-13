import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'

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
        <label>
          Email
          <input
            type="email"
            name="email"
            data-testid="register-email"
            autoComplete="username"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />
        </label>
        <label>
          Password
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
        </label>
        {error ? (
          <p className="form-error" role="alert" data-testid="register-error">
            {error}
          </p>
        ) : null}
        <button type="submit" disabled={submitting} data-testid="register-submit">
          {submitting ? 'Creating…' : 'Create account'}
        </button>
      </form>
      <p>
        Already registered? <Link to="/login">Login</Link>
      </p>
    </section>
  )
}
