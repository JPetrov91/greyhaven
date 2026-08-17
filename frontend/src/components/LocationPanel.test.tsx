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
  fetchQuestBoard: vi.fn().mockResolvedValue({ locationCode: 'CITY_SQUARE', quests: [] }),
}))

import { fetchCurrentLocation, fetchDestinations, fetchNearbyCharacters } from '../api/world'
import type { NearbyCharactersResponse } from '../api/types'
import { fetchSparringBots } from '../api/sparring'
import { fetchNpcs } from '../api/npcs'

vi.mock('../api/sparring', () => ({
  fetchSparringBots: vi.fn(),
  startSparringDrill: vi.fn(),
}))

vi.mock('../api/npcs', () => ({
  fetchNpcs: vi.fn().mockResolvedValue({ npcs: [] }),
  talkToNpc: vi.fn(),
}))

vi.mock('../api/quests', () => ({
  fetchQuests: vi.fn().mockResolvedValue({ quests: [] }),
  fetchQuest: vi.fn(),
  acceptQuest: vi.fn(),
  turnInQuest: vi.fn(),
}))

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

const emptyNearby: NearbyCharactersResponse = { characters: [], truncated: false, limit: 50, totalCount: 0 }

function renderHero(extra?: {
  onOpenMarket?: () => void
  onSearchEncounter?: () => void
  onOpenExpedition?: () => void
  onOpenSparring?: () => void
  showYard?: boolean
  noticeOpen?: boolean
  onNoticeOpen?: () => void
  onNoticeClose?: () => void
}) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <LocationPanel
        variant="hero"
        onOpenWorld={() => undefined}
        onOpenMarket={extra?.onOpenMarket}
        onSearchEncounter={extra?.onSearchEncounter}
        onOpenExpedition={extra?.onOpenExpedition}
        onOpenSparring={extra?.onOpenSparring}
        showYard={extra?.showYard}
        noticeOpen={extra?.noticeOpen}
        onNoticeOpen={extra?.onNoticeOpen}
        onNoticeClose={extra?.onNoticeClose}
      />
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
    vi.mocked(fetchNearbyCharacters).mockResolvedValue(emptyNearby)

    const { container } = renderHero({
      onSearchEncounter: () => undefined,
      onOpenExpedition: () => undefined,
    })

    expect(await screen.findByTestId('current-location')).toHaveProperty('textContent', 'Forest')
    expect(screen.getByText('Current location')).toBeTruthy()
    expect(screen.getByRole('heading', { name: 'Greyhaven' })).toBeTruthy()
    const art = container.querySelector('.location-hero-art') as HTMLElement
    expect(art.style.backgroundImage).toContain('/locations/forest.webp')
    expect(screen.getByTestId('hero-world-map').querySelector('img')?.getAttribute('src')).toBe(
      '/icons/env/world-map.webp',
    )
    expect(screen.getByTestId('hero-travel').textContent).toContain('Travel')
    expect(screen.getByTestId('hero-travel').querySelector('img')?.getAttribute('src')).toBe(
      '/icons/actions/travel.webp',
    )
    expect(screen.getByTestId('search-encounter-button').textContent).toContain('Search')
    expect(screen.getByTestId('search-encounter-button').textContent).toContain('Hunt nearby')
    expect(screen.getByTestId('start-expedition-action').textContent).toContain('Expeditions')
    expect(screen.queryByTestId('hero-tavern')).toBeNull()
    expect(screen.queryByTestId('hero-notice')).toBeNull()
    expect(screen.queryByTestId('open-market-BROWSE_MARKET')).toBeNull()
    expect(screen.getByTestId('location-safety').textContent).toContain('Dangerous')
    expect(screen.getByTestId('location-pvp').textContent).toContain('PvE')
    expect(screen.getByTestId('location-weather').textContent).toContain('Damp')
  })

  it('shows five hero tiles with live market on city square', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue({
      id: 'loc-square',
      code: 'CITY_SQUARE',
      name: 'City Square',
      description: 'The heart of Greyhaven.',
      safety: 'SAFE',
      region: 'Greyhaven',
      actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY', 'TALK_NPCS', 'NOTICE_BOARD'],
    })
    vi.mocked(fetchDestinations).mockResolvedValue({ destinations: [] })
    vi.mocked(fetchNearbyCharacters).mockResolvedValue(emptyNearby)

    renderHero({ onOpenMarket: () => undefined })

    expect(await screen.findByTestId('current-location')).toHaveProperty('textContent', 'City Square')
    expect(screen.getByTestId('location-safety').textContent).toContain('Safe Zone')
    expect(screen.getByTestId('location-pvp').textContent).toContain('No PvP')
    expect(screen.getByTestId('open-market-BROWSE_MARKET')).toHaveProperty('disabled', false)
    expect(screen.getByTestId('hero-tavern')).toHaveProperty('disabled', true)
    expect(screen.getByTestId('talk-npcs-action')).toHaveProperty('disabled', false)
    expect(screen.getByTestId('hero-notice')).toHaveProperty('disabled', true)
    expect(screen.getByTestId('hero-guild')).toHaveProperty('disabled', true)
    expect(screen.getByTestId('location-weather').textContent).toContain('Cloudy')
    expect(screen.queryByTestId('npc-dialogue')).toBeNull()
    screen.getByTestId('talk-npcs-action').click()
    expect(await screen.findByTestId('npc-dialogue')).toBeTruthy()
  })

  it('opens a notice-board workspace instead of overlaying home tiles', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue({
      id: 'loc-square',
      code: 'CITY_SQUARE',
      name: 'City Square',
      description: 'The heart of Greyhaven.',
      safety: 'SAFE',
      region: 'Greyhaven',
      actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY', 'TALK_NPCS', 'NOTICE_BOARD'],
    })
    vi.mocked(fetchDestinations).mockResolvedValue({ destinations: [] })
    vi.mocked(fetchNearbyCharacters).mockResolvedValue(emptyNearby)

    const { container } = renderHero({
      onNoticeOpen: () => undefined,
      onNoticeClose: () => undefined,
      noticeOpen: true,
    })

    expect(await screen.findByTestId('notice-board-scene')).toBeTruthy()
    expect(screen.getByTestId('location-panel').getAttribute('data-workspace')).toBe('notice-board')
    expect(screen.getByTestId('notice-board')).toBeTruthy()
    expect(screen.queryByTestId('talk-npcs-action')).toBeNull()
    expect(screen.queryByTestId('hero-travel')).toBeNull()
    const art = container.querySelector('.location-board-art') as HTMLElement
    expect(art.style.backgroundImage).toContain('/locations/notice_board.png')
  })

  it('splits the Locations page into a tall hero, people column, and no hub dump', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue({
      id: 'loc-square',
      code: 'CITY_SQUARE',
      name: 'City Square',
      description: 'The heart of Greyhaven.',
      safety: 'SAFE',
      region: 'Greyhaven',
      actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY', 'TALK_NPCS', 'NOTICE_BOARD'],
    })
    vi.mocked(fetchDestinations).mockResolvedValue({
      destinations: [
        {
          id: 'loc-tavern',
          code: 'TAVERN',
          name: 'Tavern',
          safety: 'SAFE',
        },
      ],
    })
    vi.mocked(fetchNearbyCharacters).mockResolvedValue(emptyNearby)
    vi.mocked(fetchNpcs).mockResolvedValue({
      npcs: [
        {
          code: 'MILITIA_OFFICER',
          name: 'Watch-Sergeant Bren',
          title: 'Militia officer',
          description: 'Posts notices.',
          greeting: 'The watch has work.',
          portraitCode: 'militia-officer',
          locationCode: 'CITY_SQUARE',
          merchantCode: null,
          interactions: ['TALK', 'QUEST'],
          questBadges: ['ACTIVE'],
        },
        {
          code: 'CAPTAIN_VARRO',
          name: 'Captain Varro',
          title: 'Watch captain',
          description: 'Commands the square.',
          greeting: 'Keep moving.',
          portraitCode: 'militia-officer',
          locationCode: 'CITY_SQUARE',
          merchantCode: null,
          interactions: ['TALK'],
          questBadges: [],
        },
      ],
    })

    const onOpenTravel = vi.fn()
    const onNoticeOpen = vi.fn()
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    const { container } = render(
      <QueryClientProvider client={queryClient}>
        <LocationPanel onOpenTravel={onOpenTravel} onNoticeOpen={onNoticeOpen} />
      </QueryClientProvider>,
    )

    expect(await screen.findByTestId('current-location')).toHaveProperty('textContent', 'City Square')
    expect(screen.queryByTestId('location-actions')).toBeNull()
    expect(screen.queryByText('Available actions')).toBeNull()
    expect(screen.queryByTestId('hero-world-map')).toBeNull()
    expect(screen.queryByTestId('hero-tavern')).toBeNull()
    expect(screen.queryByTestId('hero-guild')).toBeNull()
    expect(screen.queryByTestId('destination-list')).toBeNull()
    expect(screen.getByTestId('location-code').textContent).toBe('CITY_SQUARE')
    expect(container.querySelector('.locations-split')).toBeTruthy()
    expect(screen.getByTestId('npc-strip-MILITIA_OFFICER').textContent).toContain('Watch-Sergeant Bren')
    expect(screen.getByTestId('npc-strip-mark-MILITIA_OFFICER').textContent).toBe('…')
    expect(screen.getByTestId('npc-strip-CAPTAIN_VARRO')).toBeTruthy()
    expect(screen.getByTestId('nearby-empty').textContent).toBe('The square is quiet.')
    expect(screen.getByTestId('hero-notice').textContent).toContain('Notice')
    expect(screen.queryByTestId('search-encounter-button')).toBeNull()
    screen.getByTestId('hero-travel').click()
    expect(onOpenTravel).toHaveBeenCalled()
    screen.getByTestId('npc-strip-MILITIA_OFFICER').click()
    expect(await screen.findByTestId('npc-dialogue')).toBeTruthy()
  })

  it('opens the travel sheet on Locations and lists destinations there', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue({
      id: 'loc-square',
      code: 'CITY_SQUARE',
      name: 'City Square',
      description: 'The heart of Greyhaven.',
      safety: 'SAFE',
      region: 'Greyhaven',
      actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY'],
    })
    vi.mocked(fetchDestinations).mockResolvedValue({
      destinations: [
        {
          id: 'loc-forest',
          code: 'FOREST',
          name: 'Forest',
          safety: 'DANGEROUS',
        },
      ],
    })
    vi.mocked(fetchNearbyCharacters).mockResolvedValue(emptyNearby)

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={queryClient}>
        <LocationPanel travelOpen onTravelClose={() => undefined} />
      </QueryClientProvider>,
    )

    expect(await screen.findByTestId('travel-sheet')).toBeTruthy()
    expect(screen.getByTestId('destination-FOREST')).toBeTruthy()
  })

  it('shows Search only where encounters are allowed and hides the NPC strip when empty', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue({
      id: 'loc-forest',
      code: 'FOREST',
      name: 'Forest',
      description: 'Dense woods press close to the road.',
      safety: 'DANGEROUS',
      region: 'Greyhaven',
      actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY', 'SEARCH_ENCOUNTER'],
    })
    vi.mocked(fetchDestinations).mockResolvedValue({ destinations: [] })
    vi.mocked(fetchNearbyCharacters).mockResolvedValue({
      characters: [{ id: 'p1', name: 'Mira', level: 4, avatarCode: 'male_unyielding' }],
      truncated: true,
      limit: 50,
      totalCount: 80,
    })
    vi.mocked(fetchNpcs).mockResolvedValue({ npcs: [] })

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={queryClient}>
        <LocationPanel onSearchEncounter={() => undefined} />
      </QueryClientProvider>,
    )

    expect(await screen.findByTestId('search-encounter-button')).toBeTruthy()
    expect(screen.queryByTestId('npc-strip')).toBeNull()
    expect(screen.queryByTestId('hero-notice')).toBeNull()
    expect(screen.getByTestId('nearby-Mira').textContent).toContain('Mira')
    expect(screen.getByTestId('here-now-count').textContent).toBe('80')
    expect(screen.getByTestId('nearby-truncated').textContent).toContain('Showing the first 50')
  })

  it('offers a Duels action on the yard without replacing the location page', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue({
      id: 'loc-yard',
      code: 'SPARRING_YARD',
      name: 'Sparring Yard',
      description: 'Unranked steel for new fighters.',
      safety: 'SAFE',
      region: 'Greyhaven',
      actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY', 'CHALLENGE_DUEL', 'START_SPARRING_DRILL'],
    })
    vi.mocked(fetchDestinations).mockResolvedValue({ destinations: [] })
    vi.mocked(fetchNearbyCharacters).mockResolvedValue(emptyNearby)
    vi.mocked(fetchSparringBots).mockResolvedValue([{ level: 1, name: 'Green Recruit', code: 'SPARRING_BOT_L01' }])

    const onOpenSparring = vi.fn()
    renderHero({ onOpenSparring })

    expect(await screen.findByTestId('current-location')).toHaveProperty('textContent', 'Sparring Yard')
    expect(screen.getByTestId('enter-sparring-action').textContent).toContain('Duels')
    expect(screen.queryByTestId('sparring-yard-panel')).toBeNull()
    screen.getByTestId('enter-sparring-action').click()
    expect(onOpenSparring).toHaveBeenCalled()
  })

  it('loads duel and drill panels on the location page after opening Duels', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue({
      id: 'loc-yard',
      code: 'SPARRING_YARD',
      name: 'Sparring Yard',
      description: 'Unranked steel for new fighters.',
      safety: 'SAFE',
      region: 'Greyhaven',
      actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY', 'CHALLENGE_DUEL', 'START_SPARRING_DRILL'],
    })
    vi.mocked(fetchDestinations).mockResolvedValue({ destinations: [] })
    vi.mocked(fetchNearbyCharacters).mockResolvedValue(emptyNearby)
    vi.mocked(fetchSparringBots).mockResolvedValue([{ level: 1, name: 'Green Recruit', code: 'SPARRING_BOT_L01' }])

    renderHero({ showYard: true })

    expect(await screen.findByTestId('sparring-yard-panel')).toBeTruthy()
    expect(screen.getByTestId('enter-sparring-action').getAttribute('aria-current')).toBe('true')
    expect(screen.getByTestId('sparring-nearby-empty')).toBeTruthy()
    expect(screen.getByText('Green Recruit')).toBeTruthy()
  })
})
