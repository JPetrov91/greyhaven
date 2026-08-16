import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'

const forms = readFileSync(join(dirname(fileURLToPath(import.meta.url)), 'forms.css'), 'utf8')

describe('form engine', () => {
  it('defines control, search, select, dropdown, and floating classes', () => {
    for (const name of [
      'ui-control',
      'ui-input',
      'ui-textarea',
      'ui-select',
      'ui-search',
      'ui-dropdown',
      'ui-control-error',
      'ui-floating',
      'tooltip-panel',
      'tooltip-panel-compact',
      'tooltip-panel-peek',
      'tooltip-panel-inspector',
      'tooltip-ledger',
    ]) {
      expect(forms).toContain(`.${name}`)
    }
  })

  it('defines interaction states including error and focus-visible', () => {
    expect(forms).toContain(':hover')
    expect(forms).toContain(':focus-visible')
    expect(forms).toContain(':disabled')
    expect(forms).toContain('ui-control-error')
    expect(forms).toContain('aria-invalid')
    expect(forms).toContain('var(--focus-ring)')
    expect(forms).toContain('::placeholder')
    expect(forms).toContain('var(--color-edge-selected-highlight)')
    expect(forms).not.toContain('border: var(--border-interactive)')
    expect(forms).not.toContain('border: var(--border-selected)')
    expect(forms).toContain('%236a5340')
  })

  it('uses compact UI type and tight radius', () => {
    expect(forms).toContain('var(--type-compact-family)')
    expect(forms).toContain('var(--radius-2)')
    expect(forms).not.toMatch(/--font-chrome/)
    expect(forms).not.toMatch(/--type-display/)
  })

  it('replaces browser scrollbar chrome', () => {
    expect(forms).toContain('scrollbar-width: thin')
    expect(forms).toContain('::-webkit-scrollbar')
    expect(forms).toContain('var(--scrollbar-thumb)')
    expect(forms).toContain('appearance: none')
  })

  it('avoids glow, glass, and highlight-gold edges', () => {
    expect(forms).not.toMatch(/backdrop-filter/)
    expect(forms).not.toMatch(/--color-gold-highlight/)
    expect(forms).not.toMatch(/0 0 \d+px/)
  })
})
