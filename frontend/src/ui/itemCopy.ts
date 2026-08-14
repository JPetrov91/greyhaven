import type { InventoryItemResponse, ItemComparisonResponse } from '../api/types'
import { formatRarity } from './formatRarity'
import { SLOT_LABELS } from './equipmentSlots'

export function comparisonLabel(verdict: ItemComparisonResponse['verdict']): string {
  if (verdict === 'UPGRADE') {
    return 'Upgrade'
  }
  if (verdict === 'DOWNGRADE') {
    return 'Downgrade'
  }
  if (verdict === 'MIXED') {
    return 'Mixed'
  }
  return 'Same'
}

export function verdictTone(
  verdict: ItemComparisonResponse['verdict'],
): 'upgrade' | 'downgrade' | 'mixed' | 'neutral' {
  if (verdict === 'UPGRADE') {
    return 'upgrade'
  }
  if (verdict === 'DOWNGRADE') {
    return 'downgrade'
  }
  if (verdict === 'MIXED') {
    return 'mixed'
  }
  return 'neutral'
}

export function itemStatsLine(item: InventoryItemResponse): string {
  const parts: string[] = []
  if (item.weaponDamage != null) {
    parts.push(`Damage ${item.weaponDamage}`)
  }
  if (item.armorValue != null) {
    parts.push(`Armor ${item.armorValue}`)
  }
  if (item.healAmount != null) {
    parts.push(`Heal ${item.healAmount}`)
  }
  if (parts.length === 0) {
    parts.push('No combat stats')
  }
  return parts.join(' · ')
}

export function itemAriaLabel(item: InventoryItemResponse): string {
  const parts = [
    item.displayName,
    formatRarity(item.rarity),
    item.type,
    `Qty ${item.quantity}`,
    itemStatsLine(item),
  ]
  if (item.equipped) {
    parts.push('Equipped')
  }
  if (item.listedQuantity > 0) {
    parts.push(`Listed ${item.listedQuantity}`)
  }
  if (item.legacy) {
    parts.push('Legacy')
  }
  if (item.equipmentSlot && !item.canEquip) {
    parts.push('Unusable')
  }
  if (item.equipmentSlot) {
    parts.push(SLOT_LABELS[item.equipmentSlot])
  }
  return parts.join(' · ')
}

export function comparisonClass(item: InventoryItemResponse): string {
  if (item.comparison == null) {
    return ''
  }
  return `comparison-${item.comparison.verdict.toLowerCase()}`
}

export function shouldShowItemComparison(item: InventoryItemResponse): boolean {
  if (item.comparison == null || item.equipped) {
    return false
  }
  return item.comparison.equippedItemId !== item.id
}
