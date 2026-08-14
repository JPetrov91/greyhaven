import { describe, expect, it } from 'vitest'
import type { InventoryItemResponse } from '../api/types'
import { itemAriaLabel, shouldShowItemComparison } from './itemCopy'

function potion(): InventoryItemResponse {
  return {
    id: 'p1',
    definitionId: 'd1',
    code: 'HEALING_POTION',
    name: 'Healing Potion',
    displayName: 'Healing Potion',
    description: 'Heal',
    type: 'CONSUMABLE',
    rarity: 'COMMON',
    quantity: 2,
    requiredLevel: 1,
    requiredStrength: 0,
    requiredAgility: 0,
    requiredEndurance: 0,
    requiredPerception: 0,
    baseValue: 8,
    equipped: false,
    canEquip: false,
    twoHanded: false,
    legacy: false,
    equipmentSlot: null,
    weaponFamily: null,
    armorCategory: null,
    usable: true,
    listedQuantity: 0,
    rolledWeaponDamage: null,
    rolledArmorValue: null,
    weaponDamage: null,
    armorValue: null,
    healAmount: 40,
    affixes: [],
    comparison: null,
  }
}

describe('itemAriaLabel', () => {
  it('includes name, rarity, quantity and stats', () => {
    const label = itemAriaLabel(potion())
    expect(label).toContain('Healing Potion')
    expect(label).toContain('Common')
    expect(label).toContain('Qty 2')
    expect(label).toContain('Heal 40')
  })
})

describe('shouldShowItemComparison', () => {
  it('hides comparison when the item is already equipped', () => {
    expect(
      shouldShowItemComparison(
        potion(),
      ),
    ).toBe(false)
    expect(
      shouldShowItemComparison({
        ...potion(),
        type: 'WEAPON',
        equipmentSlot: 'MAIN_HAND',
        equipped: true,
        comparison: {
          slot: 'MAIN_HAND',
          equippedItemId: 'p1',
          verdict: 'SAME',
          deltas: [],
        },
      }),
    ).toBe(false)
    expect(
      shouldShowItemComparison({
        ...potion(),
        type: 'WEAPON',
        equipmentSlot: 'MAIN_HAND',
        equipped: false,
        comparison: {
          slot: 'MAIN_HAND',
          equippedItemId: 'other',
          verdict: 'UPGRADE',
          deltas: [],
        },
      }),
    ).toBe(true)
  })
})
