import type { InventoryItemResponse, ItemComparisonResponse } from '../api/types'
import { RarityBadge } from './RarityBadge'
import { SLOT_LABELS } from './equipmentSlots'
import { StatRow } from './StatRow'
import { StatusBadge } from './StatusBadge'

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

type Props = {
  item: InventoryItemResponse
  equippedName?: string | null
}

export function ItemTooltip({ item, equippedName = null }: Props) {
  const comparison = item.comparison

  return (
    <div className="item-tooltip">
      <header className="item-tooltip-header">
        <strong className={`item-name rarity-ink-${item.rarity.toLowerCase()}`}>{item.displayName}</strong>
        <RarityBadge rarity={item.rarity} />
      </header>
      <p className="item-tooltip-meta">
        {item.type}
        {item.weaponFamily ? ` · ${item.weaponFamily}` : ''}
        {item.armorCategory ? ` · ${item.armorCategory}` : ''}
        {item.equipmentSlot ? ` · ${SLOT_LABELS[item.equipmentSlot]}` : ''}
        {item.twoHanded ? ' · Two-handed' : ''}
      </p>
      {comparison ? (
        <div className="item-comparison" data-testid={`comparison-${item.code}`}>
          <p className="item-comparison-heading">
            <span>
              {SLOT_LABELS[comparison.slot]}: {equippedName ?? (comparison.equippedItemId ? 'Equipped' : 'Empty')}{' '}
              vs {item.displayName}
            </span>
            <StatusBadge tone={verdictTone(comparison.verdict)}>{comparisonLabel(comparison.verdict)}</StatusBadge>
          </p>
          <dl className="stat-list">
            {comparison.deltas.map((delta) => (
              <StatRow
                key={delta.stat}
                label={delta.stat}
                value={`${delta.equippedValue} → ${delta.candidateValue}`}
                delta={delta.delta}
              />
            ))}
          </dl>
        </div>
      ) : null}
      {item.description ? <p className="item-tooltip-desc">{item.description}</p> : null}
      <dl className="stat-list">
        {item.weaponDamage != null ? <StatRow label="Damage" value={item.weaponDamage} /> : null}
        {item.armorValue != null ? <StatRow label="Armor" value={item.armorValue} /> : null}
        {item.healAmount != null ? <StatRow label="Heal" value={item.healAmount} /> : null}
        <StatRow label="Vendor value" value={item.baseValue} />
      </dl>
      {item.affixes.length > 0 ? (
        <ul className="affix-list">
          {item.affixes.map((affix) => (
            <li key={`${affix.kind}-${affix.code}-${affix.magnitude}`}>
              {affix.displayName} ({affix.stat} {affix.magnitude})
            </li>
          ))}
        </ul>
      ) : null}
      <p className="item-tooltip-meta">
        Req L{item.requiredLevel} STR {item.requiredStrength} AGI {item.requiredAgility} END{' '}
        {item.requiredEndurance} PER {item.requiredPerception}
        {item.equipmentSlot && !item.canEquip ? ' · Unmet' : ''}
      </p>
      <p className="item-tooltip-meta">
        Qty {item.quantity}
        {item.listedQuantity > 0 ? ` · Listed ${item.listedQuantity}` : ''}
        {item.legacy ? ' · Legacy' : ''}
      </p>
    </div>
  )
}
