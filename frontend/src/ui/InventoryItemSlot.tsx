import { useState, type KeyboardEvent } from 'react'
import type { InventoryItemResponse } from '../api/types'
import { classNames } from './classNames'
import { comparisonClass, itemAriaLabel } from './itemCopy'
import { ItemIcon } from './itemIcons'
import { ItemPeek } from './ItemPeek'
import { Tooltip } from './Tooltip'

type Props = {
  item: InventoryItemResponse
  selected: boolean
  onSelect: () => void
}

export function InventoryItemSlot({ item, selected, onSelect }: Props) {
  const [hovered, setHovered] = useState(false)
  const [focused, setFocused] = useState(false)
  const open = !selected && (hovered || focused)

  function handleKeyDown(event: KeyboardEvent<HTMLLIElement>) {
    if (event.key === 'Escape' && selected) {
      event.stopPropagation()
    }
  }

  return (
    <li
      data-testid={`inventory-item-${item.code}`}
      className={classNames(
        'inventory-slot',
        `inventory-slot-${item.rarity.toLowerCase()}`,
        selected && 'inventory-slot-selected',
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
      <Tooltip content={<ItemPeek item={item} />} open={open}>
        <button
          type="button"
          className="inventory-slot-select"
          aria-label={itemAriaLabel(item)}
          aria-pressed={selected}
          onClick={onSelect}
        >
          <ItemIcon item={item} />
          <span className="visually-hidden">{itemAriaLabel(item)}</span>
          {item.quantity > 1 ? <span className="inventory-slot-qty">{item.quantity}</span> : null}
          {item.equipped ? (
            <span className="inventory-slot-flag" title="Equipped">
              Eq
            </span>
          ) : null}
          {item.listedQuantity > 0 ? (
            <span className="inventory-slot-listed" title="Listed">
              L
            </span>
          ) : null}
        </button>
      </Tooltip>
    </li>
  )
}

export function InventoryEmptySlot() {
  return <li className="inventory-slot inventory-slot-empty" data-testid="inventory-empty-slot" aria-hidden="true" />
}
