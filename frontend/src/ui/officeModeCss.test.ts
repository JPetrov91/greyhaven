import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'

const here = dirname(fileURLToPath(import.meta.url))

describe('office mode CSS', () => {
  it('keeps item status visible in compact mode', () => {
    const css = readFileSync(join(here, '../index.css'), 'utf8')
    expect(css).not.toMatch(/\[data-ui-mode='compact'\] \.item-card-badges \{\s*display:\s*none/)
    expect(css).not.toMatch(/\[data-ui-mode='compact'\] \.item-card \.inventory-item-meta \{\s*display:\s*none/)
    expect(css).toMatch(/max-width:\s*1200px/)
  })

  it('hides decorative dashboard art in compact mode', () => {
    const css = readFileSync(join(here, 'game-shell.css'), 'utf8')
    expect(css).toMatch(/\[data-ui-mode='compact'\].*location-hero-art/s)
    expect(css).toMatch(/display:\s*none/)
    expect(css).toMatch(/html\[data-ui-mode='compact'\] \.crafting-panel/)
    expect(css).toMatch(/html\[data-ui-mode='compact'\] \.pvp-panel/)
    expect(css).toMatch(/html\[data-ui-mode='compact'\] \.mastery-panel/)
  })
})
