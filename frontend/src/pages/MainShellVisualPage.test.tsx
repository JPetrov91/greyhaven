// @vitest-environment jsdom

import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { MainShellVisualPage } from './MainShellVisualPage'

afterEach(() => {
  cleanup()
})

describe('MainShellVisualPage', () => {
  it('renders the full home-shell composition from static fixtures', () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch')

    render(<MainShellVisualPage />)

    expect(screen.getByTestId('main-shell-visual')).toBeTruthy()
    expect(screen.getByTestId('topbar-identity')).toBeTruthy()
    expect(screen.getAllByText('Artino').length).toBeGreaterThan(0)
    expect(screen.getByTestId('nav-home')).toBeTruthy()
    expect(screen.getByTestId('location-panel')).toBeTruthy()
    expect(screen.getByTestId('current-location').textContent).toBe('The Trade District')
    expect(screen.getByTestId('character-summary')).toBeTruthy()
    expect(screen.getByTestId('equipment-overview')).toBeTruthy()
    expect(screen.getByTestId('expedition-overview')).toBeTruthy()
    expect(screen.getByTestId('daily-objectives')).toBeTruthy()
    expect(screen.getByTestId('world-events')).toBeTruthy()
    expect(screen.getByTestId('guild-placeholder')).toBeTruthy()
    expect(screen.getByTestId('activity-panel')).toBeTruthy()
    expect(screen.getByTestId('chat-panel')).toBeTruthy()
    expect(screen.getByTestId('expedition-exp-north')).toBeTruthy()
    expect(screen.getByTestId('expedition-exp-ruins')).toBeTruthy()
    expect(screen.getByTestId('objective-obj-quests')).toBeTruthy()
    expect(screen.getByTestId('event-evt-boss')).toBeTruthy()
    expect(screen.getByText('Iron Vanguard')).toBeTruthy()
    expect(screen.getByTestId('chat-message-chat-1')).toBeTruthy()
    expect(document.querySelector('.game-shell')).toBeNull()
    expect(document.querySelector('.surface-page')).not.toBeNull()
    expect(document.querySelector('.type-display')).not.toBeNull()
    expect(document.querySelector('.type-page-heading')).not.toBeNull()
    expect(fetchSpy).not.toHaveBeenCalled()
    fetchSpy.mockRestore()
  })
})
