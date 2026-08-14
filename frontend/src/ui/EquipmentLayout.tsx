import type { EquipmentResponse, EquipmentSlot, InventoryItemResponse } from '../api/types'
import { classNames } from './classNames'
import { EQUIPMENT_SLOTS, SLOT_LABELS, slotTestId } from './equipmentSlots'
import { RarityBadge } from './RarityBadge'

type Props = {
  equipment: EquipmentResponse
  items: InventoryItemResponse[]
  onSlotClick?: (slot: EquipmentSlot) => void
  compact?: boolean
  includeSlotTestIds?: boolean
  testId?: string
}

const DOLL_ORDER: EquipmentSlot[] = [
  'HEAD',
  'AMULET',
  'CHEST',
  'HANDS',
  'MAIN_HAND',
  'OFF_HAND',
  'RING',
  'LEGS',
  'FEET',
]

export function EquipmentLayout({
  equipment,
  items,
  onSlotClick,
  compact = false,
  includeSlotTestIds = false,
  testId = 'equipment-summary',
}: Props) {
  const slots = compact ? EQUIPMENT_SLOTS : DOLL_ORDER

  return (
    <div
      className={classNames('equipment-layout', compact ? 'equipment-layout-compact' : 'equipment-layout-doll')}
      data-testid={testId}
    >
      {!compact ? <div className="paper-doll-silhouette" aria-hidden="true" /> : null}
      {slots.map((slot) => {
        const equippedId = equipment.slots[slot]
        const equippedItem = items.find((item) => item.id === equippedId)
        const inner = (
          <>
            <span className="equipment-slot-label">{SLOT_LABELS[slot]}</span>
            <span className="equipment-slot-value">
              <span data-testid={includeSlotTestIds ? slotTestId(slot) : undefined}>
                {equippedItem?.displayName ?? 'Empty'}
              </span>
              {equippedItem ? <RarityBadge rarity={equippedItem.rarity} /> : null}
            </span>
          </>
        )

        if (onSlotClick) {
          return (
            <button
              key={slot}
              type="button"
              className={classNames('equipment-slot', `equipment-slot-${slot.toLowerCase()}`)}
              aria-label={`Filter inventory by ${SLOT_LABELS[slot]}`}
              onClick={() => onSlotClick(slot)}
            >
              {inner}
            </button>
          )
        }

        return (
          <div key={slot} className={classNames('equipment-slot', `equipment-slot-${slot.toLowerCase()}`)}>
            {inner}
          </div>
        )
      })}
    </div>
  )
}
