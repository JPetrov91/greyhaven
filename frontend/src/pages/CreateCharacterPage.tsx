import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { createCharacter } from '../api/character'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { Button } from '../ui/Button'
import { ErrorState } from '../ui/ErrorState'
import { Field } from '../ui/Field'

const CHARACTER_NAME_PATTERN = /^[A-Za-z][A-Za-z0-9_-]*$/

export function CreateCharacterPage() {
  const { refreshMe } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)

    const formData = new FormData(event.currentTarget)
    const trimmed = String(formData.get('name') ?? '').trim()
    if (trimmed.length < 3 || trimmed.length > 24) {
      setError('Character name must be between 3 and 24 characters.')
      return
    }
    if (!CHARACTER_NAME_PATTERN.test(trimmed)) {
      setError('Start with a letter. Letters, digits, underscores, and hyphens only.')
      return
    }

    setSubmitting(true)
    try {
      await createCharacter(trimmed)
      await refreshMe()
      navigate('/game', { replace: true })
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message)
      } else {
        setError('Unable to create character.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-landing create-character-screen">
      <div className="auth-landing-bg" aria-hidden="true">
        <img className="auth-landing-bg-atmosphere" src="/auth/greyhaven-login-atmosphere.png" alt="" />
        <img className="auth-landing-bg-hero" src="/auth/greyhaven-login-bg.png" alt="" />
      </div>
      <div className="auth-landing-overlay" aria-hidden="true" />
      <div className="auth-card-wrap create-character-wrap">
        <div className="auth-lock">
          <section className="auth-card create-character-page" data-testid="create-character-page">
            <span className="auth-card-corner auth-card-corner-tl" aria-hidden="true" />
            <span className="auth-card-corner auth-card-corner-tr" aria-hidden="true" />
            <span className="auth-card-corner auth-card-corner-bl" aria-hidden="true" />
            <span className="auth-card-corner auth-card-corner-br" aria-hidden="true" />
            <Link to="/login" className="auth-brand create-character-brand" aria-label="Greyhaven">
              <img className="auth-crest" src="/auth/crest.png" alt="" />
            </Link>
            <h1 className="auth-card-heading">Create Character</h1>
            <p className="muted">Choose a unique name. Your character begins at level 1 with 100 gold.</p>
            <form className="auth-form" onSubmit={handleSubmit} data-testid="create-character-form">
              <Field label="Character name">
                <input
                  type="text"
                  name="name"
                  data-testid="character-name"
                  minLength={3}
                  maxLength={24}
                  title="Start with a letter. Letters, digits, underscores, and hyphens only."
                  required
                />
              </Field>
              {error ? <ErrorState testId="create-character-error">{error}</ErrorState> : null}
              <Button type="submit" disabled={submitting} data-testid="create-character-submit">
                {submitting ? 'Creating…' : 'Enter Greyhaven'}
              </Button>
            </form>
          </section>
        </div>
      </div>
    </div>
  )
}
