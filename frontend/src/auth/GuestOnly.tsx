import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from './AuthContext'

export function GuestOnly() {
  const { isAuthenticated, isLoading, me } = useAuth()

  if (isLoading) {
    return <p className="muted">Checking session…</p>
  }

  if (isAuthenticated && me) {
    return <Navigate to="/characters" replace />
  }

  return <Outlet />
}
