import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import { fetchInventory } from '../api/inventory'
import {
  buyMarketListing,
  cancelMarketListing,
  createMarketListing,
  fetchMarketListings,
  fetchOwnMarketListings,
} from '../api/market'
import { fetchCurrentLocation } from '../api/world'
import type { InventoryItemResponse, ItemType, LocationAction, MarketListingResponse } from '../api/types'

const ITEM_TYPES: { value: ItemType | ''; label: string }[] = [
  { value: '', label: 'All types' },
  { value: 'WEAPON', label: 'Weapons' },
  { value: 'ARMOR', label: 'Armor' },
  { value: 'ACCESSORY', label: 'Accessories' },
  { value: 'CONSUMABLE', label: 'Consumables' },
  { value: 'MATERIAL', label: 'Materials' },
]

type Props = {
  onClose?: () => void
}

export function MarketPanel({ onClose }: Props) {
  const queryClient = useQueryClient()
  const [itemType, setItemType] = useState<ItemType | ''>('')
  const [selectedItemId, setSelectedItemId] = useState('')
  const [quantity, setQuantity] = useState(1)
  const [price, setPrice] = useState(10)
  const [error, setError] = useState<string | null>(null)

  const listingsQuery = useQuery({
    queryKey: ['market-listings', itemType],
    queryFn: () => fetchMarketListings(itemType),
    retry: false,
  })

  const ownListingsQuery = useQuery({
    queryKey: ['market-own-listings'],
    queryFn: fetchOwnMarketListings,
    retry: false,
  })

  const inventoryQuery = useQuery({
    queryKey: ['inventory'],
    queryFn: fetchInventory,
    retry: false,
  })

  const locationQuery = useQuery({
    queryKey: ['location'],
    queryFn: fetchCurrentLocation,
    retry: false,
  })

  const atMarket = (locationQuery.data?.actions ?? []).includes('BUY_ITEM' satisfies LocationAction)

  const listableItems = useMemo(
    () =>
      (inventoryQuery.data?.items ?? []).filter(
        (item) => !item.equipped && item.quantity - item.listedQuantity > 0,
      ),
    [inventoryQuery.data],
  )

  const selectedItem = listableItems.find((item) => item.id === selectedItemId) ?? null
  const maxQuantity = selectedItem ? selectedItem.quantity - selectedItem.listedQuantity : 1

  async function refreshAfterChange() {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['market-listings'] }),
      queryClient.invalidateQueries({ queryKey: ['market-own-listings'] }),
      queryClient.invalidateQueries({ queryKey: ['inventory'] }),
      queryClient.invalidateQueries({ queryKey: ['character'] }),
      queryClient.invalidateQueries({ queryKey: ['activity'] }),
    ])
  }

  const createMutation = useMutation({
    mutationFn: () => createMarketListing(selectedItemId, quantity, price),
    onSuccess: async () => {
      setError(null)
      setSelectedItemId('')
      await refreshAfterChange()
    },
    onError: (cause) => {
      setError(cause instanceof ApiError ? cause.message : 'Unable to create listing.')
    },
  })

  const buyMutation = useMutation({
    mutationFn: buyMarketListing,
    onSuccess: async () => {
      setError(null)
      await refreshAfterChange()
    },
    onError: (cause) => {
      setError(cause instanceof ApiError ? cause.message : 'Unable to buy that listing.')
    },
  })

  const cancelMutation = useMutation({
    mutationFn: cancelMarketListing,
    onSuccess: async () => {
      setError(null)
      await refreshAfterChange()
    },
    onError: (cause) => {
      setError(cause instanceof ApiError ? cause.message : 'Unable to cancel that listing.')
    },
  })

  const listings = listingsQuery.data?.listings ?? []
  const listingsTruncated = listingsQuery.data?.truncated ?? false
  const ownListings = ownListingsQuery.data?.listings ?? []
  const busy = createMutation.isPending || buyMutation.isPending || cancelMutation.isPending
  const tradeDisabled = busy || !atMarket

  const loadError = listingsQuery.error
    ? listingsQuery.error instanceof ApiError
      ? listingsQuery.error.message
      : 'Unable to load marketplace listings.'
    : null
  const ownLoadError = ownListingsQuery.error
    ? ownListingsQuery.error instanceof ApiError
      ? ownListingsQuery.error.message
      : 'Unable to load your marketplace listings.'
    : null

  return (
    <section className="expedition-panel" data-testid="market-panel" aria-label="Marketplace">
      <div className="expedition-header">
        <h2>Marketplace</h2>
        {onClose ? (
          <button type="button" className="nav-button" data-testid="close-market" onClick={onClose}>
            Close
          </button>
        ) : null}
      </div>
      <p className="muted">Browse player listings, sell from your pack, and manage your own offers.</p>
      {locationQuery.data && !atMarket ? (
        <p className="muted" data-testid="market-travel-hint">
          Travel to the Market to buy, sell, or cancel listings.
        </p>
      ) : null}
      {error ? (
        <p className="form-error" role="alert" data-testid="market-error">
          {error}
        </p>
      ) : null}

      <section className="location-section" aria-labelledby="market-filter-heading">
        <h3 id="market-filter-heading">Filters</h3>
        <label className="muted">
          Item type{' '}
          <select
            data-testid="market-type-filter"
            value={itemType}
            onChange={(event) => setItemType(event.target.value as ItemType | '')}
          >
            {ITEM_TYPES.map((option) => (
              <option key={option.label} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </label>
      </section>

      <section className="location-section" aria-labelledby="market-listings-heading">
        <h3 id="market-listings-heading">Active listings</h3>
        {listingsQuery.isLoading ? (
          <p className="muted">Loading listings…</p>
        ) : loadError ? (
          <p className="form-error" role="alert" data-testid="market-load-error">
            {loadError}
          </p>
        ) : listings.length === 0 ? (
          <p className="muted" data-testid="market-listings-empty">
            No listings match this filter.
          </p>
        ) : (
          <>
            <ul className="inventory-list" data-testid="market-listings">
              {listings.map((listing) => (
                <ListingRow
                  key={listing.id}
                  listing={listing}
                  actionLabel="Buy"
                  actionTestId={`buy-listing-${listing.itemCode}`}
                  disabled={tradeDisabled || listing.ownListing}
                  onAction={() => buyMutation.mutate(listing.id)}
                />
              ))}
            </ul>
            {listingsTruncated ? (
              <p className="muted" data-testid="market-listings-truncated">
                Showing the {listings.length} newest listings.
              </p>
            ) : null}
          </>
        )}
      </section>

      <section className="location-section" aria-labelledby="create-listing-heading">
        <h3 id="create-listing-heading">Create listing</h3>
        {listableItems.length === 0 ? (
          <p className="muted">Unequip or obtain an item before listing it.</p>
        ) : (
          <form
            className="market-create-form"
            onSubmit={(event) => {
              event.preventDefault()
              if (!selectedItemId) {
                setError('Choose an item to list.')
                return
              }
              createMutation.mutate()
            }}
          >
            <label>
              Item
              <select
                data-testid="market-item-select"
                value={selectedItemId}
                onChange={(event) => {
                  setSelectedItemId(event.target.value)
                  const next = listableItems.find((item) => item.id === event.target.value)
                  if (next) {
                    setQuantity(Math.min(quantity, next.quantity - next.listedQuantity) || 1)
                  }
                }}
              >
                <option value="">Select an item</option>
                {listableItems.map((item) => (
                  <option key={item.id} value={item.id}>
                    {itemLabel(item)}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Quantity
              <input
                data-testid="market-quantity-input"
                type="number"
                min={1}
                max={maxQuantity}
                value={quantity}
                onChange={(event) => setQuantity(Number(event.target.value))}
              />
            </label>
            <label>
              Price (gold)
              <input
                data-testid="market-price-input"
                type="number"
                min={1}
                value={price}
                onChange={(event) => setPrice(Number(event.target.value))}
              />
            </label>
            <button
              type="submit"
              className="travel-button"
              data-testid="create-listing-button"
              disabled={tradeDisabled || !selectedItemId}
            >
              {createMutation.isPending ? 'Listing…' : 'List item'}
            </button>
          </form>
        )}
      </section>

      <section className="location-section" aria-labelledby="own-listings-heading">
        <h3 id="own-listings-heading">Your listings</h3>
        {ownListingsQuery.isLoading ? (
          <p className="muted">Loading your listings…</p>
        ) : ownLoadError ? (
          <p className="form-error" role="alert" data-testid="own-listings-error">
            {ownLoadError}
          </p>
        ) : ownListings.length === 0 ? (
          <p className="muted" data-testid="own-listings-empty">
            You have no active listings.
          </p>
        ) : (
          <ul className="inventory-list" data-testid="own-listings">
            {ownListings.map((listing) => (
              <ListingRow
                key={listing.id}
                listing={listing}
                actionLabel="Cancel"
                actionTestId={`cancel-listing-${listing.itemCode}`}
                disabled={tradeDisabled}
                onAction={() => cancelMutation.mutate(listing.id)}
              />
            ))}
          </ul>
        )}
      </section>
    </section>
  )
}

function itemLabel(item: InventoryItemResponse): string {
  const available = item.quantity - item.listedQuantity
  return `${item.name} · ${item.rarity} · qty ${available}`
}

function ListingRow({
  listing,
  actionLabel,
  actionTestId,
  disabled,
  onAction,
}: {
  listing: MarketListingResponse
  actionLabel: string
  actionTestId: string
  disabled: boolean
  onAction: () => void
}) {
  return (
    <li data-testid={`market-listing-${listing.itemCode}`}>
      <div className="inventory-item-main">
        <strong>{listing.itemName}</strong>
        <span className={`rarity rarity-${listing.rarity.toLowerCase()}`}>{listing.rarity}</span>
      </div>
      <p className="inventory-item-meta">
        {listing.itemType} · Qty {listing.quantity} · {listing.price} gold · Seller {listing.sellerName}
      </p>
      <div className="inventory-item-actions">
        <button
          type="button"
          className="travel-button"
          data-testid={actionTestId}
          disabled={disabled}
          onClick={onAction}
        >
          {actionLabel}
        </button>
      </div>
    </li>
  )
}
