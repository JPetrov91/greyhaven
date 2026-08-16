// @vitest-environment jsdom

import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it } from 'vitest'
import { MainShellGameTopBar } from './MainShellGameTopBar'
import { MainShellLocationPanel } from './MainShellLocationPanel'

afterEach(() => {
  cleanup()
})

describe('main-shell chrome copies', () => {
  it('renders the production top-bar structure with engine surfaces and fixture data', () => {
    render(<MainShellGameTopBar />)

    expect(document.querySelector('.game-topbar.surface-base')).not.toBeNull()
    expect(document.querySelector('.type-display')).not.toBeNull()
    expect(screen.getByTestId('topbar-identity')).toBeTruthy()
    expect(screen.getByTestId('topbar-silver').textContent).toContain('1,250,764')
    expect(screen.getByTestId('topbar-gold').textContent).toContain('4,320')
    expect(screen.getByTestId('topbar-inventory')).toBeTruthy()
  })

  it('renders the production location-hero structure with engine type roles', () => {
    render(<MainShellLocationPanel />)

    expect(document.querySelector('.location-hero.surface-base')).not.toBeNull()
    expect(screen.getByTestId('current-location').textContent).toBe('The Trade District')
    expect(screen.getByTestId('location-description').className).toContain('type-body')
    expect(screen.getByTestId('hero-travel')).toBeTruthy()
    expect(screen.getByTestId('hero-tavern')).toBeTruthy()
    expect(screen.getByTestId('open-market-BROWSE_MARKET')).toBeTruthy()
    expect(screen.getByTestId('open-chat-action')).toBeTruthy()
    expect(document.querySelectorAll('.location-hero-tile')).toHaveLength(5)
  })
})
