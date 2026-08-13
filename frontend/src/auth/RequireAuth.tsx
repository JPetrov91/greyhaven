import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from './AuthContext'

export function RequireAuth() {
  const { isAuthenticated, isLoading, me } = useAuth()
  const location = useLocation()

  if (isLoading) {
    return <p className="muted">Checking session…</p>
  }

  if (!isAuthenticated || !me) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  if (!me.hasCharacter && location.pathname !== '/create-character') {
    return <Navigate to="/create-character" replace />
  }

  if (me.hasCharacter && location.pathname === '/create-character') {
    return <Navigate to="/game" replace />
  }

  return <Outlet />
}
