// @vitest-environment jsdom

import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it } from 'vitest'
import type { EquipmentResponse, InventoryItemResponse } from '../api/types'
import { EquipmentLayout } from './EquipmentLayout'
import { EQUIPMENT_SLOTS } from './equipmentSlots'

afterEach(() => {
  cleanup()
})

const emptyEquipment: EquipmentResponse = {
  slots: {
    HEAD: null,
    CHEST: null,
    HANDS: null,
    LEGS: null,
    FEET: null,
    MAIN_HAND: null,
    OFF_HAND: null,
    AMULET: null,
    RING: null,
  },
}

function sword(): InventoryItemResponse {
  return {
    id: 'item-sword',
    definitionId: 'def-sword',
    code: 'RUSTY_SWORD',
    name: 'Rusty Sword',
    displayName: 'Rusty Sword',
    description: 'A blade',
    type: 'WEAPON',
    rarity: 'COMMON',
    quantity: 1,
    requiredLevel: 1,
    requiredStrength: 0,
    requiredAgility: 0,
    requiredEndurance: 0,
    requiredPerception: 0,
    baseValue: 5,
    equipped: true,
    canEquip: true,
    twoHanded: false,
    legacy: true,
    equipmentSlot: 'MAIN_HAND',
    weaponFamily: 'SWORD',
    armorCategory: null,
    usable: false,
    listedQuantity: 0,
    rolledWeaponDamage: 6,
    rolledArmorValue: null,
    weaponDamage: 6,
    armorValue: null,
    healAmount: null,
    affixes: [],
    comparison: null,
  }
}

describe('EquipmentLayout', () => {
  it('shows a distinct empty placeholder icon for every unequipped slot', () => {
    render(<EquipmentLayout includeSlotTestIds equipment={emptyEquipment} items={[]} />)

    expect(screen.getByTestId('equipped-weapon')).toHaveProperty('textContent', 'Empty')
    expect(screen.getByTestId('equipped-armor')).toHaveProperty('textContent', 'Empty')
    const icons = document.querySelectorAll('.equipment-slot-empty .equipment-slot-icon')
    expect(icons).toHaveLength(EQUIPMENT_SLOTS.length)
    const titles = Array.from(icons).map((icon) => icon.querySelector('title')?.textContent)
    expect(new Set(titles).size).toBe(EQUIPMENT_SLOTS.length)
  })

  it('hides the placeholder when a slot is filled', () => {
    render(
      <EquipmentLayout
        includeSlotTestIds
        equipment={{ ...emptyEquipment, slots: { ...emptyEquipment.slots, MAIN_HAND: 'item-sword' } }}
        items={[sword()]}
      />,
    )

    expect(screen.getByTestId('equipped-weapon')).toHaveProperty('textContent', 'Rusty Sword')
    const filled = document.querySelector('.equipment-slot-main_hand')
    expect(filled?.className).not.toMatch(/equipment-slot-empty/)
    expect(filled?.querySelector('.equipment-slot-icon')).toBeNull()
  })
})
