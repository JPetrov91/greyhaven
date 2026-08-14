import type { EquipmentSlot } from '../api/types'
import { SLOT_LABELS } from './equipmentSlots'

type Props = {
  slot: EquipmentSlot
  className?: string
}

export function EquipmentSlotIcon({ slot, className = 'equipment-slot-icon' }: Props) {
  return (
    <svg className={className} viewBox="0 0 32 32" aria-hidden="true" focusable="false">
      <title>{SLOT_LABELS[slot]}</title>
      {slot === 'HEAD' ? (
        <>
          <path
            d="M8 14.5c0-5 3.4-8.2 8-8.2s8 3.2 8 8.2v3.2H8Z"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.7"
            strokeLinejoin="round"
          />
          <path d="M7.5 18.2h17" fill="none" stroke="currentColor" strokeWidth="1.7" />
          <path d="M11 18.2v4.6h10v-4.6" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinejoin="round" />
        </>
      ) : null}
      {slot === 'CHEST' ? (
        <>
          <path
            d="M10 9.2 16 7.5 22 9.2v4.4c0 7.2-2.8 10.4-6 11.6-3.2-1.2-6-4.4-6-11.6Z"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.7"
            strokeLinejoin="round"
          />
          <path d="M13.2 14.2h5.6" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
        </>
      ) : null}
      {slot === 'HANDS' ? (
        <>
          <path
            d="M11.2 8.5h3.2v7.2h-3.2Z"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.6"
            strokeLinejoin="round"
          />
          <path
            d="M15.2 10.2h2.8v5.5h-2.8"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.6"
            strokeLinejoin="round"
          />
          <path
            d="M9.2 14.4h12.2v8.2c0 1.4-1.2 2.4-2.6 2.4H11.8c-1.4 0-2.6-1-2.6-2.4Z"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.7"
            strokeLinejoin="round"
          />
        </>
      ) : null}
      {slot === 'LEGS' ? (
        <>
          <path
            d="M11 7.8h10l-1.2 6.2h-7.6Z"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.7"
            strokeLinejoin="round"
          />
          <path d="M12.4 14v10.4H9.8V14" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinejoin="round" />
          <path d="M19.6 14v10.4h2.6V14" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinejoin="round" />
        </>
      ) : null}
      {slot === 'FEET' ? (
        <>
          <path
            d="M8.5 13.2h9.4v6.6H8.5Z"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.7"
            strokeLinejoin="round"
          />
          <path
            d="M17.9 15.4h5.6c.8 0 1.4.8 1.4 1.7 0 1.6-1.4 2.7-3.2 2.7h-3.8"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.7"
            strokeLinejoin="round"
          />
        </>
      ) : null}
      {slot === 'MAIN_HAND' ? (
        <>
          <path d="M18.8 6.8 9.4 16.2l2.6 2.6 9.4-9.4Z" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinejoin="round" />
          <path d="M9.2 16.4 7.4 22.8 13.8 21" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinejoin="round" />
          <path d="M12.2 13.4 16.8 18" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
        </>
      ) : null}
      {slot === 'OFF_HAND' ? (
        <path
          d="M8.4 8.2h15.2v8.4c0 5.4-3.4 8.6-7.6 10-4.2-1.4-7.6-4.6-7.6-10Z"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.7"
          strokeLinejoin="round"
        />
      ) : null}
      {slot === 'AMULET' ? (
        <>
          <path d="M10.5 8.2c1.8 2.2 3.6 3.3 5.5 3.3s3.7-1.1 5.5-3.3" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
          <circle cx="16" cy="18.4" r="4.6" fill="none" stroke="currentColor" strokeWidth="1.7" />
          <circle cx="16" cy="18.4" r="1.4" fill="currentColor" />
        </>
      ) : null}
      {slot === 'RING' ? (
        <>
          <circle cx="16" cy="17.2" r="6.2" fill="none" stroke="currentColor" strokeWidth="1.8" />
          <path d="M13.4 11.4h5.2l-.8-3.2h-3.6Z" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round" />
        </>
      ) : null}
    </svg>
  )
}
