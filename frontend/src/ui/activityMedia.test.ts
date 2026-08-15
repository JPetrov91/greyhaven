import { describe, expect, it } from 'vitest'
import { activityIconUrl, activityMessageParts, formatRelativeTime } from './activityMedia'

describe('activityMedia', () => {
  it('maps event types to painted icons', () => {
    expect(activityIconUrl('EXPEDITION_COMPLETED')).toBe('/icons/activity/chest.webp')
    expect(activityIconUrl('COMBAT_VICTORY')).toBe('/icons/activity/swords.webp')
    expect(activityIconUrl('alert')).toBe('/icons/activity/alert.webp')
  })

  it('formats relative timestamps', () => {
    const now = Date.parse('2026-08-15T10:00:00Z')
    expect(formatRelativeTime('2026-08-15T09:53:00Z', now)).toBe('7m ago')
    expect(formatRelativeTime('2026-08-15T08:00:00Z', now)).toBe('2h ago')
    expect(formatRelativeTime('2026-08-15T10:00:00Z', now)).toBe('now')
  })

  it('highlights known activity subjects without inventing copy', () => {
    expect(activityMessageParts('EXPEDITION_COMPLETED', 'Your Forest Patrol returned.')).toEqual([
      { text: 'Your ', tone: 'plain' },
      { text: 'Forest Patrol', tone: 'gold' },
      { text: ' returned.', tone: 'plain' },
    ])
    expect(activityMessageParts('ITEM_FOUND', 'You found Iron Plate Boots.')).toEqual([
      { text: 'You found ', tone: 'plain' },
      { text: 'Iron Plate Boots', tone: 'blue' },
      { text: '.', tone: 'plain' },
    ])
    expect(activityMessageParts('COMBAT_VICTORY', 'You defeated a Dire Wolf.')).toEqual([
      { text: 'You defeated a ', tone: 'plain' },
      { text: 'Dire Wolf', tone: 'red' },
      { text: '.', tone: 'plain' },
    ])
  })
})
