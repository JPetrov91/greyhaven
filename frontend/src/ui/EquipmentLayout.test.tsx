// @vitest-environment jsdom

import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it } from 'vitest'
import type { EquipmentResponse, InventoryItemResponse } from '../api/types'
import { EquipmentLayout } from './EquipmentLayout'
import { EQUIPMENT_SLOTS, FUTURE_EQUIPMENT_SLOTS } from './equipmentSlots'

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
  it('shows a distinct empty placeholder icon for every unequipped live slot', () => {
    render(<EquipmentLayout includeSlotTestIds equipment={emptyEquipment} items={[]} />)

    expect(screen.getByTestId('equipped-weapon')).toHaveProperty('textContent', 'Empty')
    expect(screen.getByTestId('equipped-armor')).toHaveProperty('textContent', 'Empty')
    const icons = document.querySelectorAll('.equipment-slot-empty .equipment-slot-empty-art')
    expect(icons).toHaveLength(EQUIPMENT_SLOTS.length)
    const sources = Array.from(icons).map((icon) => icon.getAttribute('src'))
    expect(new Set(sources).size).toBe(EQUIPMENT_SLOTS.length)
    expect(screen.getByTestId('equipment-character-figure').getAttribute('src')).toBe('/equipment/figure-male.webp')
  })

  it('uses the female figure when the character gender is female', () => {
    render(<EquipmentLayout includeSlotTestIds figureGender="FEMALE" equipment={emptyEquipment} items={[]} />)

    expect(screen.getByTestId('equipment-character-figure').getAttribute('src')).toBe('/equipment/figure-female.webp')
  })

  it('keeps the item icon when a slot is filled', () => {
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
    expect(filled?.querySelector('.item-icon-face')).not.toBeNull()
  })

  it('places the character stage between the slot columns on the full doll', () => {
    const { container } = render(
      <EquipmentLayout includeFutureSlots includeSlotTestIds equipment={emptyEquipment} items={[]} />,
    )

    const layout = container.querySelector('.equipment-layout-doll-full')
    expect(layout?.querySelector(':scope > .equipment-stage')).not.toBeNull()
    expect(layout?.querySelector(':scope > .equipment-slot-head')).not.toBeNull()
    expect(layout?.querySelector(':scope > .equipment-slot-main_hand')).not.toBeNull()
    expect(layout?.querySelector(':scope > .equipment-slot-shoulders')).not.toBeNull()
    expect(layout?.querySelector(':scope > .equipment-slot-feet')).not.toBeNull()
    expect(screen.getByTestId('equipment-character-figure')).toBeTruthy()
  })

  it('renders locked future slots on the full doll without assigning compact grid areas', () => {
    const { container } = render(
      <EquipmentLayout includeFutureSlots compact equipment={emptyEquipment} items={[]} />,
    )

    expect(container.querySelector('.equipment-layout-compact')).not.toBeNull()
    expect(container.querySelector('.equipment-layout-doll-full')).toBeNull()
    for (const slot of FUTURE_EQUIPMENT_SLOTS) {
      expect(screen.getByTestId(`equipment-slot-${slot}`)).toHaveProperty('disabled', false)
      expect(screen.getByTestId(`equipment-slot-${slot}`).className).toMatch(/equipment-slot-locked/)
    }
  })
})
