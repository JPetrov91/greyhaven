import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { checkCharacterNameAvailable, createCharacter } from '../api/character'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import {
  AVATARS,
  avatarsFor,
  type AvatarDefinition,
  type CharacterGender,
} from '../character/avatars'
import { CHARACTER_NAME_PATTERN, randomCharacterName } from '../character/nameRandomizer'
import { Button } from '../ui/Button'
import { ErrorState } from '../ui/ErrorState'

const VISIBLE_THUMBS = 3
const NAME_RULES_HINT = 'Letters, digits, and spaces only. No special characters.'

function nameHint(name: string): { valid: boolean; message: string } {
  const trimmed = name.trim()
  if (trimmed.length === 0) {
    return { valid: false, message: '3–24 characters' }
  }
  if (trimmed.length < 3 || trimmed.length > 24) {
    return { valid: false, message: '3–24 characters' }
  }
  if (!CHARACTER_NAME_PATTERN.test(trimmed)) {
    return { valid: false, message: NAME_RULES_HINT }
  }
  return { valid: true, message: 'Name looks valid' }
}

export function CreateCharacterPage() {
  const { refreshMe, logout } = useAuth()
  const navigate = useNavigate()
  const [name, setName] = useState('')
  const [gender, setGender] = useState<CharacterGender>('MALE')
  const [avatarCode, setAvatarCode] = useState('male_unyielding')
  const [carouselStart, setCarouselStart] = useState(0)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [nameAvailable, setNameAvailable] = useState<boolean | null>(null)

  const catalog = useMemo(() => avatarsFor(gender), [gender])
  const selected = catalog.find((avatar) => avatar.code === avatarCode) ?? catalog[0] ?? AVATARS[0]
  const hint = nameHint(name)
  const canEnter = name.trim().length > 0 && !submitting

  useEffect(() => {
    const trimmed = name.trim()
    if (!hint.valid) {
      setNameAvailable(null)
      return
    }
    let cancelled = false
    const timer = window.setTimeout(() => {
      void checkCharacterNameAvailable(trimmed)
        .then((result) => {
          if (!cancelled) {
            setNameAvailable(result.available)
          }
        })
        .catch(() => {
          if (!cancelled) {
            setNameAvailable(null)
          }
        })
    }, 300)
    return () => {
      cancelled = true
      window.clearTimeout(timer)
    }
  }, [name, hint.valid])

  function selectGender(next: CharacterGender) {
    setGender(next)
    const nextCatalog = avatarsFor(next)
    setAvatarCode(nextCatalog[0]?.code ?? (next === 'FEMALE' ? 'female_veiled' : 'male_unyielding'))
    setCarouselStart(0)
  }

  function selectAvatar(avatar: AvatarDefinition) {
    setAvatarCode(avatar.code)
    const index = catalog.findIndex((item) => item.code === avatar.code)
    if (index >= 0) {
      const maxStart = Math.max(0, catalog.length - VISIBLE_THUMBS)
      setCarouselStart(Math.min(Math.max(0, index - 1), maxStart))
    }
  }

  function shiftCarousel(delta: number) {
    const maxStart = Math.max(0, catalog.length - VISIBLE_THUMBS)
    setCarouselStart((current) => Math.min(maxStart, Math.max(0, current + delta)))
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    const trimmed = name.trim()
    const nextHint = nameHint(trimmed)
    if (!nextHint.valid) {
      setError(
        trimmed.length < 3 || trimmed.length > 24
          ? 'Character name must be between 3 and 24 characters.'
          : NAME_RULES_HINT,
      )
      return
    }

    setSubmitting(true)
    try {
      await createCharacter({ name: trimmed, gender, avatarCode: selected.code })
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

  async function handleBack() {
    try {
      await logout()
    } finally {
      navigate('/login', { replace: true })
    }
  }

  const visible = catalog.slice(carouselStart, carouselStart + VISIBLE_THUMBS)
  const statusText =
    hint.valid && nameAvailable === true
      ? 'Name available'
      : hint.valid && nameAvailable === false
        ? 'Name already taken'
        : hint.message

  return (
    <div className="create-character-screen" data-testid="create-character-page">
      <div className="create-character-bg" aria-hidden="true">
        <img className="create-character-bg-atmosphere" src="/auth/greyhaven-login-atmosphere.png" alt="" />
      </div>
      <div className="create-character-overlay" aria-hidden="true" />

      <form className="create-character-layout" onSubmit={handleSubmit} data-testid="create-character-form">
        <header className="create-character-brand-row">
          <Link to="/login" className="create-character-brand" aria-label="Greyhaven">
            <img className="create-character-crest" src="/auth/crest.png" alt="" />
            <div>
              <img className="create-character-wordmark" src="/auth/greyhaven-wordmark.png?v=2" alt="" />
              <p className="create-character-tagline">A persistent dark fantasy world.</p>
            </div>
          </Link>
        </header>

        <section className="create-character-panel create-character-left" aria-label="Identity">
          <div className="create-character-step">
            <h2>1. Name your character</h2>
            <div className="create-character-name-row">
              <input
                type="text"
                name="name"
                data-testid="character-name"
                minLength={3}
                maxLength={24}
                autoComplete="off"
                placeholder="Enter a name"
                title={NAME_RULES_HINT}
                value={name}
                onChange={(event) => {
                  setName(event.target.value)
                  setError(null)
                }}
              />
              <button
                type="button"
                className="create-character-randomize"
                data-testid="character-name-randomize"
                aria-label="Generate a thematic name"
                onClick={() => {
                  setName(randomCharacterName(gender))
                  setError(null)
                }}
              >
                <DieIcon />
              </button>
            </div>
            <p
              className={
                hint.valid && nameAvailable !== false
                  ? 'create-character-name-ok'
                  : 'create-character-name-hint'
              }
              data-testid="create-character-name-status"
            >
              {hint.valid && nameAvailable !== false ? <CheckIcon /> : null}
              <span>{statusText}</span>
            </p>
          </div>

          <div className="create-character-step">
            <h2>2. Choose gender</h2>
            <div className="create-character-genders">
              <button
                type="button"
                className={gender === 'MALE' ? 'is-selected' : undefined}
                data-testid="character-gender-male"
                aria-pressed={gender === 'MALE'}
                onClick={() => selectGender('MALE')}
              >
                <MaleIcon />
                Male
              </button>
              <button
                type="button"
                className={gender === 'FEMALE' ? 'is-selected' : undefined}
                data-testid="character-gender-female"
                aria-pressed={gender === 'FEMALE'}
                onClick={() => selectGender('FEMALE')}
              >
                <FemaleIcon />
                Female
              </button>
            </div>
          </div>
        </section>

        <section className="create-character-center" aria-label="Avatar">
          <h1>Create your character</h1>
          <div className="create-character-portrait-frame">
            <OrnateCorners />
            <div className="create-character-portrait-well">
              <img src={selected.imageUrl} alt="" />
            </div>
          </div>
          <p className="create-character-avatar-title" data-testid="create-character-avatar-title">
            {selected.title}
          </p>
          <div className="create-character-carousel">
            <button
              type="button"
              className="create-character-carousel-arrow"
              data-testid="character-avatar-prev"
              aria-label="Previous avatars"
              disabled={carouselStart === 0}
              onClick={() => shiftCarousel(-1)}
            >
              <ChevronIcon direction="left" />
            </button>
            <ul className="create-character-thumbs">
              {visible.map((avatar) => (
                <li key={avatar.code}>
                  <button
                    type="button"
                    className={avatar.code === selected.code ? 'is-selected' : undefined}
                    data-testid={`character-avatar-${avatar.code}`}
                    aria-pressed={avatar.code === selected.code}
                    onClick={() => selectAvatar(avatar)}
                  >
                    <span className="create-character-thumb-frame">
                      <OrnateCorners compact />
                      <img src={avatar.imageUrl} alt="" />
                    </span>
                    <span>{avatar.title}</span>
                  </button>
                </li>
              ))}
            </ul>
            <button
              type="button"
              className="create-character-carousel-arrow"
              data-testid="character-avatar-next"
              aria-label="Next avatars"
              disabled={carouselStart >= catalog.length - VISIBLE_THUMBS}
              onClick={() => shiftCarousel(1)}
            >
              <ChevronIcon direction="right" />
            </button>
          </div>
          <div className="create-character-dots" aria-hidden="true">
            {catalog.map((avatar, index) => (
              <span key={avatar.code} className={index === catalog.findIndex((item) => item.code === selected.code) ? 'is-active' : undefined} />
            ))}
          </div>
          <p className="create-character-disclaimer">
            Avatar selection is cosmetic only and does not affect gameplay. You can change your
            avatar later from the Account menu.
          </p>
        </section>

        <aside className="create-character-panel create-character-right" aria-label="Summary">
          <h2>Your character</h2>
          <dl className="create-character-summary">
            <div>
              <dt>Name</dt>
              <dd data-testid="create-character-summary-name">{name.trim() || '—'}</dd>
            </div>
            <div>
              <dt>Gender</dt>
              <dd data-testid="create-character-summary-gender">{gender === 'MALE' ? 'Male' : 'Female'}</dd>
            </div>
            <div className="create-character-summary-avatar">
              <dt>Selected avatar</dt>
              <dd>
                <span data-testid="create-character-summary-avatar">{selected.title}</span>
                <span className="create-character-summary-thumb">
                  <img src={selected.imageUrl} alt="" />
                </span>
              </dd>
            </div>
          </dl>
          {error ? <ErrorState testId="create-character-error">{error}</ErrorState> : null}
          <Button type="submit" disabled={!canEnter} data-testid="create-character-submit">
            {submitting ? 'Creating…' : 'Enter Greyhaven'}
          </Button>
          <Button
            type="button"
            variant="secondary"
            data-testid="create-character-back"
            onClick={() => void handleBack()}
          >
            Back
          </Button>
        </aside>
      </form>
    </div>
  )
}

function OrnateCorners({ compact = false }: { compact?: boolean }) {
  return (
    <span className={compact ? 'cc-corners is-compact' : 'cc-corners'} aria-hidden="true">
      <i />
      <i />
      <i />
      <i />
    </span>
  )
}

function DieIcon() {
  return (
    <svg viewBox="0 0 32 32" aria-hidden="true">
      <defs>
        <linearGradient id="cc-die-top" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stopColor="#f6e7b4" />
          <stop offset="55%" stopColor="#d4b05a" />
          <stop offset="100%" stopColor="#8a6428" />
        </linearGradient>
        <linearGradient id="cc-die-left" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor="#c9a24a" />
          <stop offset="100%" stopColor="#5c4218" />
        </linearGradient>
        <linearGradient id="cc-die-right" x1="1" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor="#e6c878" />
          <stop offset="100%" stopColor="#6e4e1c" />
        </linearGradient>
      </defs>
      <path fill="url(#cc-die-top)" d="M16 3.2 28 10 16 16.6 4 10 16 3.2Z" />
      <path fill="url(#cc-die-left)" d="M4 10v11.2L16 28.2V16.6L4 10Z" />
      <path fill="url(#cc-die-right)" d="M28 10v11.2L16 28.2V16.6L28 10Z" />
      <path fill="none" stroke="#f3e2b0" strokeOpacity="0.55" d="M16 3.2 28 10 16 16.6 4 10 16 3.2Z" />
      <circle fill="#2a1c0c" cx="16" cy="10" r="1.15" />
      <circle fill="#2a1c0c" cx="10.2" cy="18.6" r="1" />
      <circle fill="#2a1c0c" cx="13.4" cy="22.4" r="1" />
      <circle fill="#2a1c0c" cx="21.8" cy="17.8" r="1" />
      <circle fill="#2a1c0c" cx="19.2" cy="21.2" r="1" />
      <circle fill="#2a1c0c" cx="22.6" cy="22.8" r="1" />
    </svg>
  )
}

function CheckIcon() {
  return (
    <svg viewBox="0 0 18 18" aria-hidden="true">
      <circle cx="9" cy="9" r="8" fill="#1d3a22" stroke="#8dcc86" />
      <path d="M5 9.2 7.8 12 13 6" fill="none" stroke="#b7e4b2" strokeWidth="1.7" />
    </svg>
  )
}

function MaleIcon() {
  return (
    <svg viewBox="0 0 48 48" aria-hidden="true">
      <defs>
        <linearGradient id="cc-male-metal" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stopColor="#f3e2b0" />
          <stop offset="48%" stopColor="#d4b05a" />
          <stop offset="100%" stopColor="#8d6a2e" />
        </linearGradient>
      </defs>
      <circle cx="24" cy="24" r="18.5" fill="none" stroke="url(#cc-male-metal)" strokeWidth="1.6" />
      <circle cx="24" cy="24" r="15.2" fill="rgba(18,12,8,0.55)" stroke="url(#cc-male-metal)" strokeWidth="0.8" />
      <circle cx="21" cy="27" r="7" fill="none" stroke="url(#cc-male-metal)" strokeWidth="2.1" />
      <path
        d="M26.2 21.6 36 12M29.2 12h7v7"
        fill="none"
        stroke="url(#cc-male-metal)"
        strokeWidth="2.1"
        strokeLinejoin="round"
      />
    </svg>
  )
}

function FemaleIcon() {
  return (
    <svg viewBox="0 0 48 48" aria-hidden="true">
      <defs>
        <linearGradient id="cc-female-metal" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stopColor="#f3e2b0" />
          <stop offset="48%" stopColor="#d4b05a" />
          <stop offset="100%" stopColor="#8d6a2e" />
        </linearGradient>
      </defs>
      <circle cx="24" cy="24" r="18.5" fill="none" stroke="url(#cc-female-metal)" strokeWidth="1.6" />
      <circle cx="24" cy="24" r="15.2" fill="rgba(18,12,8,0.55)" stroke="url(#cc-female-metal)" strokeWidth="0.8" />
      <circle cx="24" cy="20" r="7" fill="none" stroke="url(#cc-female-metal)" strokeWidth="2.1" />
      <path d="M24 27v11M19.2 33.4h9.6" fill="none" stroke="url(#cc-female-metal)" strokeWidth="2.1" />
    </svg>
  )
}

function ChevronIcon({ direction }: { direction: 'left' | 'right' }) {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className={direction === 'left' ? 'is-left' : undefined}>
      <path d="M9 5.5 16 12 9 18.5" />
    </svg>
  )
}
