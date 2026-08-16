// @vitest-environment jsdom

import { cleanup, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import App from './App'
import {
  DEV_UI_COMBAT_PATH,
  DEV_UI_EQUIPMENT_PATH,
  DEV_UI_INVENTORY_PATH,
  DEV_UI_LOCATIONS_PATH,
  DEV_UI_MAIN_SHELL_PATH,
  DEV_UI_PATH,
} from './dev/devUi'

vi.mock('./auth/AuthContext', () => ({
  useAuth: () => ({
    isAuthenticated: false,
    me: null,
    logout: vi.fn(),
    isLoading: false,
  }),
}))

afterEach(() => {
  cleanup()
})

describe('App routes', () => {
  it('serves the UI showcase at /dev/ui in development', () => {
    expect(import.meta.env.DEV).toBe(true)

    render(
      <MemoryRouter initialEntries={[DEV_UI_PATH]}>
        <App />
      </MemoryRouter>,
    )

    expect(screen.getByTestId('ui-showcase')).toBeTruthy()
    expect(document.querySelector('.app-header')).toBeNull()
    expect(document.querySelector('.app-shell-showcase')).not.toBeNull()
  })

  it('serves the main-shell visual sandbox at /dev/ui/main-shell in development', () => {
    expect(import.meta.env.DEV).toBe(true)

    render(
      <MemoryRouter initialEntries={[DEV_UI_MAIN_SHELL_PATH]}>
        <App />
      </MemoryRouter>,
    )

    expect(screen.getByTestId('main-shell-visual')).toBeTruthy()
    expect(document.querySelector('.app-header')).toBeNull()
    expect(document.querySelector('.app-main-game')).not.toBeNull()
    expect(document.querySelector('.app-shell-showcase')).toBeNull()
  })

  it('serves equipment and inventory visual sandboxes in development', () => {
    const { unmount } = render(
      <MemoryRouter initialEntries={[DEV_UI_EQUIPMENT_PATH]}>
        <App />
      </MemoryRouter>,
    )
    expect(screen.getByTestId('equipment-visual')).toBeTruthy()
    expect(document.querySelector('.app-main-game')).not.toBeNull()
    unmount()

    render(
      <MemoryRouter initialEntries={[DEV_UI_INVENTORY_PATH]}>
        <App />
      </MemoryRouter>,
    )
    expect(screen.getByTestId('inventory-visual')).toBeTruthy()
    expect(document.querySelector('.app-main-game')).not.toBeNull()
  })

  it('serves locations and combat visual sandboxes in development', () => {
    const { unmount } = render(
      <MemoryRouter initialEntries={[DEV_UI_LOCATIONS_PATH]}>
        <App />
      </MemoryRouter>,
    )
    expect(screen.getByTestId('locations-visual')).toBeTruthy()
    expect(document.querySelector('.app-main-game')).not.toBeNull()
    unmount()

    render(
      <MemoryRouter initialEntries={[DEV_UI_COMBAT_PATH]}>
        <App />
      </MemoryRouter>,
    )
    expect(screen.getByTestId('combat-visual')).toBeTruthy()
    expect(document.querySelector('.app-main-game')).not.toBeNull()
  })
})
