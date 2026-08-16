// @vitest-environment jsdom

import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { InventoryVisualPage } from './InventoryVisualPage'

afterEach(() => {
  cleanup()
})

describe('InventoryVisualPage', () => {
  it('renders the inventory workspace on the shared visual shell without fetching', () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch')

    render(<InventoryVisualPage />)

    expect(screen.getByTestId('inventory-visual')).toBeTruthy()
    expect(screen.getByTestId('topbar-identity')).toBeTruthy()
    expect(screen.getByTestId('nav-inventory').getAttribute('aria-current')).toBe('page')
    expect(screen.getByTestId('inventory-panel')).toBeTruthy()
    expect(screen.getByTestId('inventory-list')).toBeTruthy()
    expect(screen.getByTestId('inventory-item-STEEL_LONGSWORD')).toBeTruthy()
    expect(screen.getByTestId('inventory-capacity').textContent).toContain('/')
    expect(screen.getByTestId('activity-panel')).toBeTruthy()
    expect(screen.getByTestId('chat-panel')).toBeTruthy()
    expect(document.querySelector('.game-shell')).toBeNull()
    expect(fetchSpy).not.toHaveBeenCalled()
    fetchSpy.mockRestore()
  })
})
