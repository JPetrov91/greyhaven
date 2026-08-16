import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'

const root = join(dirname(fileURLToPath(import.meta.url)), '..')

function source(relative: string): string {
  return readFileSync(join(root, relative), 'utf8')
}

describe('production UI engine adoption', () => {
  it('routes game chrome through surface and type roles', () => {
    expect(source('components/GameLayout.tsx')).toContain('surface-page')
    expect(source('components/GameTopBar.tsx')).toContain('surface-raised')
    expect(source('components/GameTopBar.tsx')).toContain('IconButton')
    expect(source('components/GameTopBar.tsx')).toContain('CounterBadge')
    expect(source('components/GameLeftNav.tsx')).toContain('surface-base')
    expect(source('components/GameLeftNav.tsx')).toContain('type-section-heading')
  })

  it('uses form primitives instead of raw fields on live screens', () => {
    const inventory = source('components/InventoryPanel.tsx')
    const market = source('components/MarketPanel.tsx')
    const chat = source('components/ChatPanel.tsx')
    const arena = source('components/ArenaPanel.tsx')
    const sparring = source('components/SparringYardPanel.tsx')
    const mastery = source('components/MasteryPanel.tsx')
    const activity = source('components/ActivityPanel.tsx')

    expect(inventory).toContain('SearchInput')
    expect(inventory).toContain('<Select')
    expect(inventory).toContain('TextInput')
    expect(inventory).not.toMatch(/<input/)
    expect(inventory).not.toMatch(/<select/)

    expect(market).toContain('SearchInput')
    expect(market).toContain('<Select')
    expect(market).toContain('TextInput')
    expect(market).not.toMatch(/<input/)
    expect(market).not.toMatch(/<select/)

    expect(chat).toContain('TextInput')
    expect(chat).toContain('CompactDataRow')
    expect(chat).not.toMatch(/<input/)

    expect(arena).toContain('TextInput')
    expect(arena).toContain('<Select')
    expect(sparring).toContain('Field')
    expect(sparring).toContain('TextInput')
    expect(sparring).toContain('CompactDataRow')
    expect(sparring).toContain('Panel')
    expect(sparring).not.toMatch(/<select/)
    expect(mastery).toContain('<Select')
    expect(activity).toContain('Dropdown')
    expect(activity).toContain('Section')
  })

  it('uses specialized meters on character and combat', () => {
    expect(source('components/CharacterSummaryPanel.tsx')).toContain('HealthBar')
    expect(source('components/CharacterSummaryPanel.tsx')).toContain('StaminaBar')
    expect(source('components/CharacterSummaryPanel.tsx')).toContain('XPBar')
    expect(source('components/GameTopBar.tsx')).toContain('XPBar')
    expect(source('components/CombatStage.tsx')).toContain('HealthBar')
    expect(source('components/CombatStage.tsx')).toContain('StaminaBar')
  })
})
