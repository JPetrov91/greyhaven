import type { InventoryItemResponse } from '../api/types'
import { classNames } from './classNames'
import { EquipmentSlotIcon } from './equipmentIcons'

type Props = {
  item: InventoryItemResponse
  className?: string
}

export function ItemIcon({ item, className = 'item-icon' }: Props) {
  const kind = item.type.toLowerCase()
  return (
    <span className={classNames('item-icon-face', `item-icon-face-${kind}`, className)}>
      <ItemGlyph item={item} />
    </span>
  )
}

function ItemGlyph({ item }: { item: InventoryItemResponse }) {
  if (item.equipmentSlot) {
    return <EquipmentSlotIcon slot={item.equipmentSlot} className="item-icon-svg" />
  }
  if (item.type === 'CONSUMABLE') {
    return (
      <svg className="item-icon-svg" viewBox="0 0 32 32" aria-hidden="true" focusable="false">
        <title>Consumable</title>
        <path
          d="M12.2 7.2h7.6l1.4 6.2c1.8 1.4 3 3.6 3 6.2 0 4.2-3.4 7.2-7.6 7.2s-7.6-3-7.6-7.2c0-2.6 1.2-4.8 3-6.2Z"
          fill="currentColor"
          fillOpacity="0.38"
          stroke="currentColor"
          strokeWidth="1.7"
          strokeLinejoin="round"
        />
        <path d="M13 13.4h6" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
      </svg>
    )
  }
  if (item.type === 'MATERIAL') {
    return (
      <svg className="item-icon-svg" viewBox="0 0 32 32" aria-hidden="true" focusable="false">
        <title>Material</title>
        <path
          d="M16 6.8 26 12.2v7.6L16 25.2 6 19.8v-7.6Z"
          fill="currentColor"
          fillOpacity="0.32"
          stroke="currentColor"
          strokeWidth="1.7"
          strokeLinejoin="round"
        />
        <path d="M16 25.2V16M6 12.2 16 16l10-3.8" fill="none" stroke="currentColor" strokeWidth="1.6" />
      </svg>
    )
  }
  return (
    <svg className="item-icon-svg" viewBox="0 0 32 32" aria-hidden="true" focusable="false">
      <title>Item</title>
      <path
        d="M8.5 12.2h15v12.4H8.5Z"
        fill="currentColor"
        fillOpacity="0.28"
        stroke="currentColor"
        strokeWidth="1.7"
        strokeLinejoin="round"
      />
      <path
        d="M11.2 12.2V9.8c0-2.6 2.1-4.2 4.8-4.2s4.8 1.6 4.8 4.2v2.4"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.7"
      />
    </svg>
  )
}
