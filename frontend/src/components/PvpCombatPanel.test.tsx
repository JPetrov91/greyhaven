// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { fetchCharacter } from '../api/character'
import type { PvpMatchResponse } from '../api/pvp'
import { PvpCombatPanel } from './PvpCombatPanel'

vi.mock('../api/character', () => ({
  fetchCharacter: vi.fn(),
}))

vi.mock('../api/pvp', async () => {
  const actual = await vi.importActual<typeof import('../api/pvp')>('../api/pvp')
  return {
    ...actual,
    submitArenaAction: vi.fn(),
    submitDuelAction: vi.fn(),
    acknowledgeArenaMatch: vi.fn(),
  }
})

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

function matchFixture(overrides: Partial<PvpMatchResponse> = {}): PvpMatchResponse {
  return {
    id: 'match-1',
    matchKind: 'ARENA',
    status: 'ACTIVE',
    roundNumber: 1,
    attackerName: 'Hero',
    defenderName: 'Rival',
    attackerId: 'char-1',
    defenderId: 'char-2',
    attackerHealth: 120,
    attackerMaxHealth: 165,
    attackerStamina: 5,
    attackerMaxStamina: 85,
    defenderHealth: 140,
    defenderMaxHealth: 165,
    defenderStamina: 40,
    defenderMaxStamina: 85,
    potionAvailable: false,
    events: [],
    defenderIntent: { kind: 'BASIC_ATTACK', label: 'Basic Attack' },
    actionPreviews: [
      {
        action: 'QUICK_ATTACK',
        techniqueCode: null,
        name: 'Quick Attack',
        description: 'A reliable strike.',
        staminaCost: 8,
        hitChancePercent: 72,
        disabledReason: 'INSUFFICIENT_STAMINA',
      },
      {
        action: 'DEFEND',
        techniqueCode: null,
        name: 'Defend',
        description: 'Guard yourself.',
        staminaCost: 0,
        hitChancePercent: null,
        disabledReason: null,
      },
      {
        action: 'USE_TECHNIQUE',
        techniqueCode: 'SWORD_CLEAVE',
        name: 'Cleave',
        description: 'A heavy slash.',
        staminaCost: 14,
        hitChancePercent: 60,
        disabledReason: 'INSUFFICIENT_STAMINA',
      },
    ],
    techniques: [],
    settlement: null,
    waitingForOpponent: false,
    yourPendingAction: null,
    outcomeAcknowledged: false,
    attackerStatuses: [],
    defenderStatuses: [],
    ...overrides,
  }
}

function renderPanel(match: PvpMatchResponse) {
  vi.mocked(fetchCharacter).mockResolvedValue({ id: 'char-1', name: 'Hero' } as never)
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={queryClient}>
      <PvpCombatPanel match={match} onUpdate={() => {}} />
    </QueryClientProvider>,
  )
}

describe('PvpCombatPanel', () => {
  it('renders server action previews instead of a hardcoded action list', () => {
    renderPanel(matchFixture())

    expect(screen.getByTestId('pvp-action-QUICK_ATTACK').textContent).toContain('Quick Attack')
    expect(screen.getByTestId('pvp-action-QUICK_ATTACK').textContent).toContain('72%')
    expect((screen.getByTestId('pvp-action-QUICK_ATTACK') as HTMLButtonElement).disabled).toBe(true)
    expect((screen.getByTestId('pvp-action-DEFEND') as HTMLButtonElement).disabled).toBe(false)
    expect(screen.getByTestId('pvp-action-SWORD_CLEAVE').textContent).toContain('Cleave')
    expect(screen.queryByRole('button', { name: 'HEAVY_ATTACK' })).toBeNull()
  })
})
