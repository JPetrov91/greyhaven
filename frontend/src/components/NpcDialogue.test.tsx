// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { NpcDialogue } from './NpcDialogue'

vi.mock('../api/npcs', () => ({
  fetchNpcs: vi.fn(),
  talkToNpc: vi.fn(),
}))
vi.mock('../api/quests', () => ({
  acceptQuest: vi.fn(),
  turnInQuest: vi.fn(),
}))

import { fetchNpcs, talkToNpc } from '../api/npcs'
import { turnInQuest } from '../api/quests'

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

describe('NpcDialogue', () => {
  it('talks to the militia officer', async () => {
    vi.mocked(fetchNpcs).mockResolvedValue({
      npcs: [
        {
          code: 'MILITIA_OFFICER',
          name: 'Watch-Sergeant Bren',
          title: 'Militia officer',
          description: 'Posts notices.',
          greeting: 'The watch has work.',
          portraitCode: 'militia-officer',
          locationCode: 'CITY_SQUARE',
          merchantCode: null,
          interactions: ['TALK', 'QUEST'],
          questBadges: ['AVAILABLE_QUEST'],
        },
      ],
    })
    vi.mocked(talkToNpc).mockResolvedValue({
      code: 'MILITIA_OFFICER',
      name: 'Watch-Sergeant Bren',
      title: 'Militia officer',
      portraitCode: 'militia-officer',
      text: 'Old Town is restless.',
      merchantCode: null,
      actions: [
        { type: 'ACCEPT', questCode: 'QST_MILITIA_NOTICE', merchantCode: null, label: 'Accept quest' },
        { type: 'CLOSE', questCode: null, merchantCode: null, label: 'Leave' },
      ],
    })
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={queryClient}>
        <NpcDialogue open onClose={() => undefined} />
      </QueryClientProvider>,
    )
    fireEvent.click(await screen.findByTestId('talk-npc-MILITIA_OFFICER'))
    expect(await screen.findByTestId('npc-talk-text')).toHaveProperty('textContent', 'Old Town is restless.')
    expect(screen.getByTestId('talk-npc-MILITIA_OFFICER').textContent).toContain('!')
    expect(screen.getByTestId('npc-action-ACCEPT')).toBeTruthy()
  })

  it('shows a quest complete summary after turn-in', async () => {
    vi.mocked(fetchNpcs).mockResolvedValue({
      npcs: [
        {
          code: 'MILITIA_OFFICER',
          name: 'Watch-Sergeant Bren',
          title: 'Militia officer',
          description: 'Posts notices.',
          greeting: 'The watch has work.',
          portraitCode: 'militia-officer',
          locationCode: 'CITY_SQUARE',
          merchantCode: null,
          interactions: ['TALK', 'QUEST'],
          questBadges: ['TURN_IN'],
        },
      ],
    })
    vi.mocked(talkToNpc).mockResolvedValue({
      code: 'MILITIA_OFFICER',
      name: 'Watch-Sergeant Bren',
      title: 'Militia officer',
      portraitCode: 'militia-officer',
      text: 'You came back.',
      merchantCode: null,
      actions: [
        { type: 'TURN_IN', questCode: 'QST_MILITIA_NOTICE', merchantCode: null, label: 'Turn in' },
        { type: 'CLOSE', questCode: null, merchantCode: null, label: 'Leave' },
      ],
    })
    vi.mocked(turnInQuest).mockResolvedValue({
      code: 'QST_MILITIA_NOTICE',
      name: 'Militia Notice',
      description: 'Old Town is restless.',
      category: 'MAIN',
      status: 'COMPLETED',
      recommendedLevel: 1,
      startNpcCode: 'MILITIA_OFFICER',
      startNpcName: 'Watch-Sergeant Bren',
      turnInNpcCode: 'MILITIA_OFFICER',
      turnInNpcName: 'Watch-Sergeant Bren',
      nextQuestCode: 'QST_ARM_THE_WATCH',
      nextQuestName: 'Arm the Watch',
      repeatable: false,
      tracked: true,
      objectives: [],
      rewards: [
        { kind: 'XP', amount: 40, itemCode: null, itemName: null, unlockCode: null },
        { kind: 'ITEM', amount: 1, itemCode: 'HEALING_POTION', itemName: 'Healing Potion', unlockCode: null },
      ],
      unlocks: [],
    })
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={queryClient}>
        <NpcDialogue open onClose={() => undefined} />
      </QueryClientProvider>,
    )
    fireEvent.click(await screen.findByTestId('talk-npc-MILITIA_OFFICER'))
    fireEvent.click(await screen.findByTestId('npc-action-TURN_IN'))
    await waitFor(() => {
      expect(screen.getByTestId('quest-complete').textContent).toContain('Quest Complete')
    })
    expect(screen.getByTestId('quest-complete').textContent).toContain('40 XP')
    expect(screen.getByTestId('quest-complete').textContent).toContain('Healing Potion')
    expect(screen.getByTestId('quest-complete').textContent).toContain('Arm the Watch')
  })
})
