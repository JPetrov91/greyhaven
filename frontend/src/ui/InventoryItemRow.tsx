import type { InventoryItemResponse } from '../api/types'
import { Badge } from './Badge'
import { classNames } from './classNames'
import { comparisonClass, itemAriaLabel, itemStatsLine } from './itemCopy'
import { ItemIcon } from './itemIcons'
import { RarityBadge } from './RarityBadge'

type Props = {
  item: InventoryItemResponse
  selected: boolean
  onSelect: () => void
}

export function InventoryItemRow({ item, selected, onSelect }: Props) {
  return (
    <li
      data-testid={`inventory-item-${item.code}`}
      className={classNames(
        'inventory-row',
        selected && 'inventory-row-selected',
        item.equipped && 'inventory-item-equipped',
        item.equipmentSlot && !item.canEquip && 'inventory-item-unusable',
        comparisonClass(item),
      )}
    >
      <button type="button" className="inventory-row-select" aria-pressed={selected} onClick={onSelect}>
        <ItemIcon item={item} className="item-icon item-icon-row" />
        <span className={`item-name rarity-ink-${item.rarity.toLowerCase()}`}>{item.displayName}</span>
        <RarityBadge rarity={item.rarity} />
        <span className="inventory-row-qty">Qty {item.quantity}</span>
        <span className="inventory-row-stats">{itemStatsLine(item)}</span>
        {item.equipped ? <Badge>Equipped</Badge> : null}
        <span className="visually-hidden">{itemAriaLabel(item)}</span>
      </button>
    </li>
  )
}
