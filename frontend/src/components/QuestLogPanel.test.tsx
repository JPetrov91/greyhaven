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
  name: 'Militia Notice',
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
})
