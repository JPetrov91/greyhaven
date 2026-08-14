import { describe, expect, it } from 'vitest'
import { isGameNavActive } from './gameNav'

describe('isGameNavActive', () => {
  it('marks World on /game without hash or market panel', () => {
    const loc = { pathname: '/game', search: '', hash: '' }
    expect(isGameNavActive('world', loc)).toBe(true)
    expect(isGameNavActive('character', loc)).toBe(false)
    expect(isGameNavActive('inventory', loc)).toBe(false)
    expect(isGameNavActive('market', loc)).toBe(false)
  })

  it('marks Character and Inventory from the hash', () => {
    expect(isGameNavActive('character', { pathname: '/game', search: '', hash: '#character' })).toBe(true)
    expect(isGameNavActive('world', { pathname: '/game', search: '', hash: '#character' })).toBe(false)
    expect(isGameNavActive('inventory', { pathname: '/game', search: '', hash: '#inventory' })).toBe(true)
  })

  it('marks Market from the panel query and ignores hash', () => {
    const loc = { pathname: '/game', search: '?panel=market', hash: '#inventory' }
    expect(isGameNavActive('market', loc)).toBe(true)
    expect(isGameNavActive('inventory', loc)).toBe(false)
    expect(isGameNavActive('world', loc)).toBe(false)
  })
})
