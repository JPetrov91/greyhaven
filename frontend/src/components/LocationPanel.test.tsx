// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
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
import { fetchQuests } from '../api/quests'
import type { NearbyCharactersResponse } from '../api/types'
import { fetchSparringBots } from '../api/sparring'
import { fetchNpcs, talkToNpc } from '../api/npcs'

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
  onOpenTalk?: (npcCode?: string) => void
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
        onTalkOpen={extra?.onOpenTalk}
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

    const onOpenTalk = vi.fn()
    renderHero({ onOpenMarket: () => undefined, onOpenTalk })

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
    expect(onOpenTalk).toHaveBeenCalledWith('MILITIA_OFFICER')
    expect(screen.queryByTestId('npc-dialogue')).toBeNull()
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
    expect(screen.queryByTestId('location-quest-action')).toBeNull()
    screen.getByTestId('hero-travel').click()
    expect(onOpenTravel).toHaveBeenCalled()
    vi.mocked(talkToNpc).mockImplementation(async (code) => ({
      code,
      name: code === 'CAPTAIN_VARRO' ? 'Captain Varro' : 'Watch-Sergeant Bren',
      title: code === 'CAPTAIN_VARRO' ? 'Watch captain' : 'Militia officer',
      portraitCode: 'militia-officer',
      text: code === 'CAPTAIN_VARRO' ? 'Keep the road honest.' : 'Greyhaven still opens the gates.',
      merchantCode: null,
      actions: [{ type: 'CLOSE', questCode: null, merchantCode: null, label: 'Not now' }],
    }))
    fireEvent.click(screen.getByTestId('npc-strip-MILITIA_OFFICER'))
    expect(await screen.findByTestId('npc-dialogue')).toBeTruthy()
    expect(screen.getByTestId('npc-dialogue').getAttribute('data-variant')).toBe('dock')
    expect(screen.queryByTestId('nearby-empty')).toBeNull()
    expect(screen.queryByTestId('talk-npc-MILITIA_OFFICER')).toBeNull()
    expect(screen.getByTestId('npc-strip-MILITIA_OFFICER').getAttribute('aria-pressed')).toBe('true')
    expect(await screen.findByTestId('npc-talk-text')).toHaveProperty('textContent', 'Greyhaven still opens the gates.')
    fireEvent.click(screen.getByTestId('npc-strip-CAPTAIN_VARRO'))
    expect(await screen.findByText('Keep the road honest.')).toBeTruthy()
    fireEvent.click(screen.getByRole('button', { name: 'Close talk' }))
    expect(screen.queryByTestId('npc-dialogue')).toBeNull()
    expect(screen.getByTestId('nearby-empty')).toBeTruthy()
    expect(screen.getByTestId('npc-strip-MILITIA_OFFICER').getAttribute('aria-pressed')).toBe('false')
  })

  it('leads to Bren on the first Square before Talk', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue({
      id: 'loc-square',
      code: 'CITY_SQUARE',
      name: 'City Square',
      description: 'Greyhaven’s heart still pretends it is morning.',
      safety: 'SAFE',
      region: 'Greyhaven',
      actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY', 'TALK_NPCS', 'NOTICE_BOARD'],
    })
    vi.mocked(fetchDestinations).mockResolvedValue({ destinations: [] })
    vi.mocked(fetchNearbyCharacters).mockResolvedValue(emptyNearby)
    vi.mocked(fetchQuests).mockResolvedValue({
      quests: [
        {
          code: 'QST_MILITIA_NOTICE',
          name: 'Issued Steel',
          description: 'The watch is thin.',
          category: 'MAIN',
          status: 'ACTIVE',
          recommendedLevel: 1,
          startNpcCode: 'MILITIA_OFFICER',
          startNpcName: 'Watch-Sergeant Bren',
          turnInNpcCode: 'MILITIA_OFFICER',
          turnInNpcName: 'Watch-Sergeant Bren',
          nextQuestCode: null,
          nextQuestName: null,
          repeatable: false,
          tracked: true,
          objectives: [],
          rewards: [],
          unlocks: [],
          kitFamily: null,
        },
      ],
    })
    vi.mocked(fetchNpcs).mockResolvedValue({
      npcs: [
        {
          code: 'MILITIA_OFFICER',
          name: 'Watch-Sergeant Bren',
          title: 'Watch-Sergeant',
          description: 'Posts notices.',
          greeting: 'The watch has work.',
          portraitCode: 'militia-officer',
          locationCode: 'CITY_SQUARE',
          merchantCode: null,
          interactions: ['TALK', 'QUEST'],
          questBadges: ['ACTIVE'],
        },
        {
          code: 'SILENT_BROKER',
          name: 'Seraphin',
          title: 'Silent Broker',
          description: 'Watches.',
          greeting: '…',
          portraitCode: 'broker',
          locationCode: 'CITY_SQUARE',
          merchantCode: null,
          interactions: ['TALK'],
          questBadges: [],
        },
      ],
    })
    const onAimBren = vi.fn()
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={queryClient}>
        <LocationPanel onAimBren={onAimBren} />
      </QueryClientProvider>,
    )
    expect(await screen.findByTestId('square-coach-line')).toHaveProperty(
      'textContent',
      'The watch-sergeant will speak if you talk to him.',
    )
    expect(screen.getByTestId('issued-steel-banner').textContent).toContain('Issued Steel')
    expect(screen.getByTestId('npc-strip-talk-MILITIA_OFFICER').textContent).toBe('Talk')
    expect(screen.getByTestId('npc-strip-MILITIA_OFFICER').className).toContain('npc-strip-card-lead')
    expect(screen.getByTestId('npc-strip-SILENT_BROKER').className).toContain('npc-strip-card-dim')
    fireEvent.click(screen.getByTestId('issued-steel-banner'))
    expect(onAimBren).toHaveBeenCalledTimes(1)
  })

  it('marks Bren for turn-in without starter glow', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue({
      id: 'loc-square',
      code: 'CITY_SQUARE',
      name: 'City Square',
      description: 'Greyhaven’s heart still pretends it is morning.',
      safety: 'SAFE',
      region: 'Greyhaven',
      actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY', 'TALK_NPCS', 'NOTICE_BOARD'],
    })
    vi.mocked(fetchDestinations).mockResolvedValue({ destinations: [] })
    vi.mocked(fetchNearbyCharacters).mockResolvedValue(emptyNearby)
    vi.mocked(fetchQuests).mockResolvedValue({
      quests: [
        {
          code: 'QST_MILITIA_NOTICE',
          name: 'Issued Steel',
          description: 'The watch is thin.',
          category: 'MAIN',
          status: 'READY_TO_TURN_IN',
          recommendedLevel: 1,
          startNpcCode: 'MILITIA_OFFICER',
          startNpcName: 'Watch-Sergeant Bren',
          turnInNpcCode: 'MILITIA_OFFICER',
          turnInNpcName: 'Watch-Sergeant Bren',
          nextQuestCode: null,
          nextQuestName: null,
          repeatable: false,
          tracked: true,
          objectives: [],
          rewards: [],
          unlocks: [],
          kitFamily: 'SWORD',
        },
      ],
    })
    vi.mocked(fetchNpcs).mockResolvedValue({
      npcs: [
        {
          code: 'MILITIA_OFFICER',
          name: 'Watch-Sergeant Bren',
          title: 'Watch-Sergeant',
          description: 'Posts notices.',
          greeting: 'The watch has work.',
          portraitCode: 'militia-officer',
          locationCode: 'CITY_SQUARE',
          merchantCode: null,
          interactions: ['TALK', 'QUEST'],
          questBadges: ['TURN_IN'],
        },
        {
          code: 'CAPTAIN_VARRO',
          name: 'Captain Varro',
          title: 'Caravan captain',
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
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={queryClient}>
        <LocationPanel />
      </QueryClientProvider>,
    )
    expect(await screen.findByTestId('npc-strip-MILITIA_OFFICER')).toBeTruthy()
    expect(screen.getByTestId('npc-strip-MILITIA_OFFICER').className).not.toContain('npc-strip-card-lead')
    expect(screen.queryByTestId('npc-strip-talk-MILITIA_OFFICER')).toBeNull()
    expect(screen.getByTestId('npc-strip-mark-MILITIA_OFFICER').textContent).toBe('?')
    expect(screen.getByTestId('npc-strip-CAPTAIN_VARRO').className).not.toContain('npc-strip-card-dim')
    expect(screen.getByTestId('square-coach-line').textContent).toBe(
      'The watch-sergeant is waiting for your report.',
    )
    expect(screen.getByTestId('issued-steel-banner').textContent).toContain('Report to Bren')
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

  it('glows Travel after kit grant on the Square', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue({
      id: 'loc-square',
      code: 'CITY_SQUARE',
      name: 'City Square',
      description: 'Greyhaven’s heart still pretends it is morning.',
      safety: 'SAFE',
      region: 'Greyhaven',
      actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY', 'TALK_NPCS'],
    })
    vi.mocked(fetchDestinations).mockResolvedValue({
      destinations: [
        { id: 'loc-old', code: 'OLD_TOWN', name: 'Old Town', safety: 'DANGEROUS' },
        { id: 'loc-forest', code: 'FOREST', name: 'Forest', safety: 'DANGEROUS' },
      ],
    })
    vi.mocked(fetchNearbyCharacters).mockResolvedValue(emptyNearby)
    vi.mocked(fetchQuests).mockResolvedValue({
      quests: [
        {
          code: 'QST_MILITIA_NOTICE',
          name: 'Issued Steel',
          description: 'The watch is thin.',
          category: 'MAIN',
          status: 'ACTIVE',
          recommendedLevel: 1,
          startNpcCode: 'MILITIA_OFFICER',
          startNpcName: 'Watch-Sergeant Bren',
          turnInNpcCode: 'MILITIA_OFFICER',
          turnInNpcName: 'Watch-Sergeant Bren',
          nextQuestCode: null,
          nextQuestName: null,
          repeatable: false,
          tracked: true,
          kitFamily: 'SWORD',
          objectives: [
            {
              type: 'TALK_TO_NPC',
              targetCode: 'MILITIA_OFFICER',
              requiredAmount: 1,
              currentAmount: 1,
              completed: true,
              displayText: 'Talk to Watch-Sergeant Bren',
              consumeOnTurnIn: false,
            },
            {
              type: 'VISIT_LOCATION',
              targetCode: 'OLD_TOWN',
              requiredAmount: 1,
              currentAmount: 0,
              completed: false,
              displayText: 'Reach Old Town',
              consumeOnTurnIn: false,
            },
            {
              type: 'SEARCH_LOCATION',
              targetCode: 'OLD_TOWN',
              requiredAmount: 1,
              currentAmount: 0,
              completed: false,
              displayText: 'Search the alleys',
              consumeOnTurnIn: false,
            },
          ],
          rewards: [],
          unlocks: [],
        },
      ],
    })
    vi.mocked(fetchNpcs).mockResolvedValue({ npcs: [] })
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={queryClient}>
        <LocationPanel travelOpen onTravelClose={() => undefined} />
      </QueryClientProvider>,
    )
    expect(await screen.findByTestId('hero-travel')).toBeTruthy()
    expect(screen.getByTestId('hero-travel').className).toContain('location-hero-tile-lead')
    expect(screen.getByTestId('hero-travel').textContent).toContain('Travel — leave this place.')
    expect(screen.getByTestId('issued-steel-banner').textContent).toContain('Travel to Old Town')
    expect(screen.getByTestId('travel-sheet-rule').textContent).toContain('Leave this place')
    expect(screen.getByTestId('destination-offered-OLD_TOWN')).toBeTruthy()
    expect(screen.getByTestId('destination-OLD_TOWN').textContent).toBe('Go')
    expect(screen.getByTestId('destination-FOREST').closest('li')?.className).toContain('destination-row-dim')
  })

  it('pulses Dangerous and glows Search on first Old Town', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue({
      id: 'loc-old',
      code: 'OLD_TOWN',
      name: 'Old Town',
      description: 'The Square’s noise dies in a lane.',
      safety: 'DANGEROUS',
      region: 'Greyhaven',
      actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY', 'SEARCH_ENCOUNTER'],
    })
    vi.mocked(fetchDestinations).mockResolvedValue({ destinations: [] })
    vi.mocked(fetchNearbyCharacters).mockResolvedValue(emptyNearby)
    vi.mocked(fetchQuests).mockResolvedValue({
      quests: [
        {
          code: 'QST_MILITIA_NOTICE',
          name: 'Issued Steel',
          description: 'The watch is thin.',
          category: 'MAIN',
          status: 'ACTIVE',
          recommendedLevel: 1,
          startNpcCode: 'MILITIA_OFFICER',
          startNpcName: 'Watch-Sergeant Bren',
          turnInNpcCode: 'MILITIA_OFFICER',
          turnInNpcName: 'Watch-Sergeant Bren',
          nextQuestCode: null,
          nextQuestName: null,
          repeatable: false,
          tracked: true,
          kitFamily: 'SWORD',
          objectives: [
            {
              type: 'VISIT_LOCATION',
              targetCode: 'OLD_TOWN',
              requiredAmount: 1,
              currentAmount: 1,
              completed: true,
              displayText: 'Reach Old Town',
              consumeOnTurnIn: false,
            },
            {
              type: 'SEARCH_LOCATION',
              targetCode: 'OLD_TOWN',
              requiredAmount: 1,
              currentAmount: 0,
              completed: false,
              displayText: 'Search the alleys',
              consumeOnTurnIn: false,
            },
          ],
          rewards: [],
          unlocks: [],
        },
      ],
    })
    vi.mocked(fetchNpcs).mockResolvedValue({ npcs: [] })
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={queryClient}>
        <LocationPanel onSearchEncounter={() => undefined} />
      </QueryClientProvider>,
    )
    expect(await screen.findByTestId('location-safety')).toBeTruthy()
    expect(screen.getByTestId('location-safety').className).toContain('location-hero-pill-pulse')
    expect(screen.getByTestId('search-encounter-button').className).toContain('location-hero-tile-lead')
    expect(screen.getByTestId('search-encounter-button').textContent).toContain('Search — look here.')
    expect(screen.getByTestId('square-coach-line').textContent).toContain('Dangerous ground')
    expect(screen.queryByTestId('npc-strip')).toBeNull()
  })

  it('pulses SAFE during post-grant Talk', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue({
      id: 'loc-square',
      code: 'CITY_SQUARE',
      name: 'City Square',
      description: 'Greyhaven’s heart still pretends it is morning.',
      safety: 'SAFE',
      region: 'Greyhaven',
      actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY', 'TALK_NPCS'],
    })
    vi.mocked(fetchDestinations).mockResolvedValue({ destinations: [] })
    vi.mocked(fetchNearbyCharacters).mockResolvedValue(emptyNearby)
    vi.mocked(fetchQuests).mockResolvedValue({
      quests: [
        {
          code: 'QST_MILITIA_NOTICE',
          name: 'Issued Steel',
          description: 'The watch is thin.',
          category: 'MAIN',
          status: 'ACTIVE',
          recommendedLevel: 1,
          startNpcCode: 'MILITIA_OFFICER',
          startNpcName: 'Watch-Sergeant Bren',
          turnInNpcCode: 'MILITIA_OFFICER',
          turnInNpcName: 'Watch-Sergeant Bren',
          nextQuestCode: null,
          nextQuestName: null,
          repeatable: false,
          tracked: true,
          kitFamily: 'SWORD',
          objectives: [
            {
              type: 'VISIT_LOCATION',
              targetCode: 'OLD_TOWN',
              requiredAmount: 1,
              currentAmount: 0,
              completed: false,
              displayText: 'Reach Old Town',
              consumeOnTurnIn: false,
            },
          ],
          rewards: [],
          unlocks: [],
        },
      ],
    })
    vi.mocked(fetchNpcs).mockResolvedValue({
      npcs: [
        {
          code: 'MILITIA_OFFICER',
          name: 'Watch-Sergeant Bren',
          title: 'Watch-Sergeant',
          description: 'Posts notices.',
          greeting: 'The watch has work.',
          portraitCode: 'militia-officer',
          locationCode: 'CITY_SQUARE',
          merchantCode: null,
          interactions: ['TALK', 'QUEST'],
          questBadges: ['ACTIVE'],
        },
      ],
    })
    vi.mocked(talkToNpc).mockResolvedValue({
      code: 'MILITIA_OFFICER',
      name: 'Watch-Sergeant Bren',
      title: 'Watch-Sergeant',
      portraitCode: 'militia-officer',
      text: 'The Square is safe enough.',
      merchantCode: null,
      actions: [{ type: 'CLOSE', questCode: null, merchantCode: null, label: 'Close' }],
    })
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={queryClient}>
        <LocationPanel talkOpen talkNpcCode="MILITIA_OFFICER" onTalkClose={() => undefined} />
      </QueryClientProvider>,
    )
    expect(await screen.findByTestId('location-safety')).toBeTruthy()
    expect(screen.getByTestId('location-safety').className).toContain('location-hero-pill-pulse')
    expect(screen.getByTestId('hero-travel').className).not.toContain('location-hero-tile-lead')
  })
})
