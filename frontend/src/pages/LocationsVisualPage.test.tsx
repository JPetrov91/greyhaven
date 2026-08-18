// @vitest-environment jsdom

import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { LocationsVisualPage } from './LocationsVisualPage'

afterEach(() => {
  cleanup()
})

describe('LocationsVisualPage', () => {
  it('renders the locations workspace on the shared visual shell without fetching', () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch')

    render(<LocationsVisualPage />)

    expect(screen.getByTestId('locations-visual')).toBeTruthy()
    expect(screen.getByTestId('topbar-identity')).toBeTruthy()
    expect(screen.getByTestId('nav-world').getAttribute('aria-current')).toBe('page')
    expect(screen.getByTestId('location-panel')).toBeTruthy()
    expect(screen.getByTestId('current-location').textContent).toBe('The Trade District')
    expect(screen.getByTestId('hero-travel')).toBeTruthy()
    expect(screen.getByTestId('npc-strip-EDRIC_VARN')).toBeTruthy()
    expect(screen.getByTestId('npc-strip-MARA_HELDEN')).toBeTruthy()
    expect(screen.queryByTestId('location-actions')).toBeNull()
    expect(screen.queryByTestId('destination-list')).toBeNull()
    expect(screen.getByTestId('nearby-Mira Calden')).toBeTruthy()
    expect(screen.getByTestId('activity-panel')).toBeTruthy()
    expect(screen.getByTestId('chat-panel')).toBeTruthy()
    expect(document.querySelector('.game-shell')).toBeNull()
    expect(fetchSpy).not.toHaveBeenCalled()
    fetchSpy.mockRestore()
  })
})
