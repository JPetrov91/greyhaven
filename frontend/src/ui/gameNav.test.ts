import { describe, expect, it } from 'vitest'
import { gameLink, gameViewFromLocation, isGameNavActive } from './gameNav'

describe('isGameNavActive', () => {
  it('marks Home on /game without hash or market panel', () => {
    const loc = { pathname: '/game', search: '', hash: '' }
    expect(isGameNavActive('home', loc)).toBe(true)
    expect(isGameNavActive('world', loc)).toBe(false)
    expect(isGameNavActive('character', loc)).toBe(false)
    expect(isGameNavActive('inventory', loc)).toBe(false)
    expect(isGameNavActive('market', loc)).toBe(false)
  })

  it('marks Locations from the world hash', () => {
    const loc = { pathname: '/game', search: '', hash: '#world' }
    expect(isGameNavActive('world', loc)).toBe(true)
    expect(isGameNavActive('home', loc)).toBe(false)
  })

  it('marks Character, Inventory, Mastery and Expeditions from the hash', () => {
    expect(isGameNavActive('character', { pathname: '/game', search: '', hash: '#character' })).toBe(true)
    expect(isGameNavActive('world', { pathname: '/game', search: '', hash: '#character' })).toBe(false)
    expect(isGameNavActive('inventory', { pathname: '/game', search: '', hash: '#inventory' })).toBe(true)
    expect(isGameNavActive('equipment', { pathname: '/game', search: '', hash: '#inventory' })).toBe(true)
    expect(isGameNavActive('mastery', { pathname: '/game', search: '', hash: '#mastery' })).toBe(true)
    expect(isGameNavActive('expeditions', { pathname: '/game', search: '', hash: '#expeditions' })).toBe(true)
    expect(isGameNavActive('world', { pathname: '/game', search: '', hash: '#mastery' })).toBe(false)
  })

  it('marks Market from the panel query and ignores hash', () => {
    const loc = { pathname: '/game', search: '?panel=market', hash: '#inventory' }
    expect(isGameNavActive('market', loc)).toBe(true)
    expect(isGameNavActive('inventory', loc)).toBe(false)
    expect(isGameNavActive('home', loc)).toBe(false)
  })
})

describe('gameViewFromLocation', () => {
  it('returns home by default and market when the panel query is set', () => {
    expect(gameViewFromLocation({ pathname: '/game', search: '', hash: '' })).toBe('home')
    expect(gameViewFromLocation({ pathname: '/game', search: '?panel=market', hash: '' })).toBe('market')
  })
})

describe('gameLink', () => {
  it('builds market as a query and other views as hashes', () => {
    expect(gameLink('market')).toEqual({ pathname: '/game', search: '?panel=market', hash: '' })
    expect(gameLink('world')).toEqual({ pathname: '/game', search: '', hash: 'world' })
    expect(gameLink('home')).toEqual({ pathname: '/game', search: '', hash: '' })
  })
})
