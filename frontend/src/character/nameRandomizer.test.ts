import { describe, expect, it } from 'vitest'
import { CHARACTER_NAME_PATTERN, randomCharacterName } from './nameRandomizer'

describe('randomCharacterName', () => {
  it('builds a male thematic name with a space', () => {
    const name = randomCharacterName('MALE', () => 0)
    expect(name).toBe('Ragnar Ironfist')
    expect(name.length).toBeGreaterThanOrEqual(3)
    expect(name.length).toBeLessThanOrEqual(24)
    expect(name).toMatch(CHARACTER_NAME_PATTERN)
  })

  it('builds a female thematic name with a space', () => {
    const name = randomCharacterName('FEMALE', () => 0)
    expect(name).toBe('Morwen Nightveil')
    expect(name).toMatch(CHARACTER_NAME_PATTERN)
  })

  it('varies when the RNG advances', () => {
    let calls = 0
    const name = randomCharacterName('MALE', () => {
      calls += 1
      return calls === 1 ? 0.99 : 0
    })
    expect(name).toBe('Bjorn Ironfist')
  })
})
