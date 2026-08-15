import { describe, expect, it } from 'vitest'
import { arenaRankFromRating } from './arenaRank'

describe('arenaRankFromRating', () => {
  it('maps the starting rating to Bronze IV', () => {
    const rank = arenaRankFromRating(1000)
    expect(rank.name).toBe('Bronze IV')
    expect(rank.nextName).toBe('Bronze III')
    expect(rank.progress).toBe(0)
    expect(rank.needed).toBe(50)
  })

  it('advances divisions inside a tier', () => {
    const rank = arenaRankFromRating(1075)
    expect(rank.name).toBe('Bronze III')
    expect(rank.progress).toBe(25)
  })

  it('promotes into the next tier', () => {
    const rank = arenaRankFromRating(1400)
    expect(rank.name).toBe('Gold IV')
    expect(rank.nextName).toBe('Gold III')
  })
})
