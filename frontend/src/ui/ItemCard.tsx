import { useState, type KeyboardEvent, type ReactNode } from 'react'
import type { InventoryItemResponse } from '../api/types'
import { Badge } from './Badge'
import { classNames } from './classNames'
import { comparisonClass, comparisonLabel, itemStatsLine } from './itemCopy'
import { ItemTooltip, verdictTone } from './ItemTooltip'
import { RarityBadge } from './RarityBadge'
import { StatusBadge } from './StatusBadge'
import { Tooltip } from './Tooltip'

type Props = {
  item: InventoryItemResponse
  selected: boolean
  onSelect: () => void
  actions?: ReactNode
  equippedName?: string | null
  pinTooltip?: boolean
}

export function ItemCard({ item, selected, onSelect, actions, equippedName = null, pinTooltip = true }: Props) {
  const [hovered, setHovered] = useState(false)
  const [focused, setFocused] = useState(false)
  const open = (selected && pinTooltip) || hovered || focused
  const comparison = item.comparison

  function handleKeyDown(event: KeyboardEvent<HTMLLIElement>) {
    if (event.key === 'Escape' && selected) {
      event.stopPropagation()
      onSelect()
    }
  }

  return (
    <li
      data-testid={`inventory-item-${item.code}`}
      className={classNames(
        'item-card',
        item.equipped && 'inventory-item-equipped',
        item.equipmentSlot && !item.canEquip && 'inventory-item-unusable',
        comparisonClass(item),
      )}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      onFocus={() => setFocused(true)}
      onBlur={(event) => {
        if (!event.currentTarget.contains(event.relatedTarget as Node | null)) {
          setFocused(false)
        }
      }}
      onKeyDown={handleKeyDown}
    >
      <Tooltip content={<ItemTooltip item={item} equippedName={equippedName} />} open={open} pinned={selected && pinTooltip}>
        <button type="button" className="inventory-item-select" onClick={onSelect}>
          <div className="inventory-item-main">
            <strong className={`item-name rarity-ink-${item.rarity.toLowerCase()}`}>{item.displayName}</strong>
            <RarityBadge rarity={item.rarity} />
            {comparison && !item.equipped ? (
              <StatusBadge tone={verdictTone(comparison.verdict)}>{comparisonLabel(comparison.verdict)}</StatusBadge>
            ) : null}
          </div>
          <p className="inventory-item-meta">
            {item.type}
            {` · Qty ${item.quantity}`}
            {item.equipped ? ' · Equipped' : ''}
            {item.listedQuantity > 0 ? ` · Listed ${item.listedQuantity}` : ''}
            {item.legacy ? ' · Legacy' : ''}
            {item.equipmentSlot && !item.canEquip ? ' · Unusable' : ''}
          </p>
            <p className="inventory-item-stats">{itemStatsLine(item)}</p>
        </button>
      </Tooltip>
      <div className="item-card-badges">
        {item.equipped ? <Badge>Equipped</Badge> : null}
        {item.listedQuantity > 0 ? <Badge tone="accent">Listed</Badge> : null}
        {item.twoHanded ? <Badge>Two-handed</Badge> : null}
        {item.legacy ? <Badge>Legacy</Badge> : null}
        {item.equipmentSlot && !item.canEquip ? <Badge tone="danger">Unusable</Badge> : null}
      </div>
      {actions ? <div className="inventory-item-actions">{actions}</div> : null}
    </li>
  )
}
