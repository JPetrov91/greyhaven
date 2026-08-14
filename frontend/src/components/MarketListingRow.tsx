import { Button } from '../ui/Button'
import { RarityBadge } from '../ui/RarityBadge'
import type { MarketListingResponse } from '../api/types'

type Props = {
  listing: MarketListingResponse
  actionLabel: string
  actionTestId: string
  disabled: boolean
  onAction: () => void
}

export function MarketListingRow({ listing, actionLabel, actionTestId, disabled, onAction }: Props) {
  return (
    <li data-testid={`market-listing-${listing.itemCode}`}>
      <div className="inventory-item-main">
        <strong className={`item-name rarity-ink-${listing.rarity.toLowerCase()}`}>{listing.itemName}</strong>
        <RarityBadge rarity={listing.rarity} />
      </div>
      <p className="inventory-item-meta">
        {listing.itemType} · Qty {listing.quantity} · {listing.price} gold · Seller {listing.sellerName}
      </p>
      <div className="inventory-item-actions">
        <Button type="button" data-testid={actionTestId} disabled={disabled} onClick={onAction}>
          {actionLabel}
        </Button>
      </div>
    </li>
  )
}
