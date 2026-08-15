// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { QuestLogPanel } from './QuestLogPanel'

vi.mock('../api/quests', () => ({
  fetchQuests: vi.fn(),
  acceptQuest: vi.fn(),
  turnInQuest: vi.fn(),
  trackQuest: vi.fn(),
  untrackQuest: vi.fn(),
}))

import { acceptQuest, fetchQuests } from '../api/quests'
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
  turnInNpcCode: 'MILITIA_OFFICER',
  nextQuestCode: 'QST_ARM_THE_WATCH',
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
  rewards: [{ kind: 'XP', amount: 40, itemCode: null, unlockCode: null }],
  unlocks: [],
}

describe('QuestLogPanel', () => {
  it('lists available quests and accepts them', async () => {
    vi.mocked(fetchQuests).mockResolvedValue({ quests: [notice] })
    vi.mocked(acceptQuest).mockResolvedValue({ ...notice, status: 'ACTIVE', tracked: true })
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={queryClient}>
        <QuestLogPanel />
      </QueryClientProvider>,
    )
    expect(await screen.findByTestId('quest-QST_MILITIA_NOTICE')).toBeTruthy()
    fireEvent.click(screen.getByTestId('accept-quest-QST_MILITIA_NOTICE'))
    await waitFor(() => {
      expect(acceptQuest).toHaveBeenCalledWith('QST_MILITIA_NOTICE')
    })
  })
})
