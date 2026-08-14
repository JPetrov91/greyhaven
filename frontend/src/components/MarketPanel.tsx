import { useEffect, useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useSearchParams } from 'react-router-dom'
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
import type { InventoryItemResponse, ItemType, LocationAction } from '../api/types'
import { Button } from '../ui/Button'
import { EmptyState } from '../ui/EmptyState'
import { ErrorState } from '../ui/ErrorState'
import { Field } from '../ui/Field'
import { LoadingState } from '../ui/LoadingState'
import { Panel } from '../ui/Panel'
import { formatRarity } from '../ui/formatRarity'
import { MarketListingRow } from './MarketListingRow'

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
  const [searchParams] = useSearchParams()
  const [itemType, setItemType] = useState<ItemType | ''>('')
  const [selectedItemId, setSelectedItemId] = useState(searchParams.get('listItem') ?? '')
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

  useEffect(() => {
    const listItem = searchParams.get('listItem')
    if (listItem && listableItems.some((item) => item.id === listItem)) {
      setSelectedItemId(listItem)
    }
  }, [searchParams, listableItems])

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
    <Panel className="market-panel" data-testid="market-panel" aria-label="Marketplace" title="Marketplace" actions={
        onClose ? (
          <Button type="button" variant="ghost" data-testid="close-market" onClick={onClose}>
            Close
          </Button>
        ) : null
      }
    >
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
        <Field label="Item type">
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
        </Field>
      </section>

      <section className="location-section" aria-labelledby="market-listings-heading">
        <h3 id="market-listings-heading">Active listings</h3>
        {listingsQuery.isLoading ? (
          <LoadingState>Loading listings…</LoadingState>
        ) : loadError ? (
          <ErrorState testId="market-load-error">{loadError}</ErrorState>
        ) : listings.length === 0 ? (
          <EmptyState testId="market-listings-empty">No listings match this filter.</EmptyState>
        ) : (
          <>
            <ul className="inventory-list" data-testid="market-listings">
              {listings.map((listing) => (
                <MarketListingRow
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
            <Field label="Item">
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
            </Field>
            <Field label="Quantity">
              <input
                data-testid="market-quantity-input"
                type="number"
                min={1}
                max={maxQuantity}
                value={quantity}
                onChange={(event) => setQuantity(Number(event.target.value))}
              />
            </Field>
            <Field label="Price (gold)">
              <input
                data-testid="market-price-input"
                type="number"
                min={1}
                value={price}
                onChange={(event) => setPrice(Number(event.target.value))}
              />
            </Field>
            <Button
              type="submit"
              data-testid="create-listing-button"
              disabled={tradeDisabled || !selectedItemId}
            >
              {createMutation.isPending ? 'Listing…' : 'List item'}
            </Button>
          </form>
        )}
      </section>

      <section className="location-section" aria-labelledby="own-listings-heading">
        <h3 id="own-listings-heading">Your listings</h3>
        {ownListingsQuery.isLoading ? (
          <LoadingState>Loading your listings…</LoadingState>
        ) : ownLoadError ? (
          <ErrorState testId="own-listings-error">{ownLoadError}</ErrorState>
        ) : ownListings.length === 0 ? (
          <EmptyState testId="own-listings-empty">You have no active listings.</EmptyState>
        ) : (
          <ul className="inventory-list" data-testid="own-listings">
            {ownListings.map((listing) => (
              <MarketListingRow
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
    </Panel>
  )
}

function itemLabel(item: InventoryItemResponse): string {
  const available = item.quantity - item.listedQuantity
  return `${item.displayName} · ${formatRarity(item.rarity)} · qty ${available}`
}
