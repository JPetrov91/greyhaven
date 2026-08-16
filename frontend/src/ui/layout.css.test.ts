import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'

const layout = readFileSync(join(dirname(fileURLToPath(import.meta.url)), 'layout.css'), 'utf8')

describe('layout primitives', () => {
  it('defines section, header, body, and divider classes', () => {
    for (const name of [
      'panel-header',
      'panel-body',
      'ui-section',
      'ui-section-header',
      'ui-section-heading-row',
      'ui-section-body',
      'ui-divider',
    ]) {
      expect(layout).toContain(`.${name}`)
    }
  })

  it('keeps section frameless', () => {
    const sectionBlock = layout.match(/\.ui-section \{[\s\S]*?\n\}/)?.[0] ?? ''
    expect(sectionBlock).not.toMatch(/background/)
    expect(sectionBlock).not.toMatch(/border/)
    expect(sectionBlock).not.toMatch(/box-shadow/)
    expect(sectionBlock).not.toMatch(/surface-/)
  })

  it('keeps ornamental dividers opt-in', () => {
    expect(layout).toContain('.ui-divider-ornament-diamond')
    expect(layout).toContain('.ui-divider-ornament-bar')
    expect(layout).toContain('.ui-divider-ornament-bronze')
    expect(layout).toContain('var(--asset-ui-divider-bronze)')
    const defaultDivider = layout.match(/\.ui-divider \{[\s\S]*?\n\}/)?.[0] ?? ''
    expect(defaultDivider).not.toMatch(/ornament/)
    expect(defaultDivider).not.toMatch(/::before/)
  })

  it('avoids bright gold and glow on ornaments', () => {
    expect(layout).not.toMatch(/--color-gold-highlight/)
    expect(layout).not.toMatch(/--color-gold-strong/)
    expect(layout).not.toMatch(/0 0 \d+px/)
    expect(layout).not.toMatch(/backdrop-filter/)
  })
})
