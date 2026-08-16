// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { NoticeBoard } from './NoticeBoard'
import { ToastProvider } from '../../ui/ToastRegion'

vi.mock('../../api/world', () => ({
  fetchQuestBoard: vi.fn(),
}))

vi.mock('../../api/quests', () => ({
  fetchQuest: vi.fn(),
  acceptQuest: vi.fn(),
}))

import { fetchQuestBoard } from '../../api/world'
import { acceptQuest, fetchQuest } from '../../api/quests'

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

const rat = {
  code: 'QST_RAT_PROBLEM',
  name: 'Rat Problem',
  shortDescription: 'Rats have come up from the sewers.',
  questType: 'EXTERMINATION',
  listState: 'AVAILABLE' as const,
  recommendedLevel: 1,
  difficulty: 'EASY',
  rewards: [{ kind: 'XP', amount: 80, itemCode: null, itemName: null, unlockCode: null }],
}

const caravan = {
  code: 'QST_MISSING_CARAVAN',
  name: 'The Missing Caravan',
  shortDescription: 'A merchant caravan has vanished.',
  questType: 'INVESTIGATION',
  listState: 'UNAVAILABLE' as const,
  recommendedLevel: 4,
  difficulty: 'EASY',
  rewards: [{ kind: 'XP', amount: 320, itemCode: null, itemName: null, unlockCode: null }],
}

function renderBoard() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  const onClose = vi.fn()
  render(
    <ToastProvider>
      <QueryClientProvider client={queryClient}>
        <NoticeBoard locationCode="CITY_SQUARE" open onClose={onClose} />
      </QueryClientProvider>
    </ToastProvider>,
  )
  return onClose
}

describe('NoticeBoard', () => {
  it('lists available notices and keeps the board on home', async () => {
    vi.mocked(fetchQuestBoard).mockResolvedValue({
      locationCode: 'CITY_SQUARE',
      quests: [rat, caravan],
    })
    renderBoard()
    expect(await screen.findByTestId('notice-quest-QST_RAT_PROBLEM')).toBeTruthy()
    expect(screen.getByTestId('notice-board').getAttribute('data-mode')).toBe('LIST')
    expect(screen.getByTestId('notice-quest-QST_MISSING_CARAVAN')).toHaveProperty('disabled', true)
    expect(screen.getByTestId('notice-rewards-QST_RAT_PROBLEM').textContent).toContain('80 XP')
  })

  it('expands left into preview and accepts without leaving the board route', async () => {
    vi.mocked(fetchQuestBoard).mockResolvedValue({
      locationCode: 'CITY_SQUARE',
      quests: [rat],
    })
    vi.mocked(fetchQuest).mockResolvedValue({
      code: 'QST_RAT_PROBLEM',
      name: 'Rat Problem',
      description: 'Thin the nest.',
      category: 'SIDE',
      status: 'AVAILABLE',
      recommendedLevel: 1,
      startNpcCode: 'MILITIA_OFFICER',
      startNpcName: 'Watch-Sergeant Bren',
      turnInNpcCode: 'MILITIA_OFFICER',
      turnInNpcName: 'Watch-Sergeant Bren',
      nextQuestCode: null,
      nextQuestName: null,
      repeatable: false,
      tracked: false,
      objectives: [
        {
          type: 'VISIT_LOCATION',
          targetCode: 'SEWERS',
          requiredAmount: 1,
          currentAmount: 0,
          completed: false,
          displayText: 'Reach the Sewers',
          consumeOnTurnIn: false,
        },
      ],
      rewards: [{ kind: 'GOLD', amount: 20, itemCode: null, itemName: null, unlockCode: null }],
      unlocks: [],
      questType: 'EXTERMINATION',
      difficulty: 'EASY',
      locationName: 'Sewers',
      regionName: 'Greyhaven',
    })
    vi.mocked(acceptQuest).mockResolvedValue({
      code: 'QST_RAT_PROBLEM',
      name: 'Rat Problem',
      description: 'Thin the nest.',
      category: 'SIDE',
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
      objectives: [],
      rewards: [],
      unlocks: [],
    })
    const onClose = renderBoard()
    fireEvent.click(await screen.findByTestId('notice-quest-QST_RAT_PROBLEM'))
    expect(screen.getByTestId('notice-board').getAttribute('data-mode')).toBe('PREVIEW')
    expect(screen.getByTestId('notice-board-list')).toBeTruthy()
    expect((await screen.findByTestId('notice-preview-difficulty')).textContent).toBe('Easy')
    fireEvent.click(screen.getByTestId('notice-accept'))
    await waitFor(() => {
      expect(acceptQuest).toHaveBeenCalledWith('QST_RAT_PROBLEM')
    })
    await waitFor(() => {
      expect(onClose).toHaveBeenCalled()
    })
    expect(screen.getByTestId('toast-region').textContent).toContain('Quest Accepted')
  })

  it('shows the empty board copy', async () => {
    vi.mocked(fetchQuestBoard).mockResolvedValue({ locationCode: 'CITY_SQUARE', quests: [] })
    renderBoard()
    expect(await screen.findByTestId('notice-board-empty')).toBeTruthy()
  })
})
