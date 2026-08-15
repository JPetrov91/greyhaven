// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
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
    expect(screen.getByTestId('npc-action-ACCEPT')).toBeTruthy()
  })
})
