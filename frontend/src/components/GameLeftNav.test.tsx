// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { fetchCurrentExpedition } from '../api/expedition'
import { fetchCurrentLocation } from '../api/world'
import { NAV_COLLAPSE_STORAGE_KEY } from '../ui/navCollapse'
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
  localStorage.removeItem(NAV_COLLAPSE_STORAGE_KEY)
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
    expect(screen.getByText('Navigation')).toBeTruthy()
    expect(screen.getByTestId('nav-home').querySelector('img')?.getAttribute('src')).toBe('/icons/nav/home.webp')
    expect(screen.getByTestId('nav-home').getAttribute('aria-current')).toBe('page')
    expect(screen.getByTestId('nav-world').getAttribute('aria-current')).toBeNull()
    expect(screen.getByTestId('nav-character').getAttribute('href')).toContain('character')
    expect(screen.getByTestId('nav-pvp').getAttribute('href')).toContain('pvp')
    expect(screen.getByTestId('nav-crafting').getAttribute('href')).toContain('crafting')
    expect((screen.getByTestId('nav-guild') as HTMLButtonElement).disabled).toBe(true)
    expect((screen.getByTestId('nav-rankings') as HTMLButtonElement).disabled).toBe(true)
    expect(await screen.findByTestId('quick-claim-expedition')).toBeTruthy()
    expect(screen.getByTestId('quick-tavern').getAttribute('href')).toContain('expeditions')
    expect(screen.getByTestId('quick-travel').getAttribute('href')).toContain('travel=1')
    expect(screen.getByTestId('ui-mode-toggle')).toBeTruthy()
  })

  it('marks only Locations as the current page on the world hash', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue({
      id: 'loc-1',
      code: 'CITY_SQUARE',
      name: 'City Square',
      description: 'The square',
      safety: 'SAFE',
      region: 'Greyhaven',
      actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY'],
    })
    vi.mocked(fetchCurrentExpedition).mockResolvedValue(null as never)

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <MemoryRouter initialEntries={['/game#world']}>
        <QueryClientProvider client={queryClient}>
          <GameLeftNav />
        </QueryClientProvider>
      </MemoryRouter>,
    )

    expect(await screen.findByTestId('nav-world')).toBeTruthy()
    expect(screen.getByTestId('nav-world').getAttribute('aria-current')).toBe('page')
    expect(screen.getByTestId('nav-home').getAttribute('aria-current')).toBeNull()
    expect(screen.getByTestId('nav-character').getAttribute('aria-current')).toBeNull()
    expect(screen.queryByTestId('quick-claim-expedition')).toBeNull()
  })

  it('collapses the rail and remembers the choice', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue({
      id: 'loc-1',
      code: 'CITY_SQUARE',
      name: 'City Square',
      description: 'The square',
      safety: 'SAFE',
      region: 'Greyhaven',
      actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY'],
    })
    vi.mocked(fetchCurrentExpedition).mockResolvedValue(null as never)

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    const view = render(
      <MemoryRouter initialEntries={['/game']}>
        <QueryClientProvider client={queryClient}>
          <GameLeftNav />
        </QueryClientProvider>
      </MemoryRouter>,
    )

    expect(await screen.findByTestId('nav-home')).toBeTruthy()
    expect(screen.getByTestId('nav-collapse').getAttribute('aria-pressed')).toBe('false')
    fireEvent.click(screen.getByTestId('nav-collapse'))
    expect(screen.getByLabelText('Primary').className).toContain('is-collapsed')
    expect(screen.getByTestId('nav-collapse').getAttribute('aria-pressed')).toBe('true')
    expect(localStorage.getItem(NAV_COLLAPSE_STORAGE_KEY)).toBe('true')

    view.unmount()
    render(
      <MemoryRouter initialEntries={['/game']}>
        <QueryClientProvider client={queryClient}>
          <GameLeftNav />
        </QueryClientProvider>
      </MemoryRouter>,
    )
    expect(await screen.findByLabelText('Primary')).toBeTruthy()
    expect(screen.getByLabelText('Primary').className).toContain('is-collapsed')
  })
})
