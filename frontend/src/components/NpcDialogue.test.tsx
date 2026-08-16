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
          questBadges: ['ACTIVE'],
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
        { type: 'DIALOGUE', questCode: 'QST_MILITIA_NOTICE', merchantCode: null, label: 'I’ll walk Old Town', action: 'WALK_OLD_TOWN' },
        { type: 'DIALOGUE', questCode: 'QST_MILITIA_NOTICE', merchantCode: null, label: 'Why me?', action: 'WHY_ME' },
        { type: 'CHOOSE_KIT', questCode: 'QST_MILITIA_NOTICE', merchantCode: null, label: 'Sword', hint: 'Rusty weapon + shield', action: 'SWORD' },
        { type: 'CHOOSE_KIT', questCode: 'QST_MILITIA_NOTICE', merchantCode: null, label: 'Axe', hint: 'Rusty weapon + shield', action: 'AXE' },
        { type: 'CHOOSE_KIT', questCode: 'QST_MILITIA_NOTICE', merchantCode: null, label: 'Mace', hint: 'Rusty weapon + shield', action: 'MACE' },
        { type: 'CHOOSE_KIT', questCode: 'QST_MILITIA_NOTICE', merchantCode: null, label: 'Daggers', hint: 'No shield', action: 'DAGGERS' },
        { type: 'CLOSE', questCode: null, merchantCode: null, label: 'Not now' },
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
    expect(screen.getByTestId('talk-npc-MILITIA_OFFICER').textContent).toContain('…')
    expect(screen.getByTestId('npc-action-DIALOGUE-WALK_OLD_TOWN')).toBeTruthy()
    expect(screen.getByTestId('npc-action-DIALOGUE-WHY_ME')).toBeTruthy()
    expect(screen.getByTestId('npc-action-CHOOSE_KIT-SWORD').textContent).toContain('Rusty weapon + shield')
    expect(screen.getByTestId('npc-action-CHOOSE_KIT-AXE').textContent).toContain('Rusty weapon + shield')
    expect(screen.getByTestId('npc-action-CHOOSE_KIT-MACE').textContent).toContain('Rusty weapon + shield')
    expect(screen.getByTestId('npc-action-CHOOSE_KIT-DAGGERS').textContent).toContain('No shield')
    expect(screen.getByTestId('npc-action-CLOSE').textContent).toContain('Not now')
    expect(screen.queryByTestId('npc-action-CHOOSE_KIT-BOW')).toBeNull()
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
      name: 'Issued Steel',
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
        { kind: 'ITEM', amount: 1, itemCode: 'RUSTY_SWORD', itemName: 'Rusty Sword', unlockCode: null },
        { kind: 'ITEM', amount: 1, itemCode: 'RUSTY_SHIELD', itemName: 'Rusty Shield', unlockCode: null },
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
    expect(screen.getByTestId('quest-complete').textContent).toContain('Rusty Sword')
    expect(screen.getByTestId('quest-complete').textContent).toContain('Rusty Shield')
    expect(screen.getByTestId('quest-complete').textContent).toContain('Arm the Watch')
  })
})
