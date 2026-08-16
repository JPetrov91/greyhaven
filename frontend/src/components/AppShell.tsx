import { useEffect, useState } from 'react'
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { Button } from '../ui/Button'
import { focusSection } from '../ui/hashFocus'
import { isDevUiPath, isDevUiShowcasePath, isDevUiVisualShellPath } from '../dev/devUi'
import { applyUiMode, persistUiMode, readStoredUiMode, type UiMode } from '../ui/uiMode'

export function AppShell() {
  const { isAuthenticated, me, logout, isLoading } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [uiMode, setUiMode] = useState<UiMode>(() => readStoredUiMode())

  const gameChrome = Boolean(isAuthenticated && me?.activeCharacterId && location.pathname.startsWith('/game'))

  useEffect(() => {
    const id = location.hash.replace(/^#/, '')
    if (
      id !== 'character' &&
      id !== 'inventory' &&
      id !== 'equipment' &&
      id !== 'mastery' &&
      id !== 'world' &&
      id !== 'expeditions' &&
      id !== 'crafting' &&
      id !== 'pvp' &&
      id !== 'sparring' &&
      id !== 'market'
    ) {
      return
    }
    focusSection(id)
    const retry = window.setTimeout(() => focusSection(id), 120)
    return () => window.clearTimeout(retry)
  }, [location.hash, location.pathname, me?.activeCharacterId])

  async function handleLogout() {
    try {
      await logout()
    } finally {
      navigate('/login', { replace: true })
    }
  }

  function toggleUiMode() {
    const next: UiMode = uiMode === 'compact' ? 'normal' : 'compact'
    setUiMode(next)
    persistUiMode(next)
    applyUiMode(next)
  }

  const isAuthLanding =
    location.pathname === '/login' ||
    location.pathname === '/register' ||
    location.pathname === '/create-character' ||
    location.pathname === '/characters'
  const isUiShowcase = isDevUiShowcasePath(location.pathname)
  const isVisualShell = isDevUiVisualShellPath(location.pathname)
  const hideChrome = isAuthLanding || gameChrome || isDevUiPath(location.pathname)
  const shellClass = isAuthLanding
    ? 'app-shell app-shell-auth'
    : isUiShowcase
      ? 'app-shell app-shell-showcase'
      : 'app-shell'
  const mainClass = isAuthLanding
    ? 'app-main app-main-auth'
    : gameChrome || isVisualShell
      ? 'app-main app-main-game'
      : isUiShowcase
        ? 'app-main app-main-showcase'
        : 'app-main'

  return (
    <div className={shellClass}>
      {hideChrome ? null : (
        <header className="app-header surface-raised">
          <NavLink to={isAuthenticated ? '/game' : '/login'} className="brand type-display">
            Greyhaven
          </NavLink>
          <nav className="app-nav" aria-label="Primary">
            {isLoading ? null : isAuthenticated ? (
              <>
                {!me?.activeCharacterId ? <NavLink to="/characters">Characters</NavLink> : null}
                <Button
                  type="button"
                  variant="ghost"
                  data-testid="ui-mode-toggle"
                  aria-pressed={uiMode === 'compact'}
                  onClick={toggleUiMode}
                >
                  {uiMode === 'compact' ? 'Normal mode' : 'Office mode'}
                </Button>
                <span className="nav-email" data-testid="nav-email">
                  {me?.email}
                </span>
                <Button type="button" variant="ghost" data-testid="logout-button" onClick={() => void handleLogout()}>
                  Logout
                </Button>
              </>
            ) : (
              <>
                <NavLink to="/login">Login</NavLink>
                <NavLink to="/register">Register</NavLink>
                <Button
                  type="button"
                  variant="ghost"
                  data-testid="ui-mode-toggle"
                  aria-pressed={uiMode === 'compact'}
                  onClick={toggleUiMode}
                >
                  {uiMode === 'compact' ? 'Normal mode' : 'Office mode'}
                </Button>
              </>
            )}
          </nav>
        </header>
      )}
      <main className={mainClass}>
        <Outlet />
      </main>
    </div>
  )
}
