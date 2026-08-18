// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { QuestLogPanel } from './QuestLogPanel'

vi.mock('../api/quests', () => ({
  fetchQuests: vi.fn(),
  trackQuest: vi.fn(),
  untrackQuest: vi.fn(),
}))

import { fetchQuests } from '../api/quests'
import type { QuestResponse } from '../api/types'

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

const notice: QuestResponse = {
  code: 'QST_MILITIA_NOTICE',
  name: 'Issued Steel',
  description: 'Old Town is restless.',
  category: 'MAIN',
  status: 'AVAILABLE',
  recommendedLevel: 1,
  startNpcCode: 'MILITIA_OFFICER',
  startNpcName: 'Watch-Sergeant Bren',
  turnInNpcCode: 'MILITIA_OFFICER',
  turnInNpcName: 'Watch-Sergeant Bren',
  nextQuestCode: 'QST_ARM_THE_WATCH',
  nextQuestName: 'Arm the Watch',
  repeatable: false,
  tracked: false,
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
  rewards: [{ kind: 'XP', amount: 40, itemCode: null, itemName: null, unlockCode: null }],
  unlocks: [],
}

describe('QuestLogPanel', () => {
  it('lists available quests and sends the player to the NPC', async () => {
    vi.mocked(fetchQuests).mockResolvedValue({ quests: [notice] })
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={queryClient}>
        <QuestLogPanel />
      </QueryClientProvider>,
    )
    expect(await screen.findByTestId('quest-QST_MILITIA_NOTICE')).toBeTruthy()
    expect(screen.getByTestId('quest-hint-QST_MILITIA_NOTICE').textContent).toContain('Talk to Watch-Sergeant Bren')
    expect(screen.queryByTestId('accept-quest-QST_MILITIA_NOTICE')).toBeNull()
  })

  it('hides binary counters and shows the kit preview on the Active tab', async () => {
    vi.mocked(fetchQuests).mockResolvedValue({
      quests: [
        {
          ...notice,
          status: 'ACTIVE',
          tracked: true,
          description: 'The watch is thin. Bren will put rust in your hands and send you to Old Town. Come back.',
          rewards: [
            { kind: 'XP', amount: 40, itemCode: null, itemName: null, unlockCode: null },
            { kind: 'GOLD', amount: 15, itemCode: null, itemName: null, unlockCode: null },
            { kind: 'ITEM', amount: 1, itemCode: null, itemName: 'Rusty kit — chosen with Bren', unlockCode: null },
          ],
        },
      ],
    })
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={queryClient}>
        <QuestLogPanel />
      </QueryClientProvider>,
    )
    expect(await screen.findByTestId('quest-QST_MILITIA_NOTICE')).toBeTruthy()
    expect(screen.getByTestId('quest-objective-QST_MILITIA_NOTICE').textContent).toBe('Reach Old Town')
    expect(screen.getByTestId('quest-recommended-QST_MILITIA_NOTICE').textContent).toContain('Old Town')
    expect(screen.getByText(/Rusty kit — chosen with Bren/)).toBeTruthy()
    expect(screen.queryByText(/0\/1/)).toBeNull()
  })

  it('aims at Bren from a City Square recommendation', async () => {
    const onAimBren = vi.fn()
    vi.mocked(fetchQuests).mockResolvedValue({
      quests: [
        {
          ...notice,
          status: 'ACTIVE',
          tracked: true,
          objectives: [
            {
              type: 'TALK_TO_NPC',
              targetCode: 'MILITIA_OFFICER',
              requiredAmount: 1,
              currentAmount: 0,
              completed: false,
              displayText: 'Talk to Watch-Sergeant Bren',
              consumeOnTurnIn: false,
            },
          ],
        },
      ],
    })
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={queryClient}>
        <QuestLogPanel onAimBren={onAimBren} />
      </QueryClientProvider>,
    )
    const recommended = await screen.findByTestId('quest-recommended-QST_MILITIA_NOTICE')
    expect(recommended.textContent).toContain('City Square')
    recommended.click()
    expect(onAimBren).toHaveBeenCalledTimes(1)
  })
})
