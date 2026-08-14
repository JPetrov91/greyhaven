// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { fetchCharacter } from '../api/character'
import { fetchCurrentLocation } from '../api/world'
import type { CombatResponse } from '../api/types'
import { CombatPanel } from './CombatPanel'

vi.mock('../api/combat', () => ({
  submitCombatAction: vi.fn(),
  acknowledgeCombat: vi.fn(),
}))

vi.mock('../api/character', () => ({
  fetchCharacter: vi.fn(),
}))

vi.mock('../api/world', () => ({
  fetchCurrentLocation: vi.fn(),
}))

vi.mock('./ChatPanel', () => ({
  ChatPanel: () => <section data-testid="chat-panel">chat</section>,
}))

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

function combatFixture(overrides: Partial<CombatResponse> = {}): CombatResponse {
  return {
    id: 'combat-1',
    encounterId: 'enc-1',
    status: 'PLAYER_WON',
    rulesVersion: 2,
    roundNumber: 1,
    playerHealth: 120,
    playerMaxHealth: 165,
    playerStamina: 40,
    playerMaxStamina: 85,
    enemyHealth: 0,
    enemyMaxHealth: 70,
    enemyStamina: 0,
    enemyMaxStamina: 40,
    monster: {
      id: 'mon-1',
      code: 'STREET_THUG',
      name: 'Street Thug',
      level: 1,
      maxHealth: 70,
      archetype: 'AGGRESSIVE',
    },
    potionAvailable: false,
    playerStunned: false,
    playerStatuses: [],
    enemyStatuses: [],
    techniques: [],
    coreActionCosts: { quickAttack: 8, heavyAttack: 18, preciseAttack: 12 },
    events: [],
    rewards: {
      xp: 40,
      gold: 8,
      previousLevel: 1,
      newLevel: 2,
      attributePointsGained: 2,
      items: [],
    },
    ...overrides,
  }
}

function renderPanel(combat: CombatResponse) {
  vi.mocked(fetchCharacter).mockResolvedValue({
    name: 'Hero',
    level: 11,
    derivedStats: { physicalDamage: 14, accuracy: 83, dodge: 6, criticalChance: 7, armor: 3 },
  } as never)
  vi.mocked(fetchCurrentLocation).mockResolvedValue({
    id: 'loc-1',
    code: 'FOREST',
    name: 'Whispering Forest',
    description: 'Damp woods.',
    safety: 'DANGEROUS',
    region: 'Greywood',
    actions: [],
  } as never)
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={queryClient}>
      <CombatPanel combat={combat} onCombatUpdate={() => {}} />
    </QueryClientProvider>,
  )
}

describe('CombatPanel', () => {
  it('shows a level-up from a single XP reward', () => {
    renderPanel(combatFixture())

    expect(screen.getByTestId('combat-level-up').textContent).toContain('LEVEL UP')
    expect(screen.getByTestId('combat-level-up').textContent).toContain('Level 1 → 2')
    expect(screen.getByTestId('combat-level-up').textContent).toContain('+2 Attribute Points')
  })

  it('shows a multi-level jump from one reward', () => {
    renderPanel(
      combatFixture({
        rewards: {
          xp: 500,
          gold: 8,
          previousLevel: 3,
          newLevel: 5,
          attributePointsGained: 4,
          items: [],
        },
      }),
    )

    expect(screen.getByTestId('combat-level-up').textContent).toContain('Level 3 → 5')
    expect(screen.getByTestId('combat-level-up').textContent).toContain('+4 Attribute Points')
  })

  it('shows technique buttons and status badges in Combat 2.0', () => {
    renderPanel(
      combatFixture({
        status: 'ACTIVE',
        rewards: null,
        playerStatuses: [{ type: 'GUARDED', stacks: 1, remainingRounds: 1 }],
        enemyStatuses: [{ type: 'BLEED', stacks: 2, remainingRounds: 3 }],
        techniques: [
          {
            code: 'SWORD_DEEP_CUT',
            name: 'Deep Cut',
            description: 'Opens a bleeding wound.',
            staminaCost: 12,
            disabledReason: null,
          },
        ],
      }),
    )

    expect(screen.getByTestId('combat-technique-SWORD_DEEP_CUT').textContent).toContain('Deep Cut')
    expect(screen.getByTestId('combat-player-statuses').textContent).toContain('GUARDED')
    expect(screen.getByTestId('combat-enemy-statuses').textContent).toContain('BLEED')
    expect(screen.getByTestId('combat-action-QUICK_ATTACK').textContent).toContain('(8)')
    expect(screen.getByTestId('combat-stage')).toBeTruthy()
    expect(screen.getByTestId('combat-fighter-player')).toBeTruthy()
    expect(screen.getByTestId('combat-fighter-enemy')).toBeTruthy()
    expect(screen.getByTestId('combat-enemy-health').textContent).toContain('0 / 70')
  })

  it('lets a stunned player skip the turn', () => {
    renderPanel(
      combatFixture({
        status: 'ACTIVE',
        rewards: null,
        playerStunned: true,
        playerStatuses: [{ type: 'STUN', stacks: 1, remainingRounds: 1 }],
      }),
    )

    expect(screen.getByTestId('combat-action-QUICK_ATTACK')).toHaveProperty('disabled', true)
    expect(screen.getByTestId('combat-skip-stun').textContent).toContain('Skip turn')
    expect(screen.getByTestId('combat-skip-stun')).toHaveProperty('disabled', false)
  })

  it('offers a claim-rewards action when victory loot does not fit', () => {
    renderPanel(
      combatFixture({
        status: 'PLAYER_WON',
        rewards: null,
      }),
    )

    expect(screen.getByTestId('combat-claim-rewards').textContent).toContain('inventory is full')
    expect(screen.getByTestId('combat-claim-rewards-button').textContent).toContain('Claim rewards')
    expect(screen.queryByTestId('combat-dismiss')).toBeNull()
  })

  it('hides techniques for legacy combats', () => {
    renderPanel(
      combatFixture({
        status: 'ACTIVE',
        rulesVersion: 1,
        rewards: null,
        techniques: [
          {
            code: 'SWORD_DEEP_CUT',
            name: 'Deep Cut',
            description: 'Opens a bleeding wound.',
            staminaCost: 12,
            disabledReason: null,
          },
        ],
      }),
    )

    expect(screen.queryByTestId('combat-techniques')).toBeNull()
    expect(screen.getByTestId('combat-action-QUICK_ATTACK')).toBeTruthy()
  })

  it('places flee in the side rail and filters the battle log', () => {
    renderPanel(
      combatFixture({
        status: 'ACTIVE',
        rewards: null,
        events: [
          { roundNumber: 1, sequenceNumber: 1, type: 'PLAYER_ATTACK', message: 'You strike.' },
          { roundNumber: 1, sequenceNumber: 2, type: 'ENEMY_ATTACK', message: 'The thug hits you.' },
          { roundNumber: 1, sequenceNumber: 3, type: 'INFO', message: 'Rain starts.' },
        ],
      }),
    )

    expect(screen.getByTestId('combat-action-RETREAT').textContent).toContain('Flee')
    expect(screen.getByTestId('combat-log').textContent).toContain('You strike.')
    expect(screen.getByTestId('combat-log').textContent).toContain('The thug hits you.')
  })
})
