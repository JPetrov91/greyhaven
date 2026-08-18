// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { LocationQuestAction } from './LocationQuestAction'
import { ToastProvider } from '../ui/ToastRegion'

vi.mock('../api/quests', () => ({
  fetchQuests: vi.fn(),
  turnInQuest: vi.fn(),
}))

import { fetchQuests } from '../api/quests'

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

describe('LocationQuestAction', () => {
  it('shows a talk action from the current objective hint', async () => {
    vi.mocked(fetchQuests).mockResolvedValue({
      quests: [
        {
          code: 'QST_MILITIA_NOTICE',
          name: 'Issued Steel',
          description: 'Old Town is restless.',
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
          rewards: [],
          unlocks: [],
          actionHint: 'TALK',
          actionLocationCode: 'CITY_SQUARE',
        },
      ],
    })
    const onAimBren = vi.fn()
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <ToastProvider>
        <QueryClientProvider client={queryClient}>
          <LocationQuestAction locationCode="CITY_SQUARE" onAimBren={onAimBren} />
        </QueryClientProvider>
      </ToastProvider>,
    )
    expect(await screen.findByTestId('location-quest-objective')).toHaveProperty(
      'textContent',
      'Talk to Watch-Sergeant Bren',
    )
    screen.getByTestId('location-quest-cta').click()
    expect(onAimBren).toHaveBeenCalledTimes(1)
  })
})
