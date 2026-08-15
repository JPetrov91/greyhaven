// @vitest-environment jsdom

import { cleanup, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { LoginPage } from './LoginPage'

vi.mock('../auth/AuthContext', () => ({
  useAuth: () => ({
    login: vi.fn(),
  }),
}))

afterEach(() => {
  cleanup()
})

describe('LoginPage', () => {
  beforeEach(() => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: true,
        json: async () => ({ status: 'UP' }),
      }),
    )
  })

  it('renders the cinematic login contract used by e2e tests', async () => {
    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>,
    )

    expect(screen.getByTestId('login-page')).toBeTruthy()
    expect(screen.getByTestId('login-form')).toBeTruthy()
    expect(screen.getByTestId('login-email')).toBeTruthy()
    expect(screen.getByTestId('login-password')).toBeTruthy()
    expect(screen.getByTestId('login-forgot-password')).toBeTruthy()
    expect(screen.getByRole('link', { name: 'Greyhaven' })).toBeTruthy()
    expect(screen.getByText('Welcome back, adventurer')).toBeTruthy()
    expect(screen.getByTestId('login-remember-me')).toBeTruthy()
    expect(screen.getByText(/By logging in, you agree/)).toBeTruthy()
    expect(await screen.findByTestId('auth-server-status-value')).toBeTruthy()
    expect(screen.getAllByTestId('auth-feature-art')).toHaveLength(5)
  })
})
