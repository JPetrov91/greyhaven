import type { CharacterGender } from '../character/avatars'
import type { DesignEquipmentSlot } from './equipmentSlots'

const EMPTY_SLOT_ART: Record<DesignEquipmentSlot, string> = {
  HEAD: '/equipment/slots/head.webp',
  CHEST: '/equipment/slots/chest.webp',
  HANDS: '/equipment/slots/hands.webp',
  LEGS: '/equipment/slots/legs.webp',
  FEET: '/equipment/slots/feet.webp',
  MAIN_HAND: '/equipment/slots/main_hand.webp',
  OFF_HAND: '/equipment/slots/off_hand.webp',
  AMULET: '/equipment/slots/amulet.webp',
  RING: '/equipment/slots/ring.webp',
  SHOULDERS: '/equipment/slots/shoulders.webp',
  BELT: '/equipment/slots/belt.webp',
  EARRINGS: '/equipment/slots/earrings.webp',
  RING_II: '/equipment/slots/ring.webp',
  RING_III: '/equipment/slots/ring.webp',
}

export function emptySlotArtUrl(slot: DesignEquipmentSlot): string {
  return EMPTY_SLOT_ART[slot]
}

export function equipmentFigureUrl(gender?: CharacterGender | null): string {
  return gender === 'FEMALE' ? '/equipment/figure-female.webp' : '/equipment/figure-male.webp'
}
