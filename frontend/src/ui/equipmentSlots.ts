import type { EquipmentSlot } from '../api/types'

export const EQUIPMENT_SLOTS: EquipmentSlot[] = [
  'HEAD',
  'CHEST',
  'HANDS',
  'LEGS',
  'FEET',
  'MAIN_HAND',
  'OFF_HAND',
  'AMULET',
  'RING',
]

export const SLOT_LABELS: Record<EquipmentSlot, string> = {
  HEAD: 'Head',
  CHEST: 'Chest',
  HANDS: 'Hands',
  LEGS: 'Legs',
  FEET: 'Feet',
  MAIN_HAND: 'Main Hand',
  OFF_HAND: 'Off Hand',
  AMULET: 'Amulet',
  RING: 'Ring',
}

export function slotTestId(slot: EquipmentSlot): string {
  if (slot === 'MAIN_HAND') {
    return 'equipped-weapon'
  }
  if (slot === 'CHEST') {
    return 'equipped-armor'
  }
  return `equipped-${slot}`
}
