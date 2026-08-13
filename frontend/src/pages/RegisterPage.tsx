import { Link } from 'react-router-dom'

export function RegisterPage() {
  return (
    <section className="auth-page">
      <h1>Register</h1>
      <p className="muted">Account creation will be implemented in Task 2.</p>
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
            autoComplete="new-password"
            disabled
          />
        </label>
        <button type="submit" disabled>
          Create account
        </button>
      </form>
      <p>
        Already registered? <Link to="/login">Login</Link>
      </p>
    </section>
  )
}
