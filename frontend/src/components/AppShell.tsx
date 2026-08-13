import { NavLink, Outlet } from 'react-router-dom'

export function AppShell() {
  return (
    <div className="app-shell">
      <header className="app-header">
        <NavLink to="/game" className="brand">
          Greyhaven
        </NavLink>
        <nav className="app-nav" aria-label="Primary">
          <NavLink to="/game">Game</NavLink>
          <NavLink to="/login">Login</NavLink>
          <NavLink to="/register">Register</NavLink>
        </nav>
      </header>
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  )
}
