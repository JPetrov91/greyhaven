// @vitest-environment jsdom

import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { CombatVisualPage } from './CombatVisualPage'

afterEach(() => {
  cleanup()
})

describe('CombatVisualPage', () => {
  it('renders the combat HUD on the visual shell without fetching', () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch')

    render(<CombatVisualPage />)

    expect(screen.getByTestId('combat-visual')).toBeTruthy()
    expect(screen.getByTestId('topbar-identity')).toBeTruthy()
    expect(screen.getByTestId('combat-panel')).toBeTruthy()
    expect(screen.getByTestId('combat-stage')).toBeTruthy()
    expect(screen.getByTestId('combat-monster-name').textContent).toContain('Ashfang Marauder')
    expect(screen.getByTestId('combat-action-QUICK_ATTACK')).toBeTruthy()
    expect(screen.getByTestId('combat-action-USE_POTION').hasAttribute('disabled')).toBe(true)
    expect(screen.getByTestId('combat-action-RETREAT')).toBeTruthy()
    expect(screen.getByTestId('combat-log')).toBeTruthy()
    expect(screen.getByTestId('combat-loot-preview')).toBeTruthy()
    expect(screen.getByTestId('chat-panel')).toBeTruthy()
    expect(screen.queryByTestId('activity-panel')).toBeNull()
    expect(screen.queryByTestId('nav-world')).toBeNull()
    expect(document.querySelector('.game-shell')).toBeNull()
    expect(fetchSpy).not.toHaveBeenCalled()
    fetchSpy.mockRestore()
  })
})
