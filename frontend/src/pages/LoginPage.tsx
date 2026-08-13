import { Link } from 'react-router-dom'

export function LoginPage() {
  return (
    <section className="auth-page">
      <h1>Login</h1>
      <p className="muted">Authentication will be implemented in Task 2.</p>
      <form className="auth-form" onSubmit={(event) => event.preventDefault()}>
        <label>
          Email
          <input type="email" name="email" autoComplete="username" disabled />
        </label>
        <label>
          Password
          <input
            type="password"
            name="password"
            autoComplete="current-password"
            disabled
          />
        </label>
        <button type="submit" disabled>
          Sign in
        </button>
      </form>
      <p>
        Need an account? <Link to="/register">Register</Link>
      </p>
    </section>
  )
}
