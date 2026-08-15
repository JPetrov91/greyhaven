import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from './AuthContext'

const ROSTER_PATHS = new Set(['/characters', '/create-character'])

export function RequireAuth() {
  const { isAuthenticated, isLoading, me } = useAuth()
  const location = useLocation()

  if (isLoading) {
    return <p className="muted">Checking session…</p>
  }

  if (!isAuthenticated || !me) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  if (location.pathname === '/create-character') {
    return <Navigate to="/characters" replace />
  }

  if (!me.activeCharacterId && location.pathname === '/game') {
    return <Navigate to="/characters" replace />
  }

  if (location.pathname.startsWith('/game') && !me.activeCharacterId && !ROSTER_PATHS.has(location.pathname)) {
    return <Navigate to="/characters" replace />
  }

  return <Outlet />
}
