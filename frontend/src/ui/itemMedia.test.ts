import { describe, expect, it } from 'vitest'
import { itemArtUrl } from './itemMedia'

describe('itemArtUrl', () => {
  it('maps every seeded catalog item to dedicated art', () => {
    expect(itemArtUrl('MILITIA_SHORTSWORD')).toBe('/items/militia_shortsword.webp')
    expect(itemArtUrl('ARMING_SWORD')).toBe('/items/arming_sword.webp')
    expect(itemArtUrl('RUSTY_SWORD')).toBe('/items/rusty_sword.webp')
    expect(itemArtUrl('LEATHER_ARMOR')).toBe('/items/leather_armor.webp')
    expect(itemArtUrl('COPPER_RING')).toBe('/items/copper_ring.webp')
    expect(itemArtUrl('HEALING_POTION')).toBe('/items/healing_potion.webp')
    expect(itemArtUrl('WOLF_PELT')).toBe('/items/wolf_pelt.webp')
    expect(itemArtUrl('UNKNOWN_ITEM')).toBeUndefined()
    expect(itemArtUrl(undefined)).toBeUndefined()
  })
})
