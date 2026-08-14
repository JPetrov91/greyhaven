import type { ItemType, MarketListingResponse } from '../api/types'
import { Button } from '../ui/Button'
import { classNames } from '../ui/classNames'
import { ItemIcon, type ItemIconSource } from '../ui/itemIcons'
import { RarityBadge } from '../ui/RarityBadge'

type Props = {
  listing: MarketListingResponse
  selected: boolean
  actionLabel: string
  actionTestId: string
  disabled: boolean
  disabledReason?: string
  onSelect: () => void
  onAction: () => void
}

export function listingIconSource(itemType: ItemType): ItemIconSource {
  if (itemType === 'WEAPON') {
    return { type: itemType, equipmentSlot: 'MAIN_HAND' }
  }
  if (itemType === 'ARMOR') {
    return { type: itemType, equipmentSlot: 'CHEST' }
  }
  if (itemType === 'ACCESSORY') {
    return { type: itemType, equipmentSlot: 'AMULET' }
  }
  return { type: itemType }
}

export function formatItemType(itemType: ItemType): string {
  return itemType.charAt(0) + itemType.slice(1).toLowerCase()
}

export function MarketListingRow({
  listing,
  selected,
  actionLabel,
  actionTestId,
  disabled,
  disabledReason,
  onSelect,
  onAction,
}: Props) {
  return (
    <tr
      data-testid={`market-listing-${listing.itemCode}`}
      className={classNames('market-row', selected && 'market-row-selected')}
      aria-selected={selected}
    >
      <td>
        <button type="button" className="market-row-select" onClick={onSelect}>
          <ItemIcon item={listingIconSource(listing.itemType)} className="item-icon item-icon-row" />
          <span className={`item-name rarity-ink-${listing.rarity.toLowerCase()}`}>{listing.itemName}</span>
        </button>
      </td>
      <td>
        <RarityBadge rarity={listing.rarity} />
      </td>
      <td>{formatItemType(listing.itemType)}</td>
      <td>{listing.quantity}</td>
      <td className="market-col-locked" title="Coming later">
        —
      </td>
      <td className="market-col-locked" title="Coming later">
        —
      </td>
      <td>{listing.price} gold</td>
      <td>Seller {listing.sellerName}</td>
      <td className="market-col-locked" title="Coming later">
        —
      </td>
      <td>
        <Button
          type="button"
          data-testid={actionTestId}
          disabled={disabled}
          title={disabled ? disabledReason : undefined}
          onClick={(event) => {
            event.stopPropagation()
            onSelect()
            onAction()
          }}
        >
          {actionLabel}
        </Button>
      </td>
    </tr>
  )
}
