// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { QuestTracker } from './QuestTracker'

vi.mock('../api/quests', () => ({
  fetchQuests: vi.fn(),
}))

import { fetchQuests } from '../api/quests'

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

describe('QuestTracker', () => {
  it('shows tracked objective progress', async () => {
    vi.mocked(fetchQuests).mockResolvedValue({
      quests: [
        {
          code: 'QST_MILITIA_NOTICE',
          name: 'Militia Notice',
          description: 'Old Town is restless.',
          category: 'MAIN',
          status: 'ACTIVE',
          recommendedLevel: 1,
          startNpcCode: 'MILITIA_OFFICER',
          turnInNpcCode: 'MILITIA_OFFICER',
          nextQuestCode: 'QST_ARM_THE_WATCH',
          tracked: true,
          objectives: [
            {
              type: 'KILL',
              targetCode: 'STREET_THUG',
              requiredAmount: 1,
              currentAmount: 0,
              completed: false,
              displayText: 'Defeat a Street Thug',
              consumeOnTurnIn: false,
            },
          ],
          rewards: [],
          unlocks: [],
        },
      ],
    })
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <MemoryRouter>
        <QueryClientProvider client={queryClient}>
          <QuestTracker />
        </QueryClientProvider>
      </MemoryRouter>,
    )
    expect(await screen.findByTestId('tracked-quest-QST_MILITIA_NOTICE')).toBeTruthy()
    expect(screen.getByText('Defeat a Street Thug 0/1')).toBeTruthy()
  })
})
