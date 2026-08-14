// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter, useLocation } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { fetchCharacter, respecCharacter } from '../api/character'
import { fetchInventory } from '../api/inventory'
import { fetchCurrentLocation } from '../api/world'
import type { CharacterResponse, InventoryResponse } from '../api/types'
import { CharacterSummaryPanel } from './CharacterSummaryPanel'

vi.mock('../api/character', () => ({
  fetchCharacter: vi.fn(),
  allocateAttributes: vi.fn(),
  respecCharacter: vi.fn(),
}))

vi.mock('../api/world', () => ({
  fetchCurrentLocation: vi.fn(),
}))

vi.mock('../api/inventory', () => ({
  fetchInventory: vi.fn(),
}))

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

function characterFixture(overrides: Partial<CharacterResponse> = {}): CharacterResponse {
  return {
    id: 'char-1',
    accountId: 'acc-1',
    name: 'Hero',
    level: 11,
    experience: 8470,
    strength: 5,
    agility: 5,
    endurance: 5,
    perception: 5,
    currentHealth: 165,
    maxHealth: 165,
    currentStamina: 85,
    maxStamina: 85,
    gold: 100,
    unspentAttributePoints: 0,
    currentLocationId: 'loc-1',
    derivedStats: {
      physicalDamage: 14,
      accuracy: 83,
      dodge: 6,
      criticalChance: 7,
      armor: 3,
    },
    progression: {
      level: 11,
      totalExperience: 8470,
      experienceIntoCurrentLevel: 1240,
      experienceRequiredForNextLevel: 2000,
      experienceRemaining: 760,
      progressPercent: 62,
      maxLevel: false,
    },
    createdAt: '2026-08-14T00:00:00Z',
    updatedAt: '2026-08-14T00:00:00Z',
    ...overrides,
  }
}

function emptyInventory(): InventoryResponse {
  return {
    capacity: 40,
    usedSlots: 0,
    items: [],
    equipment: {
      slots: {
        HEAD: null,
        CHEST: null,
        HANDS: null,
        LEGS: null,
        FEET: null,
        MAIN_HAND: null,
        OFF_HAND: null,
        AMULET: null,
        RING: null,
      },
    },
    derivedStats: {
      physicalDamage: 8,
      accuracy: 80,
      dodge: 5,
      criticalChance: 5,
      armor: 0,
    },
  }
}

function locationFixture() {
  return {
    id: 'loc-1',
    code: 'CITY_SQUARE',
    name: 'City Square',
    description: '',
    safety: 'SAFE' as const,
    region: 'city',
    actions: [],
  }
}

function LocationProbe() {
  const location = useLocation()
  return <span data-testid="location-probe">{`${location.search}${location.hash}`}</span>
}

function renderPanel() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <MemoryRouter initialEntries={['/game']}>
      <QueryClientProvider client={queryClient}>
        <LocationProbe />
        <CharacterSummaryPanel />
      </QueryClientProvider>
    </MemoryRouter>,
  )
}

describe('CharacterSummaryPanel', () => {
  it('renders current XP progress from the server', async () => {
    vi.mocked(fetchCharacter).mockResolvedValue(characterFixture())
    vi.mocked(fetchCurrentLocation).mockResolvedValue(locationFixture())
    vi.mocked(fetchInventory).mockResolvedValue(emptyInventory())

    renderPanel()

    expect(await screen.findByTestId('xp-current-required')).toBeTruthy()
    expect(screen.getByTestId('xp-current-required').textContent).toContain('1,240')
    expect(screen.getByTestId('xp-current-required').textContent).toContain('2,000')
    expect(screen.getByTestId('xp-progress-percent').textContent).toBe('62%')
    expect(screen.getByTestId('xp-remaining').textContent).toContain('760')
    expect((screen.getByTestId('xp-progress-bar') as HTMLProgressElement).value).toBe(62)
  })

  it('renders near-level-up progress', async () => {
    vi.mocked(fetchCharacter).mockResolvedValue(
      characterFixture({
        progression: {
          level: 11,
          totalExperience: 9229,
          experienceIntoCurrentLevel: 1999,
          experienceRequiredForNextLevel: 2000,
          experienceRemaining: 1,
          progressPercent: 100,
          maxLevel: false,
        },
      }),
    )
    vi.mocked(fetchCurrentLocation).mockResolvedValue(locationFixture())
    vi.mocked(fetchInventory).mockResolvedValue(emptyInventory())

    renderPanel()

    expect(await screen.findByTestId('xp-current-required')).toBeTruthy()
    expect(screen.getByTestId('xp-current-required').textContent).toContain('1,999')
    expect(screen.getByTestId('xp-remaining').textContent).toContain('1 XP until Level 12')
  })

  it('renders MAX LEVEL without next-level XP', async () => {
    vi.mocked(fetchCharacter).mockResolvedValue(
      characterFixture({
        level: 30,
        experience: 184830,
        progression: {
          level: 30,
          totalExperience: 184830,
          experienceIntoCurrentLevel: 0,
          experienceRequiredForNextLevel: null,
          experienceRemaining: null,
          progressPercent: 100,
          maxLevel: true,
        },
      }),
    )
    vi.mocked(fetchCurrentLocation).mockResolvedValue(locationFixture())
    vi.mocked(fetchInventory).mockResolvedValue(emptyInventory())

    renderPanel()

    expect(await screen.findByText('Level 30 — MAX')).toBeTruthy()
    expect(screen.getByTestId('xp-progress-label').textContent).toBe('MAX LEVEL')
    expect(screen.queryByTestId('xp-remaining')).toBeNull()
    expect(screen.queryByTestId('xp-current-required')).toBeNull()
  })

  it('shows vitals, identity, and advanced stats behind disclosure', async () => {
    vi.mocked(fetchCharacter).mockResolvedValue(characterFixture({ unspentAttributePoints: 2 }))
    vi.mocked(fetchCurrentLocation).mockResolvedValue(locationFixture())
    vi.mocked(fetchInventory).mockResolvedValue(emptyInventory())

    renderPanel()

    expect(await screen.findByTestId('character-summary-name')).toBeTruthy()
    expect(screen.getByTestId('character-summary').querySelector('.portrait img')?.getAttribute('src')).toBe(
      '/character/default-avatar.webp',
    )
    expect(screen.getByText('2 unspent')).toBeTruthy()
    expect(screen.getByLabelText('Allocate Strength')).toBeTruthy()
    expect(screen.getByLabelText('Health 165 of 165')).toBeTruthy()
    expect(screen.getByLabelText('Stamina 85 of 85')).toBeTruthy()
    expect(screen.getByTestId('character-summary-damage').textContent).toBe('14')
    expect(screen.getByTestId('character-summary-armor').textContent).toBe('3')
    const advanced = screen.getByText('Advanced statistics').closest('details') as HTMLDetailsElement
    expect(advanced.open).toBe(false)
    fireEvent.click(screen.getByText('Advanced statistics'))
    expect(advanced.open).toBe(true)
    expect(advanced.textContent).toContain('Accuracy')
  })

  it('lays out home overview as portrait, vitals, and stat chips', async () => {
    vi.mocked(fetchCharacter).mockResolvedValue(characterFixture())
    vi.mocked(fetchCurrentLocation).mockResolvedValue(locationFixture())

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <MemoryRouter>
        <QueryClientProvider client={queryClient}>
          <CharacterSummaryPanel variant="overview" />
        </QueryClientProvider>
      </MemoryRouter>,
    )

    expect(await screen.findByText('Character Overview')).toBeTruthy()
    expect(screen.getByTestId('character-summary-name').textContent).toBe('Hero')
    expect(screen.getByTestId('character-summary-level').textContent).toBe('Level 11')
    expect(screen.getByTestId('character-summary').querySelector('.character-overview-portrait img')?.getAttribute('src')).toBe(
      '/character/default-avatar.webp',
    )
    expect(screen.getByTestId('character-summary-strength').textContent).toBe('5')
    expect(screen.getByTestId('overview-total-xp').textContent).toBe('8,470')
    expect(screen.getByTestId('view-character')).toBeTruthy()
    expect(screen.queryByTestId('character-summary-gold')).toBeNull()
  })

  it('confirms respec in a dialog', async () => {
    vi.mocked(fetchCharacter).mockResolvedValue(characterFixture())
    vi.mocked(fetchCurrentLocation).mockResolvedValue(locationFixture())
    vi.mocked(fetchInventory).mockResolvedValue(emptyInventory())

    renderPanel()

    fireEvent.click(await screen.findByTestId('character-respec'))
    expect(screen.getByText('Respec character?')).toBeTruthy()
    fireEvent.click(screen.getByTestId('character-respec-confirm'))
    expect(respecCharacter).toHaveBeenCalled()
  })

  it('opens inventory filtered to a clicked equipment slot', async () => {
    vi.mocked(fetchCharacter).mockResolvedValue(characterFixture())
    vi.mocked(fetchCurrentLocation).mockResolvedValue(locationFixture())
    vi.mocked(fetchInventory).mockResolvedValue(emptyInventory())

    renderPanel()

    fireEvent.click(await screen.findByRole('button', { name: 'Select Head slot' }))
    expect(screen.getByTestId('location-probe').textContent).toBe('?slot=HEAD#inventory')
  })
})
