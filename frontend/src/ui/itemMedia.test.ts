import { describe, expect, it } from 'vitest'
import { itemArtUrl } from './itemMedia'

describe('itemArtUrl', () => {
  it('maps catalog codes that have dedicated art and ignores the rest', () => {
    expect(itemArtUrl('MILITIA_SHORTSWORD')).toBe('/items/militia_shortsword.webp')
    expect(itemArtUrl('ARMING_SWORD')).toBe('/items/arming_sword.webp')
    expect(itemArtUrl('RUSTY_SWORD')).toBeUndefined()
    expect(itemArtUrl(undefined)).toBeUndefined()
  })
})
