import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'

const rows = readFileSync(join(dirname(fileURLToPath(import.meta.url)), 'rows.css'), 'utf8')

describe('row engine', () => {
  it('defines generic, activity, notification, and compact row classes', () => {
    for (const name of [
      'ui-row-list',
      'ui-row',
      'ui-row-icon',
      'ui-row-body',
      'ui-row-primary',
      'ui-row-secondary',
      'ui-row-meta',
      'ui-row-action',
      'ui-row-selected',
      'ui-row-interactive',
      'ui-row-tone-important',
      'ui-row-tone-secondary',
      'ui-activity-row',
      'ui-activity-normal',
      'ui-activity-system',
      'ui-activity-reward',
      'ui-activity-warning',
      'ui-activity-market',
      'ui-activity-pvp',
      'ui-activity-completed',
      'ui-notification-row',
      'ui-notification-unread',
      'ui-compact-row',
      'ui-row-has-portrait',
    ]) {
      expect(rows).toContain(`.${name}`)
    }
  })

  it('uses hairline ledgers instead of inset ticket plates', () => {
    expect(rows).toContain('var(--border-subtle)')
    expect(rows).toContain('var(--color-surface-selected)')
    expect(rows).toContain('var(--mark-selected)')
    expect(rows).toContain('var(--color-surface-interactive)')
    expect(rows).not.toContain('var(--color-surface-inset)')
    expect(rows).not.toContain('var(--shadow-inset)')
    expect(rows).not.toContain('var(--radius-2)')
    expect(rows).not.toMatch(/--radius-pill/)
    expect(rows).not.toMatch(/border-left:\s*2px/)
    expect(rows).not.toMatch(/--row-tint/)
  })

  it('keeps activity variants restrained to icon and type color', () => {
    expect(rows).toContain('var(--color-info)')
    expect(rows).toContain('var(--color-gold-normal)')
    expect(rows).toContain('var(--color-negative)')
    expect(rows).not.toMatch(/--color-gold-highlight/)
    expect(rows).not.toMatch(/--font-chrome/)
    expect(rows).not.toMatch(/--type-display/)
    expect(rows).not.toMatch(/0 0 \d+px/)
    expect(rows).not.toMatch(/backdrop-filter/)
    expect(rows).not.toMatch(/animation:/)
    expect(rows).not.toMatch(/@keyframes/)
  })

  it('maps copy to compact and metadata type roles', () => {
    expect(rows).toContain('var(--type-compact-family)')
    expect(rows).toContain('var(--type-meta-family)')
  })
})
