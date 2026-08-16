import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'

const tokens = readFileSync(join(dirname(fileURLToPath(import.meta.url)), 'tokens.css'), 'utf8')

function declaredCustomProperties(css: string): Set<string> {
  return new Set([...css.matchAll(/--[a-z0-9-]+(?=\s*:)/g)].map((match) => match[0]))
}

describe('design tokens', () => {
  const properties = declaredCustomProperties(tokens)

  it('declares the semantic color, spacing, radius, and z-index contract', () => {
    expect([...properties]).toEqual(
      expect.arrayContaining([
        '--color-page-bg',
        '--color-surface-base',
        '--color-surface-raised',
        '--color-surface-inset',
        '--color-surface-interactive',
        '--color-surface-selected',
        '--color-surface-floating',
        '--color-bronze-dim',
        '--color-bronze-normal',
        '--color-bronze-strong',
        '--color-gold-dim',
        '--color-gold-normal',
        '--color-gold-strong',
        '--color-gold-highlight',
        '--color-text-primary',
        '--color-text-secondary',
        '--color-text-muted',
        '--color-text-disabled',
        '--color-text-bright',
        '--color-positive',
        '--color-negative',
        '--color-warning',
        '--color-info',
        '--spacing-4',
        '--spacing-6',
        '--spacing-8',
        '--spacing-12',
        '--spacing-16',
        '--spacing-20',
        '--spacing-24',
        '--spacing-32',
        '--radius-none',
        '--radius-2',
        '--radius-4',
        '--radius-8',
        '--radius-10',
        '--radius-pill',
        '--z-base',
        '--z-raised',
        '--z-sticky',
        '--z-dropdown',
        '--z-tooltip',
        '--z-modal',
        '--floating-gap',
        '--floating-width',
        '--scrollbar-size',
        '--scrollbar-track',
        '--scrollbar-thumb',
        '--scrollbar-thumb-hover',
        '--meter-fill-health',
        '--meter-fill-stamina',
        '--meter-fill-xp',
        '--meter-fill-durability',
        '--meter-track',
        '--meter-sheen',
        '--meter-height-vital',
        '--color-counter',
        '--color-counter-fill',
        '--icon-grid',
        '--icon-stroke',
        '--icon-stroke-ornament',
        '--icon-size-sm',
        '--icon-size-md',
        '--icon-size-lg',
        '--icon-size-xl',
        '--icon-well-size',
        '--icon-well-size-lg',
        '--icon-color',
        '--icon-color-disabled',
        '--icon-color-active',
        '--icon-disabled-opacity',
        '--ornament-color',
        '--ornament-opacity',
        '--ornament-size-corner',
        '--ornament-size-accent',
        '--mark-selected',
        '--nav-idle-color',
        '--nav-selected-color',
        '--nav-selected-plate',
        '--nav-selected-fill',
        '--nav-selected-rail',
        '--nav-selected-shadow',
        '--asset-tiny-pip',
        '--control-plate',
        '--control-plate-primary',
        '--control-plate-danger',
        '--control-luminance',
      ]),
    )
  })

  it('keeps legacy aliases so existing CSS does not need a visual migration', () => {
    expect(tokens).toMatch(/--ink:\s*var\(--color-text-primary\)/)
    expect(tokens).toMatch(/--paper:\s*var\(--color-page-bg\)/)
    expect(tokens).toMatch(/--accent:\s*var\(--color-gold-normal\)/)
    expect(tokens).toMatch(/--space-4:\s*var\(--spacing-16\)/)
    expect(tokens).toMatch(/--radius:\s*var\(--radius-10\)/)
    expect(tokens).toMatch(/--radius-sm:\s*var\(--radius-8\)/)
  })

  it('declares typography role tokens', () => {
    expect([...properties]).toEqual(
      expect.arrayContaining([
        '--font-ui',
        '--font-chrome',
        '--type-display-family',
        '--type-page-heading-family',
        '--type-panel-heading-family',
        '--type-section-heading-family',
        '--type-item-family',
        '--type-body-family',
        '--type-compact-family',
        '--type-meta-family',
        '--type-micro-family',
        '--type-numeric-family',
      ]),
    )
    expect(tokens).toMatch(/--type-display-family:\s*var\(--font-chrome\)/)
    expect(tokens).toMatch(/--type-body-family:\s*var\(--font-ui\)/)
    expect(tokens).toMatch(/--type-panel-heading-family:\s*var\(--font-chrome\)/)
    expect(tokens).toMatch(/--type-section-heading-family:\s*var\(--font-ui\)/)
    expect(tokens).toMatch(/--type-section-heading-color:\s*var\(--color-bronze-normal\)/)
    expect(tokens).toMatch(/--type-numeric-color:\s*var\(--color-text-bright\)/)
    expect(tokens).toMatch(/--font-display:\s*var\(--font-chrome\)/)
    expect(tokens).not.toMatch(/Cormorant/)
    expect(tokens).not.toMatch(/#c9a227|#e4c56a|#e8e0d4|#7dba7a|#7fb3d5/)
    expect(tokens).toMatch(/\[data-ui-mode='compact'\][\s\S]*--type-body-size:\s*0\.8125rem/)
  })

  it('declares border and shadow engine tokens', () => {
    expect([...properties]).toEqual(
      expect.arrayContaining([
        '--color-border-subtle',
        '--color-border-default',
        '--color-border-interactive',
        '--color-border-selected',
        '--border-subtle',
        '--border-default',
        '--border-interactive',
        '--border-selected',
        '--surface-luminance',
        '--surface-sheen',
        '--surface-dust',
        '--surface-plate-finish',
        '--surface-highlight',
        '--surface-inner',
        '--surface-texture-page',
        '--surface-texture-panel',
        '--surface-texture-raised',
        '--surface-texture-inset',
        '--surface-texture-anchor',
        '--surface-panel-veil',
        '--surface-raised-veil',
        '--surface-inset-veil',
        '--color-edge-highlight',
        '--asset-ui-material-page',
        '--asset-ui-material-dark',
        '--asset-ui-material-panel',
        '--asset-ui-material-raised',
        '--asset-ui-material-inset',
        '--asset-ui-bronze-edge',
        '--asset-ui-panel-rim',
        '--asset-ui-divider-bronze',
        '--asset-ui-noise',
        '--asset-panel-grain',
        '--asset-inset-grain',
        '--shadow-inset',
        '--shadow-raised',
        '--shadow-floating',
        '--shadow-artwork',
      ]),
    )
    expect(tokens).not.toMatch(/--color-border-[a-z]+:\s*var\(--color-gold-strong\)/)
    expect(tokens).not.toMatch(/--color-border-[a-z]+:\s*var\(--color-gold-highlight\)/)
    expect(tokens).toMatch(/\[data-ui-mode='compact'\][\s\S]*--shadow-raised:\s*0 1px 2px/)
    expect(tokens).toMatch(/\[data-ui-mode='compact'\][\s\S]*--shadow-floating:\s*0 2px 4px/)
    expect(tokens).toMatch(/\[data-ui-mode='compact'\][\s\S]*--shadow-artwork:\s*none/)
  })

  it('declares the iconography contract', () => {
    expect(tokens).toMatch(/--icon-grid:\s*24/)
    expect(tokens).toMatch(/--icon-stroke:\s*1\.6/)
    expect(tokens).toMatch(/--icon-stroke-ornament:\s*1/)
    expect(tokens).toMatch(/--icon-size-md:\s*1rem/)
    expect(tokens).toMatch(/--icon-well-size:\s*1\.75rem/)
    expect(tokens).toMatch(/--ornament-color:\s*var\(--color-bronze-normal\)/)
    expect(tokens).toMatch(/--mark-selected:\s*var\(--surface-metal-selected\)/)
    expect(tokens).toMatch(/--icon-color:\s*var\(--color-text-secondary\)/)
    expect(tokens).toMatch(/--icon-color-disabled:\s*var\(--color-text-disabled\)/)
    expect(tokens).toMatch(/--icon-color-active:\s*var\(--color-gold-normal\)/)
  })

  it('does not raise tooltip above dropdown', () => {
    expect(tokens).toMatch(/--z-dropdown:\s*30/)
    expect(tokens).toMatch(/--z-tooltip:\s*20/)
    expect(tokens).toMatch(/--z-modal:\s*40/)
  })
})
