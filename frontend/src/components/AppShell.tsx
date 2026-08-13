import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function AppShell() {
  const { isAuthenticated, me, logout, isLoading } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    try {
      await logout()
    } finally {
      navigate('/login', { replace: true })
    }
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
              {me?.hasCharacter ? <NavLink to="/game">Game</NavLink> : null}
              {!me?.hasCharacter ? (
                <NavLink to="/create-character">Create Character</NavLink>
              ) : null}
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
