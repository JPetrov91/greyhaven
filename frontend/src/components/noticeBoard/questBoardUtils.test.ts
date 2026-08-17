import { describe, expect, it } from 'vitest'
import { questTypeArtUrl, questTypeTone, rewardChips } from './questBoardUtils'

describe('quest type art', () => {
  it('maps known types to emblems and falls back for unknown ones', () => {
    expect(questTypeArtUrl('INVESTIGATION')).toBe('/quests/types/investigation.png')
    expect(questTypeArtUrl('EXTERMINATION')).toBe('/quests/types/extermination.png')
    expect(questTypeArtUrl('MAIN')).toBe('/quests/types/main.png')
    expect(questTypeArtUrl('SIDE')).toBe('/quests/types/side.png')
    expect(questTypeArtUrl('DELIVERY')).toBe('/quests/types/generic.png')
    expect(questTypeArtUrl(null)).toBe('/quests/types/generic.png')
    expect(questTypeTone('investigation')).toBe('investigation')
    expect(questTypeTone('UNKNOWN')).toBe('generic')
  })

  it('renders XP and gold as icon chips with amounts only', () => {
    expect(
      rewardChips([
        { kind: 'XP', amount: 80, itemCode: null, itemName: null, unlockCode: null },
        { kind: 'GOLD', amount: 20, itemCode: null, itemName: null, unlockCode: null },
      ]),
    ).toEqual([
      { key: 'xp-0', kind: 'XP', label: '80', iconUrl: '/quests/rewards/xp.png' },
      { key: 'gold-1', kind: 'GOLD', label: '20', iconUrl: '/quests/rewards/gold.png' },
    ])
  })
})
