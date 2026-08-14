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

const PRIMARY_STAT_LABELS = new Set(['Damage', 'Armor', 'Heal'])

export function formatCatalogLabel(value: string): string {
  return value
    .toLowerCase()
    .split(/[_\s]+/)
    .filter(Boolean)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

export function itemInspectMeta(item: InventoryItemResponse): {
  kicker: string
  slotLine: string | null
  itemLevel: string
} {
  const family = item.weaponFamily
    ? formatCatalogLabel(item.weaponFamily)
    : item.armorCategory
      ? formatCatalogLabel(item.armorCategory)
      : formatCatalogLabel(item.type)
  let slotLine: string | null = null
  if (item.twoHanded) {
    slotLine = item.weaponFamily ? `Two-Handed ${formatCatalogLabel(item.weaponFamily)}` : 'Two-Handed'
  } else if (item.equipmentSlot) {
    slotLine = SLOT_LABELS[item.equipmentSlot]
  }
  return {
    kicker: `${formatRarity(item.rarity)} ${family}`,
    slotLine,
    itemLevel: `Item Level ${item.requiredLevel}`,
  }
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

export function itemPrimaryStatRows(
  item: InventoryItemResponse,
): Array<{ label: string; value: number | string }> {
  return itemCombatStatRows(item).filter((row) => PRIMARY_STAT_LABELS.has(row.label))
}

export function itemSecondaryStatRows(
  item: InventoryItemResponse,
): Array<{ label: string; value: number | string }> {
  return itemCombatStatRows(item).filter((row) => !PRIMARY_STAT_LABELS.has(row.label))
}

export function itemRequirementRows(
  item: InventoryItemResponse,
): Array<{ label: string; value: string }> {
  const rows: Array<{ label: string; value: string }> = [
    { label: 'Required Level', value: String(item.requiredLevel) },
  ]
  if (item.requiredStrength > 0) {
    rows.push({ label: 'Required Strength', value: String(item.requiredStrength) })
  }
  if (item.requiredAgility > 0) {
    rows.push({ label: 'Required Agility', value: String(item.requiredAgility) })
  }
  if (item.requiredEndurance > 0) {
    rows.push({ label: 'Required Endurance', value: String(item.requiredEndurance) })
  }
  if (item.requiredPerception > 0) {
    rows.push({ label: 'Required Perception', value: String(item.requiredPerception) })
  }
  if (item.equipmentSlot) {
    rows.push({ label: 'Slot', value: item.twoHanded ? 'Two-Handed' : SLOT_LABELS[item.equipmentSlot] })
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
