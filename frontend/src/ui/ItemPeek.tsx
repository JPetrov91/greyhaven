import type { InventoryItemResponse } from '../api/types'
import { itemCombatStatRows, itemInspectMeta, itemStatsLine } from './itemCopy'
import { RarityBadge } from './RarityBadge'

type Props = {
  item: InventoryItemResponse
  compact?: boolean
}

export function ItemPeek({ item, compact = false }: Props) {
  if (compact) {
    const meta = itemInspectMeta(item)
    return (
      <div className="item-peek item-peek-compact">
        <strong className={`item-name rarity-ink-${item.rarity.toLowerCase()}`}>{item.displayName}</strong>
        <p className="item-peek-meta">{meta.kicker}</p>
        {itemCombatStatRows(item).map((row) => (
          <p key={row.label} className="item-peek-row">
            <span>{row.value}</span> {row.label}
          </p>
        ))}
      </div>
    )
  }

  return (
    <div className="item-peek">
      <strong className={`item-name rarity-ink-${item.rarity.toLowerCase()}`}>{item.displayName}</strong>
      <RarityBadge rarity={item.rarity} />
      <p className="item-peek-stats">{itemStatsLine(item)}</p>
    </div>
  )
}
