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

export function itemCombatStatRows(
  item: InventoryItemResponse,
): Array<{ label: string; value: number | string }> {
  const rows: Array<{ label: string; value: number | string }> = []
  if (item.weaponDamage != null) {
    rows.push({ label: 'Damage', value: item.weaponDamage })
  }
  if (item.armorValue != null) {
    rows.push({ label: 'Armor', value: item.armorValue })
  }
  if (item.healAmount != null) {
    rows.push({ label: 'Heal', value: item.healAmount })
  }
  if (item.accuracy) {
    rows.push({ label: 'Accuracy', value: `+${item.accuracy}` })
  }
  if (item.criticalChance) {
    rows.push({ label: 'Crit', value: `+${item.criticalChance}` })
  }
  if (item.dodge) {
    rows.push({ label: 'Dodge', value: `+${item.dodge}` })
  }
  if (item.strength) {
    rows.push({ label: 'Strength', value: `+${item.strength}` })
  }
  if (item.agility) {
    rows.push({ label: 'Agility', value: `+${item.agility}` })
  }
  if (item.endurance) {
    rows.push({ label: 'Endurance', value: `+${item.endurance}` })
  }
  if (item.perception) {
    rows.push({ label: 'Perception', value: `+${item.perception}` })
  }
  if (item.staminaCostReduction) {
    rows.push({ label: 'Stamina Cost', value: `-${item.staminaCostReduction}` })
  }
  return rows
}

export function itemStatsLine(item: InventoryItemResponse): string {
  const parts = itemCombatStatRows(item).map((row) => `${row.label} ${row.value}`)
  if (parts.length === 0) {
    return 'No combat stats'
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
