// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { LocationPanel } from './LocationPanel'

vi.mock('../api/world', () => ({
  fetchCurrentLocation: vi.fn(),
  fetchDestinations: vi.fn(),
  fetchNearbyCharacters: vi.fn(),
  moveToLocation: vi.fn(),
}))

import { fetchCurrentLocation, fetchDestinations, fetchNearbyCharacters } from '../api/world'

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

function renderHero(onOpenMarket?: () => void) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <LocationPanel variant="hero" onOpenWorld={() => undefined} onOpenMarket={onOpenMarket} />
    </QueryClientProvider>,
  )
}

describe('LocationPanel', () => {
  it('uses generated forest art and mockup hero chrome', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue({
      id: 'loc-forest',
      code: 'FOREST',
      name: 'Forest',
      description: 'Dense woods press close to the road.',
      safety: 'DANGEROUS',
      region: 'Greyhaven',
      actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY', 'SEARCH_ENCOUNTER', 'START_EXPEDITION'],
    })
    vi.mocked(fetchDestinations).mockResolvedValue({ destinations: [] })
    vi.mocked(fetchNearbyCharacters).mockResolvedValue({ characters: [], truncated: false })

    const { container } = renderHero()

    expect(await screen.findByTestId('current-location')).toHaveProperty('textContent', 'Forest')
    expect(screen.getByText('Current location')).toBeTruthy()
    expect(screen.getByRole('heading', { name: 'Greyhaven' })).toBeTruthy()
    const art = container.querySelector('.location-hero-art') as HTMLElement
    expect(art.style.backgroundImage).toContain('/locations/forest.webp')
    expect(screen.getByTestId('hero-world-map').querySelector('svg')).toBeTruthy()
    expect(screen.getByTestId('hero-travel').textContent).toContain('Travel')
    expect(screen.getByTestId('hero-travel').textContent).toContain('Change location')
    expect(screen.queryByTestId('search-encounter-button')).toBeNull()
    expect(screen.getByTestId('location-safety').textContent).toContain('Dangerous')
    expect(screen.getByTestId('location-pvp').textContent).toContain('PvE')
    expect(screen.queryByTestId('location-code')).toBeNull()
    expect(screen.getByTestId('hero-tavern')).toHaveProperty('disabled', true)
    expect(screen.getByTestId('location-weather').textContent).toContain('Damp')
    expect(screen.getByTestId('location-clock').textContent).toContain('Greyhaven time')
  })

  it('shows five hero tiles with live market on city square', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue({
      id: 'loc-square',
      code: 'CITY_SQUARE',
      name: 'City Square',
      description: 'The heart of Greyhaven.',
      safety: 'SAFE',
      region: 'Greyhaven',
      actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY'],
    })
    vi.mocked(fetchDestinations).mockResolvedValue({ destinations: [] })
    vi.mocked(fetchNearbyCharacters).mockResolvedValue({ characters: [], truncated: false })

    renderHero(() => undefined)

    expect(await screen.findByTestId('current-location')).toHaveProperty('textContent', 'City Square')
    expect(screen.getByTestId('location-safety').textContent).toContain('Safe Zone')
    expect(screen.getByTestId('location-pvp').textContent).toContain('No PvP')
    expect(screen.getByTestId('open-market-BROWSE_MARKET')).toHaveProperty('disabled', false)
    expect(screen.getByTestId('hero-tavern')).toHaveProperty('disabled', true)
    expect(screen.getByTestId('hero-notice')).toHaveProperty('disabled', true)
    expect(screen.getByTestId('hero-guild')).toHaveProperty('disabled', true)
    expect(screen.getByTestId('location-weather').textContent).toContain('Cloudy')
  })

  it('hides implied inspect/travel/nearby from Available actions', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue({
      id: 'loc-square',
      code: 'CITY_SQUARE',
      name: 'City Square',
      description: 'The heart of Greyhaven.',
      safety: 'SAFE',
      region: 'Greyhaven',
      actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY'],
    })
    vi.mocked(fetchDestinations).mockResolvedValue({ destinations: [] })
    vi.mocked(fetchNearbyCharacters).mockResolvedValue({ characters: [], truncated: false })

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={queryClient}>
        <LocationPanel />
      </QueryClientProvider>,
    )

    expect(await screen.findByTestId('current-location')).toHaveProperty('textContent', 'City Square')
    expect(screen.queryByTestId('location-actions')).toBeNull()
    expect(screen.queryByText('Available actions')).toBeNull()
    expect(screen.getByTestId('location-code').textContent).toBe('CITY_SQUARE')
  })
})
