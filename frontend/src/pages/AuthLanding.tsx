import { useEffect, useState, type ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { fetchServerHealth } from '../api/health'
import { applyUiMode, persistUiMode, readStoredUiMode, type UiMode } from '../ui/uiMode'

const FEATURES = [
  {
    key: 'progression',
    title: 'Character Progression',
    body: 'Grow attributes, level, and equipment that persist between sessions.',
    art: '/auth/feature-progression.png',
  },
  {
    key: 'combat',
    title: 'Tactical Combat',
    body: 'Choose meaningful actions. The server resolves every blow.',
    art: '/auth/feature-combat.png',
  },
  {
    key: 'pve',
    title: 'PvE',
    body: 'Hunt Greyhaven’s monsters, take loot, and return when you can.',
    art: '/auth/feature-pve.png',
  },
  {
    key: 'economy',
    title: 'Economy',
    body: 'Trade on the player marketplace without watching the listing clock.',
    art: '/auth/feature-economy.png',
  },
  {
    key: 'expeditions',
    title: 'Expeditions',
    body: 'Send your character out. Results wait for you when you come back.',
    art: '/auth/feature-expeditions.png',
  },
] as const

type Props = {
  testId: string
  heading: string
  children: ReactNode
}

export function AuthLanding({ testId, heading, children }: Props) {
  const [online, setOnline] = useState<boolean | null>(null)
  const [uiMode, setUiMode] = useState<UiMode>(() => readStoredUiMode())

  useEffect(() => {
    let cancelled = false
    fetchServerHealth().then((up) => {
      if (!cancelled) {
        setOnline(up)
      }
    })
    return () => {
      cancelled = true
    }
  }, [])

  function toggleUiMode() {
    const next: UiMode = uiMode === 'compact' ? 'normal' : 'compact'
    setUiMode(next)
    persistUiMode(next)
    applyUiMode(next)
  }

  const statusLabel = online == null ? 'Checking' : online ? 'Online' : 'Offline'

  return (
    <section className="auth-landing" data-testid={testId}>
      <div className="auth-landing-bg" aria-hidden="true">
        <img className="auth-landing-bg-atmosphere" src="/auth/greyhaven-login-atmosphere.png" alt="" />
        <img className="auth-landing-bg-hero" src="/auth/greyhaven-login-bg.png" alt="" />
      </div>
      <div className="auth-landing-overlay" aria-hidden="true" />
      <div className="auth-top">
        <aside className="auth-status" data-testid="auth-server-status">
          <div className="auth-status-row">
            <p className="auth-status-label">Server status</p>
            <p
              className={online ? 'auth-status-value auth-status-online' : 'auth-status-value auth-status-offline'}
              data-testid="auth-server-status-value"
            >
              <span className="auth-status-dot" />
              {statusLabel}
            </p>
          </div>
          <p className="auth-status-version">Version 0.0.1</p>
          <button
            type="button"
            className="auth-status-mode"
            data-testid="ui-mode-toggle"
            aria-pressed={uiMode === 'compact'}
            onClick={toggleUiMode}
          >
            {uiMode === 'compact' ? 'Normal mode' : 'Office mode'}
          </button>
        </aside>
      </div>

      <div className="auth-brand-wrap">
        <Link to="/login" className="auth-brand" aria-label="Greyhaven">
          <img className="auth-crest" src="/auth/crest.png" alt="" />
          <div className="auth-brand-copy">
            <img className="auth-wordmark" src="/auth/greyhaven-wordmark.png?v=2" alt="" />
            <p className="auth-brand-tagline">A persistent dark fantasy world. Your legend endures.</p>
          </div>
        </Link>
      </div>

      <div className="auth-card-wrap">
        <div className="auth-lock">
          <div className="auth-card">
            <h1 className="auth-card-heading">
              <span className="auth-flourish" aria-hidden="true" />
              <span className="auth-card-heading-text">{heading}</span>
              <span className="auth-flourish" aria-hidden="true" />
            </h1>
            {children}
          </div>
        </div>
      </div>

      <ul className="auth-features">
        {FEATURES.map((feature) => (
          <li key={feature.key} className="auth-feature">
            <img className="auth-feature-art" src={feature.art} alt="" data-testid="auth-feature-art" />
            <div className="auth-feature-copy">
              <div className="auth-feature-title-row">
                <FeatureIcon name={feature.key} />
                <h3>{feature.title}</h3>
              </div>
              <p>{feature.body}</p>
            </div>
          </li>
        ))}
      </ul>

      <footer className="auth-footer">
        <p>© 2026 Greyhaven</p>
        <p>Play in short sessions. The world continues without you.</p>
      </footer>
    </section>
  )
}

function FeatureIcon({ name }: { name: (typeof FEATURES)[number]['key'] }) {
  const common = {
    className: 'auth-feature-icon',
    viewBox: '0 0 24 24',
    fill: 'none',
    stroke: 'currentColor',
    strokeWidth: 1.6,
    'aria-hidden': true as const,
  }
  if (name === 'progression') {
    return (
      <svg {...common}>
        <path d="M12 3 5 6v6c0 5 3.2 8.2 7 9 3.8-.8 7-4 7-9V6l-7-3Z" />
      </svg>
    )
  }
  if (name === 'combat') {
    return (
      <svg {...common}>
        <path d="M4 20 14 6l3 3-10 11H4v-3Z" />
        <path d="M16 4l4 4" />
        <path d="M8 20h4" />
      </svg>
    )
  }
  if (name === 'pve') {
    return (
      <svg {...common}>
        <path d="M12 4c2 3 6 4 6 8 0 4-2.5 8-6 8s-6-4-6-8c0-4 4-5 6-8Z" />
        <path d="M9 13h.01M15 13h.01" />
      </svg>
    )
  }
  if (name === 'economy') {
    return (
      <svg {...common}>
        <path d="M7 8h10M6 12h12M8 16h8" />
        <path d="M12 4v16" />
        <path d="M5 7h3v10H5zM16 7h3v10h-3z" />
      </svg>
    )
  }
  return (
    <svg {...common}>
      <circle cx="12" cy="12" r="3" />
      <path d="M12 3v3M12 18v3M3 12h3M18 12h3M6 6l2 2M16 16l2 2M18 6l-2 2M8 16l-2 2" />
    </svg>
  )
}
