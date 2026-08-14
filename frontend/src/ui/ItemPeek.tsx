import type { InventoryItemResponse } from '../api/types'
import { itemStatsLine } from './itemCopy'
import { RarityBadge } from './RarityBadge'

type Props = {
  item: InventoryItemResponse
}

export function ItemPeek({ item }: Props) {
  return (
    <div className="item-peek">
      <strong className={`item-name rarity-ink-${item.rarity.toLowerCase()}`}>{item.displayName}</strong>
      <RarityBadge rarity={item.rarity} />
      <p className="item-peek-stats">{itemStatsLine(item)}</p>
    </div>
  )
}
