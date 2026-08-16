import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'

const meters = readFileSync(join(dirname(fileURLToPath(import.meta.url)), 'meters.css'), 'utf8')

describe('meter and badge engine', () => {
  it('defines progress, badge, and counter classes', () => {
    for (const name of [
      'progress-bar',
      'progress-health',
      'progress-stamina',
      'progress-xp',
      'progress-durability',
      'progress-vital',
      'progress-compact',
      'progress-hairline',
      'progress-segments',
      'ui-meter',
      'ui-meter-value',
      'ui-meter-overlay',
      'ui-meter-beside',
      'badge',
      'status-badge',
      'status-badge-effect',
      'counter-badge',
      'rarity',
    ]) {
      expect(meters).toContain(`.${name}`)
    }
  })

  it('uses an inset trough, directional edge, and tight radius', () => {
    expect(meters).toContain('var(--meter-track)')
    expect(meters).toContain('var(--shadow-inset)')
    expect(meters).toContain('var(--color-edge-shadow)')
    expect(meters).toContain('var(--radius-2)')
    expect(meters).toContain('var(--meter-height-vital)')
    expect(meters).toContain('var(--meter-height-xp)')
    expect(meters).toContain('var(--meter-height-durability)')
  })

  it('gives fills a sheen without glow, animation, or highlight-gold', () => {
    expect(meters).toContain('var(--meter-fill-health)')
    expect(meters).toContain('var(--meter-fill-stamina)')
    expect(meters).toContain('var(--meter-fill-xp)')
    expect(meters).toContain('var(--meter-fill-durability)')
    expect(meters).toContain('var(--meter-sheen)')
    expect(meters).not.toMatch(/backdrop-filter/)
    expect(meters).not.toMatch(/--color-gold-highlight/)
    expect(meters).not.toMatch(/0 0 \d+px/)
    expect(meters).not.toMatch(/animation:/)
    expect(meters).not.toMatch(/transition:/)
    expect(meters).not.toMatch(/@keyframes/)
  })

  it('keeps counters as discs and badges off pastel washes', () => {
    expect(meters).toContain('var(--color-counter-fill)')
    expect(meters).toContain('var(--radius-pill)')
    expect(meters).toContain('var(--type-micro-family)')
    expect(meters).not.toMatch(/--font-chrome/)
    expect(meters).not.toMatch(/--type-display/)
    expect(meters).not.toMatch(/--danger-soft/)
    expect(meters).not.toMatch(/--mixed-soft/)
  })
})
