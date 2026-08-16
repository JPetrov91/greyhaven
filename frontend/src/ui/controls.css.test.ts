import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'

const controls = readFileSync(join(dirname(fileURLToPath(import.meta.url)), 'controls.css'), 'utf8')

describe('control engine', () => {
  it('defines button variants and interaction states', () => {
    for (const name of ['btn-primary', 'btn-secondary', 'btn-ghost', 'btn-danger', 'btn-icon', 'btn-loading']) {
      expect(controls).toContain(`.${name}`)
    }
    expect(controls).toContain(':hover')
    expect(controls).toContain(':active')
    expect(controls).toContain(':focus-visible')
    expect(controls).toContain(':disabled')
  })

  it('defines tab states without pill chrome or gold fills', () => {
    expect(controls).toContain('.tab:hover')
    expect(controls).toContain('.tab-active')
    expect(controls).toContain('.tab:disabled')
    expect(controls).toContain('.tabs-filters')
    expect(controls).toContain('var(--color-gold-normal)')
    expect(controls).toContain('var(--asset-ui-bronze-edge)')
    expect(controls).toContain('var(--control-plate)')
    expect(controls).toContain('var(--control-luminance)')
    expect(controls).not.toMatch(/\.btn-primary[^{]*\{[^}]*--color-page-bg/)
    expect(controls).not.toMatch(/\.tab[^{]*\{[^}]*--radius-pill/)
    expect(controls).not.toMatch(/\.tab-active[^{]*\{[^}]*--color-surface-selected/)
    expect(controls).not.toMatch(/\.tab:hover[^{]*\{[^}]*--color-surface-interactive/)
  })

  it('uses compact UI type and tight radius', () => {
    expect(controls).toContain('var(--type-compact-family)')
    expect(controls).toContain('var(--radius-2)')
    expect(controls).not.toMatch(/--font-chrome/)
    expect(controls).not.toMatch(/--type-display/)
  })

  it('avoids glow, glass, and highlight-gold edges', () => {
    expect(controls).not.toMatch(/backdrop-filter/)
    expect(controls).not.toMatch(/--color-gold-highlight/)
    expect(controls).not.toMatch(/0 0 \d+px/)
  })
})
