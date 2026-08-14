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

export type FutureEquipmentSlot = 'SHOULDERS' | 'BELT' | 'EARRINGS' | 'RING_II' | 'RING_III'

export type DesignEquipmentSlot = EquipmentSlot | FutureEquipmentSlot

export const FUTURE_EQUIPMENT_SLOTS: FutureEquipmentSlot[] = [
  'SHOULDERS',
  'BELT',
  'EARRINGS',
  'RING_II',
  'RING_III',
]

export const DOLL_SLOT_ORDER: DesignEquipmentSlot[] = [
  'HEAD',
  'AMULET',
  'CHEST',
  'HANDS',
  'MAIN_HAND',
  'OFF_HAND',
  'RING',
  'LEGS',
  'FEET',
  'SHOULDERS',
  'BELT',
  'EARRINGS',
  'RING_II',
  'RING_III',
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

export const DESIGN_SLOT_LABELS: Record<DesignEquipmentSlot, string> = {
  ...SLOT_LABELS,
  RING: 'Ring I',
  SHOULDERS: 'Shoulders',
  BELT: 'Belt',
  EARRINGS: 'Earrings',
  RING_II: 'Ring II',
  RING_III: 'Ring III',
}

export function isLiveEquipmentSlot(slot: DesignEquipmentSlot): slot is EquipmentSlot {
  return (EQUIPMENT_SLOTS as string[]).includes(slot)
}

export function designSlotClass(slot: DesignEquipmentSlot): string {
  return `equipment-slot-${slot.toLowerCase()}`
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
