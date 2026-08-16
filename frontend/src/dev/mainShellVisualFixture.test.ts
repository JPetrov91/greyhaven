import { describe, expect, it } from 'vitest'
import {
  mainShellActivity,
  mainShellCharacter,
  mainShellChatMessages,
  mainShellClaimable,
  mainShellCombat,
  mainShellCombatLocation,
  mainShellDestinations,
  mainShellExpeditions,
  mainShellInventory,
  mainShellLocation,
  mainShellNearby,
  mainShellNotifications,
  mainShellObjectives,
} from './mainShellVisualFixture'

describe('main shell visual fixture', () => {
  it('fills the home-shell workload without live player state', () => {
    expect(mainShellCharacter.name).toBe('Artino')
    expect(mainShellCharacter.level).toBe(47)
    expect(mainShellLocation.name).toBe('The Trade District')
    expect(mainShellExpeditions).toHaveLength(2)
    expect(mainShellObjectives.map((item) => `${item.current}/${item.required}`)).toEqual(['2/3', '0/2', '1/1'])
    expect(mainShellActivity.length).toBeGreaterThanOrEqual(5)
    expect(mainShellClaimable.length).toBeGreaterThanOrEqual(2)
    expect(mainShellNotifications.length).toBeGreaterThanOrEqual(3)
    expect(mainShellChatMessages.length).toBeGreaterThanOrEqual(8)
    expect(Object.values(mainShellInventory.equipment.slots).every(Boolean)).toBe(true)
    expect(mainShellInventory.items.some((entry) => entry.code === 'STEEL_LONGSWORD' && !entry.equipped)).toBe(true)
    expect(mainShellInventory.items.some((entry) => entry.type === 'CONSUMABLE')).toBe(true)
    expect(mainShellDestinations.map((entry) => entry.code)).toEqual(['TAVERN', 'NORTH_ROAD', 'ARENA', 'ANCIENT_RUINS'])
    expect(mainShellNearby).toHaveLength(3)
    expect(mainShellCombatLocation.code).toBe('NORTH_ROAD')
    expect(mainShellCombat.monster.code).toBe('BANDIT_VETERAN')
    expect(mainShellCombat.actionPreviews?.some((preview) => preview.action === 'RETREAT')).toBe(true)
  })
})
