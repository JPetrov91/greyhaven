// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import type { CombatResponse } from '../api/types'
import { CombatPanel } from './CombatPanel'

vi.mock('../api/combat', () => ({
  submitCombatAction: vi.fn(),
  acknowledgeCombat: vi.fn(),
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
    roundNumber: 1,
    playerHealth: 120,
    playerMaxHealth: 165,
    playerStamina: 40,
    playerMaxStamina: 85,
    enemyHealth: 0,
    enemyMaxHealth: 70,
    monster: {
      id: 'mon-1',
      code: 'STREET_THUG',
      name: 'Street Thug',
      level: 1,
      maxHealth: 70,
    },
    potionAvailable: false,
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
})
