import { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { applyUiMode, persistUiMode, readStoredUiMode, type UiMode } from '../ui/uiMode'

export function AppShell() {
  const { isAuthenticated, me, logout, isLoading } = useAuth()
  const navigate = useNavigate()
  const [uiMode, setUiMode] = useState<UiMode>(() => readStoredUiMode())

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

  return (
    <div className="app-shell">
      <header className="app-header">
        <NavLink to={isAuthenticated ? '/game' : '/login'} className="brand">
          Greyhaven
        </NavLink>
        <nav className="app-nav" aria-label="Primary">
          {isLoading ? null : isAuthenticated ? (
            <>
              {me?.hasCharacter ? (
                <>
                  <NavLink to="/game#character" data-testid="nav-character">
                    Character
                  </NavLink>
                  <NavLink to="/game" data-testid="nav-world" end>
                    World
                  </NavLink>
                  <NavLink to="/game#inventory" data-testid="nav-inventory">
                    Inventory
                  </NavLink>
                  <NavLink to="/game?panel=market" data-testid="nav-market">
                    Market
                  </NavLink>
                </>
              ) : null}
              {!me?.hasCharacter ? (
                <NavLink to="/create-character">Create Character</NavLink>
              ) : null}
              <button
                type="button"
                className="nav-button"
                data-testid="ui-mode-toggle"
                aria-pressed={uiMode === 'compact'}
                onClick={toggleUiMode}
              >
                {uiMode === 'compact' ? 'Normal mode' : 'Office mode'}
              </button>
              <span className="nav-email" data-testid="nav-email">
                {me?.email}
              </span>
              <button
                type="button"
                className="nav-button"
                data-testid="logout-button"
                onClick={handleLogout}
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login">Login</NavLink>
              <NavLink to="/register">Register</NavLink>
              <button
                type="button"
                className="nav-button"
                data-testid="ui-mode-toggle"
                aria-pressed={uiMode === 'compact'}
                onClick={toggleUiMode}
              >
                {uiMode === 'compact' ? 'Normal mode' : 'Office mode'}
              </button>
            </>
          )}
        </nav>
      </header>
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  )
}
