import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'

const surfaces = readFileSync(join(dirname(fileURLToPath(import.meta.url)), 'surfaces.css'), 'utf8')

describe('surface engine', () => {
  it('exposes a class for each surface variant', () => {
    for (const variant of [
      'surface-page',
      'surface-base',
      'surface-raised',
      'surface-inset',
      'surface-interactive',
      'surface-selected',
      'surface-floating',
    ]) {
      expect(surfaces).toContain(`.${variant}`)
    }
  })

  it('maps shared primitives only', () => {
    expect(surfaces).toMatch(/\.panel,/)
    expect(surfaces).toMatch(/\.ui-control,/)
    expect(surfaces).toMatch(/\.field input,/)
    expect(surfaces).toMatch(/\.ui-floating,/)
    expect(surfaces).toMatch(/\.tooltip-panel,/)
    expect(surfaces).toMatch(/\.ui-dialog,/)
    expect(surfaces).toMatch(/\.toast \{/)
    expect(surfaces).not.toMatch(/location-hero/)
    expect(surfaces).not.toMatch(/game-shell-main/)
  })

  it('composes grain, luminance, forged edges, and restrained depth', () => {
    expect(surfaces).toContain('var(--surface-texture-page)')
    expect(surfaces).toContain('var(--surface-texture-panel)')
    expect(surfaces).toContain('var(--surface-texture-raised)')
    expect(surfaces).toContain('var(--surface-texture-inset)')
    expect(surfaces).toContain('var(--surface-plate-position)')
    expect(surfaces).toContain('var(--surface-panel-veil)')
    expect(surfaces).toContain('var(--surface-raised-veil)')
    expect(surfaces).toContain('var(--surface-inset-veil)')
    expect(surfaces).toContain('var(--surface-texture-size-panel)')
    expect(surfaces).not.toMatch(/128px 128px/)
    expect(surfaces).not.toMatch(/feTurbulence/)
    expect(surfaces).toContain('var(--surface-plate-finish)')
    expect(surfaces).toContain('var(--surface-luminance-inset)')
    expect(surfaces).toContain('var(--surface-highlight)')
    expect(surfaces).toContain('var(--surface-inner)')
    expect(surfaces).toContain('var(--color-edge-highlight)')
    expect(surfaces).toContain('var(--shadow-inset)')
    expect(surfaces).toContain('var(--shadow-raised)')
    expect(surfaces).toContain('var(--shadow-floating)')
    expect(surfaces).toContain('border-image-source')
    expect(surfaces).toContain('var(--asset-frame-corners)')
    expect(surfaces).toContain('.panel:not(.surface-page)::before')
    expect(surfaces).toContain('.panel:not(.surface-page)::after')
    expect(surfaces).toMatch(/border-image-slice:\s*64/)
    expect(surfaces).toContain('var(--asset-ui-panel-rim)')
  })

  it('avoids glass, glow, and bright gold edges', () => {
    expect(surfaces).not.toMatch(/backdrop-filter/)
    expect(surfaces).not.toMatch(/filter:\s*blur/)
    expect(surfaces).not.toMatch(/--color-gold-highlight/)
    expect(surfaces).not.toMatch(/--color-gold-strong/)
    expect(surfaces).not.toMatch(/0 0 \d+px/)
  })
})
