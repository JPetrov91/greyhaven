// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { fetchCharacter } from '../api/character'
import { fetchInventory } from '../api/inventory'
import {
  challengeArena,
  fetchArenaOpponents,
  fetchArenaProfile,
  fetchPublicCharacter,
  fetchPvpHistory,
} from '../api/pvp'
import type { CharacterResponse, InventoryResponse } from '../api/types'
import { ArenaPanel } from './ArenaPanel'

vi.mock('../api/character', () => ({
  fetchCharacter: vi.fn(),
}))

vi.mock('../api/inventory', () => ({
  fetchInventory: vi.fn(),
}))

vi.mock('../api/pvp', () => ({
  fetchArenaProfile: vi.fn(),
  fetchArenaOpponents: vi.fn(),
  fetchPvpHistory: vi.fn(),
  fetchPublicCharacter: vi.fn(),
  updateArenaDefense: vi.fn(),
  challengeArena: vi.fn(),
  challengeDuel: vi.fn(),
}))

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

function characterFixture(): CharacterResponse {
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
    arenaRating: 1400,
    arenaMarks: 24,
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

function renderArena() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <MemoryRouter>
      <QueryClientProvider client={queryClient}>
        <ArenaPanel onMatchStarted={() => undefined} />
      </QueryClientProvider>
    </MemoryRouter>,
  )
}

describe('ArenaPanel', () => {
  it('renders the mockup dashboard with live rating, opponents, and defense', async () => {
    vi.mocked(fetchArenaProfile).mockResolvedValue({
      characterId: 'char-1',
      rating: 1400,
      marks: 24,
      preferredActionOptions: ['QUICK_ATTACK', 'HEAVY_ATTACK', 'DEFEND'],
      defense: {
        preferredAction: 'DEFEND',
        preferredTechniqueCode: null,
        healWhenHpPercentBelow: 40,
        defendWhenStaminaPercentBelow: 25,
        finisherWhenEnemyHpPercentBelow: 35,
        finisherTechniqueCode: null,
      },
    })
    vi.mocked(fetchArenaOpponents).mockResolvedValue({
      opponents: [{ id: 'opp-1', name: 'Ashfang', level: 12, rating: 1380 }],
      page: 0,
      size: 20,
      hasMore: false,
    })
    vi.mocked(fetchPvpHistory).mockResolvedValue({
      entries: [
        {
          matchId: 'm1',
          matchKind: 'ARENA',
          opponentName: 'Ashfang',
          opponentId: 'opp-1',
          result: 'WIN',
          ratingDelta: 12,
          marksAwarded: 8,
          createdAt: '2026-08-15T10:00:00Z',
        },
      ],
      page: 0,
      size: 20,
      hasMore: false,
    })
    vi.mocked(fetchCharacter).mockResolvedValue(characterFixture())
    vi.mocked(fetchInventory).mockResolvedValue(emptyInventory())

    renderArena()

    expect(await screen.findByTestId('arena-panel')).toBeTruthy()
    expect(screen.getByTestId('arena-rating').textContent).toContain('Rating 1400')
    expect(screen.getByTestId('arena-rating').textContent).toContain('Marks 24')
    expect(screen.getByText('Gold IV')).toBeTruthy()
    expect(screen.getByTestId('arena-opponents').textContent).toContain('Ashfang')
    expect(screen.getByTestId('arena-defense-form')).toBeTruthy()
    expect(screen.getByTestId('pvp-history').textContent).toContain('WIN')
    expect(screen.getByRole('heading', { name: 'Choose an Opponent' })).toBeTruthy()
    expect(screen.getByRole('heading', { name: 'PvE Arena' })).toBeTruthy()
  })

  it('inspects an opponent and starts a challenge', async () => {
    vi.mocked(fetchArenaProfile).mockResolvedValue({
      characterId: 'char-1',
      rating: 1000,
      marks: 0,
      preferredActionOptions: ['QUICK_ATTACK'],
      defense: {
        preferredAction: 'QUICK_ATTACK',
        preferredTechniqueCode: null,
        healWhenHpPercentBelow: 40,
        defendWhenStaminaPercentBelow: 25,
        finisherWhenEnemyHpPercentBelow: 35,
        finisherTechniqueCode: null,
      },
    })
    vi.mocked(fetchArenaOpponents).mockResolvedValue({
      opponents: [{ id: 'opp-1', name: 'Ashfang', level: 8, rating: 990 }],
      page: 0,
      size: 20,
      hasMore: false,
    })
    vi.mocked(fetchPvpHistory).mockResolvedValue({ entries: [], page: 0, size: 20, hasMore: false })
    vi.mocked(fetchCharacter).mockResolvedValue(characterFixture())
    vi.mocked(fetchInventory).mockResolvedValue(emptyInventory())
    vi.mocked(fetchPublicCharacter).mockResolvedValue({
      id: 'opp-1',
      name: 'Ashfang',
      level: 8,
      strength: 6,
      agility: 4,
      endurance: 5,
      perception: 3,
      arenaRating: 990,
      weaponFamily: 'SWORD',
      weaponMasteryLevel: 2,
      techniqueLoadout: [],
      equipment: [],
    })
    vi.mocked(challengeArena).mockResolvedValue({} as never)

    const onMatchStarted = vi.fn()
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <MemoryRouter>
        <QueryClientProvider client={queryClient}>
          <ArenaPanel onMatchStarted={onMatchStarted} />
        </QueryClientProvider>
      </MemoryRouter>,
    )

    fireEvent.click(await screen.findByRole('button', { name: 'Inspect' }))
    expect((await screen.findByTestId('public-inspect')).textContent).toContain('Ashfang')
    fireEvent.click(screen.getByRole('button', { name: 'Challenge' }))
    await waitFor(() => expect(challengeArena).toHaveBeenCalledWith('opp-1'))
    expect(onMatchStarted).toHaveBeenCalled()
  })

  it('switches to defense setup without losing the form contract', async () => {
    vi.mocked(fetchArenaProfile).mockResolvedValue({
      characterId: 'char-1',
      rating: 1000,
      marks: 0,
      preferredActionOptions: ['QUICK_ATTACK'],
      defense: {
        preferredAction: 'QUICK_ATTACK',
        preferredTechniqueCode: null,
        healWhenHpPercentBelow: 40,
        defendWhenStaminaPercentBelow: 25,
        finisherWhenEnemyHpPercentBelow: 35,
        finisherTechniqueCode: null,
      },
    })
    vi.mocked(fetchArenaOpponents).mockResolvedValue({ opponents: [], page: 0, size: 20, hasMore: false })
    vi.mocked(fetchPvpHistory).mockResolvedValue({ entries: [], page: 0, size: 20, hasMore: false })
    vi.mocked(fetchCharacter).mockResolvedValue(characterFixture())
    vi.mocked(fetchInventory).mockResolvedValue(emptyInventory())

    renderArena()

    expect(await screen.findByTestId('arena-defense-form')).toBeTruthy()
    fireEvent.click(screen.getByTestId('arena-tab-defense'))
    expect(screen.getByRole('heading', { name: 'Defense Setup' })).toBeTruthy()
    expect(screen.getByTestId('arena-defense-form')).toBeTruthy()
    expect(screen.getByRole('button', { name: 'Save defense' })).toBeTruthy()
  })
})
