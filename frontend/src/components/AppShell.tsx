import { useEffect, useState } from 'react'
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { Button } from '../ui/Button'
import { focusSection } from '../ui/hashFocus'
import { applyUiMode, persistUiMode, readStoredUiMode, type UiMode } from '../ui/uiMode'
import { isGameNavActive, type GameNavItem } from '../ui/gameNav'

export function AppShell() {
  const { isAuthenticated, me, logout, isLoading } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [uiMode, setUiMode] = useState<UiMode>(() => readStoredUiMode())

  useEffect(() => {
    const id = location.hash.replace(/^#/, '')
    if (id !== 'character' && id !== 'inventory' && id !== 'mastery') {
      return
    }
    focusSection(id)
    const retry = window.setTimeout(() => focusSection(id), 120)
    return () => window.clearTimeout(retry)
  }, [location.hash, location.pathname, me?.hasCharacter])

  async function handleLogout() {
    try {
      await logout()
    } finally {
      navigate('/login', { replace: true })
    }
  }

  function navClass(item: GameNavItem) {
    return isGameNavActive(item, location) ? 'active' : undefined
  }

  function toggleUiMode() {
    const next: UiMode = uiMode === 'compact' ? 'normal' : 'compact'
    setUiMode(next)
    persistUiMode(next)
    applyUiMode(next)
  }

  const isAuthLanding = location.pathname === '/login' || location.pathname === '/register'

  return (
    <div className={isAuthLanding ? 'app-shell app-shell-auth' : 'app-shell'}>
      {isAuthLanding ? null : (
        <header className="app-header">
        <NavLink to={isAuthenticated ? '/game' : '/login'} className="brand">
          Greyhaven
        </NavLink>
        <nav className="app-nav" aria-label="Primary">
          {isLoading ? null : isAuthenticated ? (
            <>
              {me?.hasCharacter ? (
                <>
                  <NavLink to="/game#character" data-testid="nav-character" className={() => navClass('character')}>
                    Character
                  </NavLink>
                  <NavLink to="/game" data-testid="nav-world" className={() => navClass('world')} end>
                    World
                  </NavLink>
                  <NavLink to="/game#inventory" data-testid="nav-inventory" className={() => navClass('inventory')}>
                    Inventory
                  </NavLink>
                  <NavLink to="/game#mastery" data-testid="nav-mastery" className={() => navClass('mastery')}>
                    Mastery
                  </NavLink>
                  <NavLink to="/game?panel=market" data-testid="nav-market" className={() => navClass('market')}>
                    Market
                  </NavLink>
                </>
              ) : null}
              {!me?.hasCharacter ? (
                <NavLink to="/create-character">Create Character</NavLink>
              ) : null}
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
              <Button type="button" variant="ghost" data-testid="logout-button" onClick={handleLogout}>
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
      <main className={isAuthLanding ? 'app-main app-main-auth' : 'app-main'}>
        <Outlet />
      </main>
    </div>
  )
}
