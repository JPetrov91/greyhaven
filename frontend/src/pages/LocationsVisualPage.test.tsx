// @vitest-environment jsdom

import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { DEV_UI_COMBAT_PATH } from '../dev/devUi'
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
    expect(screen.getByTestId('destination-list')).toBeTruthy()
    expect(screen.getByTestId('destination-NORTH_ROAD')).toBeTruthy()
    expect(screen.getByTestId('destination-NORTH_ROAD').querySelector('a')?.getAttribute('href')).toBe(DEV_UI_COMBAT_PATH)
    expect(screen.getByTestId('location-actions')).toBeTruthy()
    expect(screen.getByTestId('nearby-Mira Calden')).toBeTruthy()
    expect(screen.getByTestId('activity-panel')).toBeTruthy()
    expect(screen.getByTestId('chat-panel')).toBeTruthy()
    expect(document.querySelector('.game-shell')).toBeNull()
    expect(fetchSpy).not.toHaveBeenCalled()
    fetchSpy.mockRestore()
  })
})
