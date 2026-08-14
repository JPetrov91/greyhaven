// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { fetchCurrentExpedition } from '../api/expedition'
import { fetchCurrentLocation } from '../api/world'
import { GameLeftNav } from './GameLeftNav'

vi.mock('../api/world', () => ({
  fetchCurrentLocation: vi.fn(),
}))

vi.mock('../api/expedition', () => ({
  fetchCurrentExpedition: vi.fn(),
}))

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

describe('GameLeftNav', () => {
  it('keeps live destinations clickable and future features disabled', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue({
      id: 'loc-1',
      code: 'TAVERN',
      name: 'Tavern',
      description: 'A tavern',
      safety: 'SAFE',
      region: 'Greyhaven',
      actions: ['START_EXPEDITION', 'INSPECT', 'MOVE', 'VIEW_NEARBY'],
    })
    vi.mocked(fetchCurrentExpedition).mockResolvedValue({
      id: 'exp-1',
      expeditionType: 'FOREST_PATROL',
      expeditionName: 'Forest Patrol',
      strategy: 'BALANCED',
      status: 'COMPLETED',
      startedAt: '2026-08-14T10:00:00Z',
      completesAt: '2026-08-14T10:20:00Z',
      claimedAt: null,
      resultReady: true,
      rewards: { xp: 10, gold: 5, injuryDamage: 0, items: [] },
    })

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <MemoryRouter initialEntries={['/game']}>
        <QueryClientProvider client={queryClient}>
          <GameLeftNav />
        </QueryClientProvider>
      </MemoryRouter>,
    )

    expect(await screen.findByTestId('nav-home')).toBeTruthy()
    expect(screen.getByTestId('nav-character').getAttribute('href')).toContain('character')
    expect((screen.getByTestId('nav-guild') as HTMLButtonElement).disabled).toBe(true)
    expect((screen.getByTestId('nav-pvp') as HTMLButtonElement).disabled).toBe(true)
    expect((screen.getByTestId('nav-crafting') as HTMLButtonElement).disabled).toBe(true)
    expect((screen.getByTestId('nav-rankings') as HTMLButtonElement).disabled).toBe(true)
    expect(await screen.findByTestId('quick-claim-expedition')).toBeTruthy()
    expect(screen.getByTestId('quick-tavern').getAttribute('href')).toContain('expeditions')
    expect(screen.getByTestId('ui-mode-toggle')).toBeTruthy()
  })
})
