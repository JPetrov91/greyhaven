import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'

const css = readFileSync(join(dirname(fileURLToPath(import.meta.url)), 'uiShowcase.css'), 'utf8')

describe('ui showcase css', () => {
  it('stays page-local and uses engine tokens for forced states', () => {
    expect(css).toContain('.ui-showcase')
    expect(css).toContain('.ui-shell')
    expect(css).toContain('.ui-shell-workspace')
    expect(css).toContain('.ui-shell-banner')
    expect(css).toContain('.ui-showcase-stage')
    expect(css).toContain('.ui-showcase-compose-base')
    expect(css).toContain('.is-force-hover')
    expect(css).toContain('.is-force-focus')
    expect(css).toContain('var(--focus-ring)')
    expect(css).toContain('.ui-showcase-example')
    expect(css).toContain('.ui-showcase-samples')
    expect(css).toContain('.ui-showcase-scroll-well')
    expect(css).toContain('.ui-showcase-marks-grid')
    expect(css).toContain('.ui-showcase-slot-well')
    expect(css).toContain('var(--color-edge-selected-highlight)')
    expect(css).not.toMatch(/backdrop-filter/)
    expect(css).not.toMatch(/--color-gold-highlight/)
  })
})
