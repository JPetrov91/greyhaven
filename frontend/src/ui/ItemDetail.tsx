import type { InventoryItemResponse } from '../api/types'
import { SLOT_LABELS } from './equipmentSlots'
import { comparisonLabel, itemCombatStatRows, verdictTone } from './itemCopy'
import { ItemIcon } from './itemIcons'
import { RarityBadge } from './RarityBadge'
import { StatRow } from './StatRow'
import { StatusBadge } from './StatusBadge'

type Props = {
  item: InventoryItemResponse
  equippedName?: string | null
  showComparison?: boolean
  showIcon?: boolean
  valueLabel?: string
}

export function ItemDetail({
  item,
  equippedName = null,
  showComparison = true,
  showIcon = false,
  valueLabel = 'Vendor value',
}: Props) {
  const comparison = item.comparison

  return (
    <div className="item-detail">
      <header className="item-tooltip-header">
        {showIcon ? <ItemIcon item={item} className="item-icon item-icon-inspector" /> : null}
        <div className="item-detail-heading">
          <strong className={`item-name rarity-ink-${item.rarity.toLowerCase()}`}>{item.displayName}</strong>
          <RarityBadge rarity={item.rarity} />
        </div>
      </header>
      <p className="item-tooltip-meta">
        {item.type}
        {item.weaponFamily ? ` · ${item.weaponFamily}` : ''}
        {item.armorCategory ? ` · ${item.armorCategory}` : ''}
        {item.equipmentSlot ? ` · ${SLOT_LABELS[item.equipmentSlot]}` : ''}
        {item.twoHanded ? ' · Two-handed' : ''}
      </p>
      {showComparison && comparison ? (
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
        {itemCombatStatRows(item).map((row) => (
          <StatRow key={row.label} label={row.label} value={row.value} />
        ))}
        <StatRow label={valueLabel} value={item.baseValue} />
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
