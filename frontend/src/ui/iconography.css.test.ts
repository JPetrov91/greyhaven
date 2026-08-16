import { readdirSync, readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'
import { ORNAMENT_NAMES } from './iconography'

const here = dirname(fileURLToPath(import.meta.url))
const css = readFileSync(join(here, 'iconography.css'), 'utf8')

describe('iconography engine', () => {
  it('defines icon sizes, wells, marks, and the four ornaments', () => {
    for (const name of [
      'ui-icon',
      'ui-icon-sm',
      'ui-icon-md',
      'ui-icon-lg',
      'ui-icon-xl',
      'ui-icon-art',
      'ui-icon-disabled',
      'ui-icon-active',
      'ui-icon-well',
      'ui-icon-well-lg',
      'ui-icon-well-active',
      'ui-mark-selected',
      'ui-nav-selected',
      'ui-mark-frame',
      'ui-ornament',
      'ui-ornament-divider',
      'ui-ornament-corner',
      'ui-ornament-diamond',
      'ui-ornament-accent',
    ]) {
      expect(css).toContain(`.${name}`)
    }
  })

  it('uses icon tokens, asset-pack ornaments, and keeps marks dim', () => {
    expect(css).toContain('var(--icon-size-md)')
    expect(css).toContain('var(--icon-well-size)')
    expect(css).toContain('var(--icon-color)')
    expect(css).toContain('var(--icon-color-disabled)')
    expect(css).toContain('var(--icon-color-active)')
    expect(css).toContain('var(--icon-disabled-opacity)')
    expect(css).toContain('var(--ornament-color)')
    expect(css).toContain('var(--asset-section-divider)')
    expect(css).toContain('var(--asset-corner-accent)')
    expect(css).toContain('var(--asset-small-diamond)')
    expect(css).toContain('var(--asset-tiny-pip)')
    expect(css).toContain('var(--mark-selected)')
    expect(css).toContain('var(--nav-selected-plate)')
    expect(css).toContain('var(--nav-selected-fill)')
    expect(css).toContain('var(--nav-selected-rail)')
    expect(css).toContain('var(--nav-selected-shadow)')
    expect(css).not.toMatch(/--color-gold-highlight/)
    expect(css).not.toMatch(/--color-gold-strong/)
    expect(css).not.toMatch(/0 0 \d+px/)
    expect(css).not.toMatch(/backdrop-filter/)
    expect(css).not.toMatch(/--radius-pill/)
  })

  it('keeps the ornament pack to four SVG files', () => {
    const files = readdirSync(join(here, 'ornaments')).filter((name) => name.endsWith('.svg')).sort()
    expect(files).toEqual([...ORNAMENT_NAMES].map((name) => `${name}.svg`).sort())
  })
})
