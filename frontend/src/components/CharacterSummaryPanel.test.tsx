// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { fetchCharacter } from '../api/character'
import { fetchCurrentLocation } from '../api/world'
import type { CharacterResponse } from '../api/types'
import { CharacterSummaryPanel } from './CharacterSummaryPanel'

vi.mock('../api/character', () => ({
  fetchCharacter: vi.fn(),
  allocateAttributes: vi.fn(),
  respecCharacter: vi.fn(),
}))

vi.mock('../api/world', () => ({
  fetchCurrentLocation: vi.fn(),
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

function renderPanel() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={queryClient}>
      <CharacterSummaryPanel />
    </QueryClientProvider>,
  )
}

describe('CharacterSummaryPanel', () => {
  it('renders current XP progress from the server', async () => {
    vi.mocked(fetchCharacter).mockResolvedValue(characterFixture())
    vi.mocked(fetchCurrentLocation).mockResolvedValue(locationFixture())

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

    renderPanel()

    expect(await screen.findByText('Level 30 — MAX')).toBeTruthy()
    expect(screen.getByTestId('xp-progress-label').textContent).toBe('MAX LEVEL')
    expect(screen.queryByTestId('xp-remaining')).toBeNull()
    expect(screen.queryByTestId('xp-current-required')).toBeNull()
  })
})
