import { useState, type KeyboardEvent } from 'react'
import type { EquipmentResponse, EquipmentSlot, InventoryItemResponse } from '../api/types'
import { classNames } from './classNames'
import { EquipmentSlotIcon } from './equipmentIcons'
import {
  DESIGN_SLOT_LABELS,
  DOLL_SLOT_ORDER,
  EQUIPMENT_SLOTS,
  designSlotClass,
  isLiveEquipmentSlot,
  slotTestId,
  type DesignEquipmentSlot,
  type FutureEquipmentSlot,
} from './equipmentSlots'
import { ItemIcon } from './itemIcons'
import { ItemPeek } from './ItemPeek'
import { itemStatsLine } from './itemCopy'
import { Tooltip } from './Tooltip'

type Props = {
  equipment: EquipmentResponse
  items: InventoryItemResponse[]
  onLiveSlotClick?: (slot: EquipmentSlot) => void
  onFutureSlotClick?: (slot: FutureEquipmentSlot) => void
  selectedSlot?: DesignEquipmentSlot | null
  compact?: boolean
  includeSlotTestIds?: boolean
  includeFutureSlots?: boolean
  showStage?: boolean
  testId?: string
}

export function EquipmentLayout({
  equipment,
  items,
  onLiveSlotClick,
  onFutureSlotClick,
  selectedSlot = null,
  compact = false,
  includeSlotTestIds = false,
  includeFutureSlots = false,
  showStage = !compact,
  testId = 'equipment-summary',
}: Props) {
  const slots = includeFutureSlots ? DOLL_SLOT_ORDER : EQUIPMENT_SLOTS

  return (
    <div
      className={classNames(
        'equipment-layout',
        compact ? 'equipment-layout-compact' : 'equipment-layout-doll',
        !compact && includeFutureSlots && 'equipment-layout-doll-full',
        !compact && !includeFutureSlots && 'equipment-layout-doll-simple',
      )}
      data-testid={testId}
    >
      {showStage && !compact ? <CharacterStage /> : null}
      {slots.map((slot) => {
        const live = isLiveEquipmentSlot(slot)
        const equippedId = live ? equipment.slots[slot] : null
        const equippedItem = equippedId ? items.find((item) => item.id === equippedId) : undefined
        return (
          <EquipmentSlotControl
            key={slot}
            slot={slot}
            equippedItem={equippedItem}
            empty={!equippedItem}
            selected={selectedSlot === slot}
            compact={compact}
            includeSlotTestIds={includeSlotTestIds && live}
            live={live}
            onLiveClick={live ? onLiveSlotClick : undefined}
            onFutureClick={!live ? onFutureSlotClick : undefined}
          />
        )
      })}
    </div>
  )
}

type SlotControlProps = {
  slot: DesignEquipmentSlot
  equippedItem?: InventoryItemResponse
  empty: boolean
  selected: boolean
  compact: boolean
  includeSlotTestIds: boolean
  live: boolean
  onLiveClick?: (slot: EquipmentSlot) => void
  onFutureClick?: (slot: FutureEquipmentSlot) => void
}

function EquipmentSlotControl({
  slot,
  equippedItem,
  empty,
  selected,
  compact,
  includeSlotTestIds,
  live,
  onLiveClick,
  onFutureClick,
}: SlotControlProps) {
  const [hovered, setHovered] = useState(false)
  const [focused, setFocused] = useState(false)
  const label = DESIGN_SLOT_LABELS[slot]
  const peekOpen = Boolean(equippedItem) && (hovered || focused)
  const name = equippedItem?.displayName ?? 'Empty'
  const ariaLabel = `Select ${label} slot`

  const inner = (
    <>
      <span className="equipment-slot-label">{label}</span>
      <span className="equipment-slot-value">
        {equippedItem ? (
          <ItemIcon item={equippedItem} className="item-icon item-icon-slot" />
        ) : (
          <span className="equipment-slot-placeholder" aria-hidden="true">
            <EquipmentSlotIcon slot={slot} />
          </span>
        )}
        <span
          className="equipment-slot-name"
          data-testid={includeSlotTestIds && live && isLiveEquipmentSlot(slot) ? slotTestId(slot) : undefined}
          title={equippedItem?.displayName}
        >
          {name}
        </span>
        {equippedItem && compact ? <span className="equipment-slot-stat">{itemStatsLine(equippedItem)}</span> : null}
      </span>
    </>
  )

  const className = classNames(
    'equipment-slot',
    designSlotClass(slot),
    empty && 'equipment-slot-empty',
    selected && 'equipment-slot-selected',
    equippedItem && `equipment-slot-${equippedItem.rarity.toLowerCase()}`,
    !live && 'equipment-slot-locked',
  )

  function handleKeyDown(event: KeyboardEvent<HTMLElement>) {
    if (event.key === 'Escape') {
      event.stopPropagation()
    }
  }

  const trigger = !live ? (
    <button
      type="button"
      className={classNames(className, 'coming-later')}
      aria-label={`${ariaLabel}. Coming later`}
      title="Coming later"
      aria-pressed={selected}
      data-testid={`equipment-slot-${slot}`}
      onClick={() => onFutureClick?.(slot as FutureEquipmentSlot)}
      onKeyDown={handleKeyDown}
    >
      {inner}
      <span className="visually-hidden">Coming later</span>
    </button>
  ) : onLiveClick ? (
    <button
      type="button"
      className={className}
      aria-label={ariaLabel}
      aria-pressed={selected}
      onClick={() => {
        if (isLiveEquipmentSlot(slot)) {
          onLiveClick(slot)
        }
      }}
      onKeyDown={handleKeyDown}
    >
      {inner}
    </button>
  ) : (
    <div className={className}>{inner}</div>
  )

  return (
    <div
      className={classNames('equipment-slot-anchor', designSlotClass(slot))}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      onFocus={() => setFocused(true)}
      onBlur={(event) => {
        if (!event.currentTarget.contains(event.relatedTarget as Node | null)) {
          setFocused(false)
        }
      }}
    >
      {equippedItem ? (
        <Tooltip content={<ItemPeek item={equippedItem} />} open={peekOpen}>
          {trigger}
        </Tooltip>
      ) : (
        trigger
      )}
    </div>
  )
}

function CharacterStage() {
  return (
    <div className="equipment-stage" data-testid="equipment-character-stage">
      <svg className="equipment-stage-figure" viewBox="0 0 80 140" aria-hidden="true" focusable="false">
        <title>Character</title>
        <ellipse cx="40" cy="22" rx="11" ry="12" fill="none" stroke="currentColor" strokeWidth="1.8" />
        <path
          d="M24 42c4-8 32-8 36 0l4 18c-8 6-14 8-22 8s-14-2-22-8Z"
          fill="currentColor"
          fillOpacity="0.16"
          stroke="currentColor"
          strokeWidth="1.8"
          strokeLinejoin="round"
        />
        <path d="M18 58 10 88M62 58l8 30" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
        <path d="M32 68v48M48 68v48" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
        <path d="M28 116h12M40 116h12" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
      </svg>
      <p className="equipment-stage-caption muted">Appearance coming later</p>
    </div>
  )
}
