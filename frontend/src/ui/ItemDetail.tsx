import type { InventoryItemResponse } from '../api/types'
import { SLOT_LABELS } from './equipmentSlots'
import {
  comparisonLabel,
  itemCombatStatRows,
  itemInspectMeta,
  itemPrimaryStatRows,
  itemRequirementRows,
  itemSecondaryStatRows,
  verdictTone,
} from './itemCopy'
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
  variant?: 'default' | 'market' | 'equipment'
  hideValue?: boolean
  showQuantity?: boolean
}

export function ItemDetail({
  item,
  equippedName = null,
  showComparison = true,
  showIcon = false,
  valueLabel = 'Vendor value',
  variant = 'default',
  hideValue = false,
  showQuantity = true,
}: Props) {
  if (variant === 'market' || variant === 'equipment') {
    return (
      <InspectItemDetail
        item={item}
        equippedName={equippedName}
        showComparison={showComparison}
        hideValue={hideValue}
        showQuantity={showQuantity}
        valueLabel={valueLabel}
        stacked={variant === 'equipment'}
        comparisonHeading={variant === 'equipment' ? 'Compare to Equipped' : 'Compare (Equipped)'}
      />
    )
  }

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
        <ComparisonBlock item={item} equippedName={equippedName} />
      ) : null}
      {item.description ? <p className="item-tooltip-desc">{item.description}</p> : null}
      <dl className="stat-list">
        {itemCombatStatRows(item).map((row) => (
          <StatRow key={row.label} label={row.label} value={row.value} />
        ))}
        {hideValue ? null : <StatRow label={valueLabel} value={item.baseValue} />}
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
      {showQuantity ? (
        <p className="item-tooltip-meta">
          Qty {item.quantity}
          {item.listedQuantity > 0 ? ` · Listed ${item.listedQuantity}` : ''}
          {item.legacy ? ' · Legacy' : ''}
        </p>
      ) : null}
    </div>
  )
}

function InspectItemDetail({
  item,
  equippedName,
  showComparison,
  hideValue,
  showQuantity,
  valueLabel,
  stacked,
  comparisonHeading,
}: {
  item: InventoryItemResponse
  equippedName: string | null
  showComparison: boolean
  hideValue: boolean
  showQuantity: boolean
  valueLabel: string
  stacked: boolean
  comparisonHeading: string
}) {
  const meta = itemInspectMeta(item)
  const primary = itemPrimaryStatRows(item)
  const secondary = itemSecondaryStatRows(item)
  const requirements = itemRequirementRows(item)
  const comparison = item.comparison

  const rarityInk = `rarity-ink-${item.rarity.toLowerCase()}`

  return (
    <div className={stacked ? 'item-detail item-detail-inspect item-detail-equipment' : 'item-detail item-detail-inspect item-detail-market'}>
      <header className="item-tooltip-header">
        <span className={`item-icon-frame rarity-frame-${item.rarity.toLowerCase()}`}>
          <ItemIcon item={item} className="item-icon item-icon-inspector" />
        </span>
        <div className="item-detail-heading">
          <div className="item-detail-title-row">
            <strong className={`item-name ${rarityInk}`}>{item.displayName}</strong>
            <RarityBadge rarity={item.rarity} />
          </div>
          <p className={`item-detail-kicker ${rarityInk}`}>{meta.kicker}</p>
          {meta.slotLine ? <p className="item-detail-slot">{meta.slotLine}</p> : null}
          <p className="item-detail-level">{meta.itemLevel}</p>
          {item.equipped ? <p className="item-detail-equipped">Equipped</p> : null}
        </div>
      </header>
      {item.description ? <p className="item-tooltip-desc">{item.description}</p> : null}
      {primary.length > 0 ? (
        <div className="item-primary-stats">
          {primary.map((row) => (
            <p key={row.label} className="item-primary-stat">
              <span className="item-primary-value">{row.value}</span>
              <span className="item-primary-label">{row.label}</span>
            </p>
          ))}
        </div>
      ) : null}
      {secondary.length > 0 || item.affixes.length > 0 ? (
        <ul className="item-modifier-list">
          {secondary.map((row) => (
            <li key={row.label}>
              {row.value} {row.label}
            </li>
          ))}
          {item.affixes.map((affix) => (
            <li key={`${affix.kind}-${affix.code}-${affix.magnitude}`}>
              {affix.displayName} ({affix.stat} {affix.magnitude})
            </li>
          ))}
        </ul>
      ) : null}
      <section className="item-requirements" aria-label="Requirements">
        <h4 className="item-section-label">Requirements</h4>
        <dl className="stat-list item-requirement-list">
          {requirements.map((row) => (
            <StatRow key={row.label} label={row.label} value={row.value} />
          ))}
          {item.equipmentSlot && !item.canEquip ? <StatRow label="Requirements" value="Unmet" /> : null}
          {showQuantity ? (
            <StatRow
              label="Quantity"
              value={
                item.listedQuantity > 0 ? `${item.quantity} · Listed ${item.listedQuantity}` : String(item.quantity)
              }
            />
          ) : null}
          {item.legacy ? <StatRow label="Legacy" value="Yes" /> : null}
          {hideValue ? null : <StatRow label={valueLabel} value={item.baseValue} />}
        </dl>
      </section>
      {showComparison && comparison ? (
        <ComparisonBlock item={item} equippedName={equippedName} heading={comparisonHeading} />
      ) : null}
    </div>
  )
}

function ComparisonBlock({
  item,
  equippedName,
  heading,
}: {
  item: InventoryItemResponse
  equippedName: string | null
  heading?: string
}) {
  const comparison = item.comparison
  if (!comparison) {
    return null
  }
  return (
    <div className="item-comparison" data-testid={`comparison-${item.code}`}>
      {heading ? <h4 className="item-section-label">{heading}</h4> : null}
      <p className="item-comparison-heading">
        <span>
          {SLOT_LABELS[comparison.slot]}: {equippedName ?? (comparison.equippedItemId ? 'Equipped' : 'Empty')} vs{' '}
          {item.displayName}
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
  )
}
