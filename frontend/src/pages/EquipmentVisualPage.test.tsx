// @vitest-environment jsdom

import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { EquipmentVisualPage } from './EquipmentVisualPage'

afterEach(() => {
  cleanup()
})

describe('EquipmentVisualPage', () => {
  it('renders the equipment workspace on the shared visual shell without fetching', () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch')

    render(<EquipmentVisualPage />)

    expect(screen.getByTestId('equipment-visual')).toBeTruthy()
    expect(screen.getByTestId('topbar-identity')).toBeTruthy()
    expect(screen.getByTestId('nav-equipment').getAttribute('aria-current')).toBe('page')
    expect(screen.getByTestId('equipment-panel')).toBeTruthy()
    expect(screen.getByTestId('derived-damage')).toBeTruthy()
    expect(screen.getByTestId('activity-panel')).toBeTruthy()
    expect(screen.getByTestId('chat-panel')).toBeTruthy()
    expect(document.querySelector('.game-shell')).toBeNull()
    expect(fetchSpy).not.toHaveBeenCalled()
    fetchSpy.mockRestore()
  })
})
