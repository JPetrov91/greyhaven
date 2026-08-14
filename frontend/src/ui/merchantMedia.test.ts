import { describe, expect, it } from 'vitest'
import { merchantPortraitUrl } from './merchantMedia'

describe('merchantPortraitUrl', () => {
  it('maps known merchant portrait codes', () => {
    expect(merchantPortraitUrl('edric-varn')).toBe('/merchants/edric-varn.webp')
    expect(merchantPortraitUrl('unknown')).toBeUndefined()
  })
})
