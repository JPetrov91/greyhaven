import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'

const typography = readFileSync(join(dirname(fileURLToPath(import.meta.url)), 'typography.css'), 'utf8')

describe('typography roles', () => {
  it('exposes a class for each role', () => {
    for (const role of [
      'type-display',
      'type-page-heading',
      'type-panel-heading',
      'type-section-heading',
      'type-item',
      'type-body',
      'type-compact',
      'type-meta',
      'type-micro',
      'type-numeric',
    ]) {
      expect(typography).toContain(`.${role} {`)
    }
  })

  it('maps global elements only', () => {
    expect(typography).toMatch(/:root \{/)
    expect(typography).toMatch(/h1,\s*h2 \{/)
    expect(typography).toMatch(/h3 \{/)
    expect(typography).toMatch(/\.brand \{/)
    expect(typography).toMatch(/\.muted \{/)
  })

  it('uses tabular numbers for numeric UI', () => {
    expect(typography).toMatch(
      /\.type-numeric \{[\s\S]*font-variant-numeric:\s*tabular-nums;/,
    )
  })

  it('keeps inscription caps and bronze section labels', () => {
    expect(typography).toMatch(/\.type-display \{[\s\S]*text-transform:\s*uppercase;/)
    expect(typography).toMatch(/\.type-page-heading \{[\s\S]*text-transform:\s*uppercase;/)
    expect(typography).toMatch(/\.type-panel-heading \{[\s\S]*text-transform:\s*uppercase;/)
    expect(typography).toMatch(/\.type-section-heading \{[\s\S]*text-transform:\s*uppercase;/)
    expect(typography).toContain('.type-positive {')
    expect(typography).toContain('.type-negative {')
    expect(typography).toContain('.type-warning {')
  })
})
