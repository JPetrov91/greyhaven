import { useEffect, useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useSearchParams } from 'react-router-dom'
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
import { Button } from '../ui/Button'
import { ComingLaterButton } from '../ui/ComingLater'
import { EmptyState } from '../ui/EmptyState'
import { ErrorState } from '../ui/ErrorState'
import { Field } from '../ui/Field'
import { formatRarity } from '../ui/formatRarity'
import { gameLink } from '../ui/gameNav'
import { ItemDetail } from '../ui/ItemDetail'
import { ItemIcon } from '../ui/itemIcons'
import { LoadingState } from '../ui/LoadingState'
import { locationArtUrl } from '../ui/locationMedia'
import { Panel } from '../ui/Panel'
import { RarityBadge } from '../ui/RarityBadge'
import { StatRow } from '../ui/StatRow'
import { formatItemType, listingIconSource, MarketListingRow } from './MarketListingRow'

const ITEM_TYPES: { value: ItemType | ''; label: string }[] = [
  { value: '', label: 'All types' },
  { value: 'WEAPON', label: 'Weapons' },
  { value: 'ARMOR', label: 'Armor' },
  { value: 'ACCESSORY', label: 'Accessories' },
  { value: 'CONSUMABLE', label: 'Consumables' },
  { value: 'MATERIAL', label: 'Materials' },
]

type MarketTab = 'all' | 'mine'

type Props = {
  onClose?: () => void
}

export function MarketPanel({ onClose }: Props) {
  const queryClient = useQueryClient()
  const [searchParams] = useSearchParams()
  const [itemType, setItemType] = useState<ItemType | ''>('')
  const [search, setSearch] = useState('')
  const [tab, setTab] = useState<MarketTab>(searchParams.get('listItem') ? 'mine' : 'all')
  const [selectedListingId, setSelectedListingId] = useState<string | null>(null)
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
      setTab('mine')
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
  const travelReason = 'Travel to the Market to buy, sell, or cancel listings.'

  const visibleListings = useMemo(() => filterListings(listings, search), [listings, search])
  const visibleOwnListings = useMemo(() => filterListings(ownListings, search), [ownListings, search])
  const tableListings = tab === 'mine' ? visibleOwnListings : visibleListings

  useEffect(() => {
    if (tableListings.some((listing) => listing.id === selectedListingId)) {
      return
    }
    setSelectedListingId(tableListings[0]?.id ?? null)
  }, [tableListings, selectedListingId])

  const selectedListing = tableListings.find((listing) => listing.id === selectedListingId) ?? null
  const inspectedInventoryItem = selectedListing
    ? (inventoryQuery.data?.items ?? []).find((item) => item.id === selectedListing.itemInstanceId) ?? null
    : tab === 'mine'
      ? selectedItem
      : null

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

  const filterActive = Boolean(itemType || search.trim())
  const listingsEmptyCopy = filterActive
    ? 'No listings match this filter.'
    : 'No player listings yet. Travel to the Market and list an item from My listings.'

  function listingDisabledReason(listing: MarketListingResponse): string | undefined {
    if (!atMarket) {
      return travelReason
    }
    if (listing.ownListing && tab === 'all') {
      return 'You cannot buy your own listing.'
    }
    return undefined
  }

  return (
    <Panel
      className="market-panel game-column"
      data-testid="market-panel"
      aria-label="Marketplace"
      title="Marketplace"
      actions={
        onClose ? (
          <Button type="button" variant="ghost" data-testid="close-market" onClick={onClose}>
            Close
          </Button>
        ) : null
      }
    >
      <div className="market-banner">
        <img className="market-banner-art" src={locationArtUrl('MARKET')} alt="" />
        <div className="market-banner-copy">
          <p className="muted">Trade equipment, resources, and rare items with adventurers across Greyhaven.</p>
        </div>
      </div>

      {locationQuery.data && !atMarket ? (
        <div className="market-travel" data-testid="market-travel-hint">
          <p className="muted">{travelReason}</p>
          <Link className="btn btn-secondary" to={gameLink('world')} data-testid="market-travel-cta">
            Travel to Market
          </Link>
        </div>
      ) : null}
      {error ? (
        <p className="form-error" role="alert" data-testid="market-error">
          {error}
        </p>
      ) : null}

      <Field label="Search" className="market-search-field">
        <input
          type="search"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          data-testid="market-search"
          placeholder="Search items, e.g. longsword, iron…"
        />
      </Field>

      <div className="market-filter-row">
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
        <LockedFilter testId="market-rarity-filter" label="Rarity" value="All rarities" />
        <LockedFilter testId="market-family-filter" label="Weapon family" value="All families" />
        <LockedFilter testId="market-level-filter" label="Level range" value="1 – 60" />
        <LockedFilter testId="market-price-filter" label="Price range" value="Min – Max" />
        <LockedFilter testId="market-seller-filter" label="Seller" value="All sellers" />
        <LockedFilter testId="market-sort" label="Sort by" value="Price: Low to High" />
        <button
          type="button"
          className="btn btn-secondary market-refresh"
          data-testid="market-refresh"
          aria-label="Refresh listings"
          onClick={() => {
            void listingsQuery.refetch()
            void ownListingsQuery.refetch()
          }}
        >
          Refresh
        </button>
      </div>

      <div className="market-tabs" role="tablist" aria-label="Marketplace views">
        <button
          type="button"
          role="tab"
          className={tab === 'all' ? 'tab tab-active' : 'tab'}
          aria-selected={tab === 'all'}
          data-testid="market-tab-all"
          onClick={() => setTab('all')}
        >
          All listings ({visibleListings.length})
        </button>
        <ComingLaterButton className="tab" role="tab" aria-selected={false} data-testid="market-tab-orders">
          Buy orders
        </ComingLaterButton>
        <button
          type="button"
          role="tab"
          className={tab === 'mine' ? 'tab tab-active' : 'tab'}
          aria-selected={tab === 'mine'}
          data-testid="market-tab-mine"
          onClick={() => setTab('mine')}
        >
          My listings ({ownListings.length})
        </button>
        <ComingLaterButton className="tab" role="tab" aria-selected={false} data-testid="market-tab-history">
          My sales history
        </ComingLaterButton>
      </div>

      <div className="market-workspace">
        <div className="market-browse">
          {tab === 'mine' ? (
            <section className="market-sell" aria-labelledby="create-listing-heading">
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
                    title={!atMarket ? travelReason : undefined}
                  >
                    {createMutation.isPending ? 'Listing…' : 'List item'}
                  </Button>
                </form>
              )}
            </section>
          ) : null}

          {tab === 'all' ? (
            listingsQuery.isLoading ? (
              <LoadingState>Loading listings…</LoadingState>
            ) : loadError ? (
              <ErrorState testId="market-load-error">{loadError}</ErrorState>
            ) : visibleListings.length === 0 ? (
              <EmptyState testId="market-listings-empty">{listingsEmptyCopy}</EmptyState>
            ) : (
              <ListingTable
                listings={visibleListings}
                selectedId={selectedListingId}
                actionLabel="Buy"
                actionTestId={(listing) => `buy-listing-${listing.itemCode}`}
                disabled={(listing) => tradeDisabled || listing.ownListing}
                disabledReason={listingDisabledReason}
                testId="market-listings"
                onSelect={setSelectedListingId}
                onAction={(listing) => buyMutation.mutate(listing.id)}
              />
            )
          ) : ownListingsQuery.isLoading ? (
            <LoadingState>Loading your listings…</LoadingState>
          ) : ownLoadError ? (
            <ErrorState testId="own-listings-error">{ownLoadError}</ErrorState>
          ) : visibleOwnListings.length === 0 ? (
            <EmptyState testId="own-listings-empty">You have no active listings.</EmptyState>
          ) : (
            <ListingTable
              listings={visibleOwnListings}
              selectedId={selectedListingId}
              actionLabel="Cancel"
              actionTestId={(listing) => `cancel-listing-${listing.itemCode}`}
              disabled={() => tradeDisabled}
              disabledReason={() => (!atMarket ? travelReason : undefined)}
              testId="own-listings"
              onSelect={setSelectedListingId}
              onAction={(listing) => cancelMutation.mutate(listing.id)}
            />
          )}

          <div className="market-footer">
            <p className="muted" data-testid="market-listing-count">
              {listingsTruncated && tab === 'all'
                ? `Showing the ${listings.length} newest listings.`
                : `Showing ${tableListings.length} listing${tableListings.length === 1 ? '' : 's'}.`}
            </p>
            <div className="market-pagination" aria-label="Listing pages">
              <ComingLaterButton className="tab" data-testid="market-page-1">
                1
              </ComingLaterButton>
              <ComingLaterButton className="tab" data-testid="market-page-2">
                2
              </ComingLaterButton>
              <ComingLaterButton className="tab" data-testid="market-page-3">
                3
              </ComingLaterButton>
            </div>
          </div>
        </div>

        <aside className="market-inspector" aria-label="Selected listing">
          {selectedListing ? (
            <ListingInspector
              listing={selectedListing}
              inventoryItem={inspectedInventoryItem}
              tab={tab}
              tradeDisabled={tradeDisabled}
              travelReason={travelReason}
              buyPending={buyMutation.isPending}
              cancelPending={cancelMutation.isPending}
              onBuy={() => buyMutation.mutate(selectedListing.id)}
              onCancel={() => cancelMutation.mutate(selectedListing.id)}
            />
          ) : tab === 'mine' && selectedItem ? (
            <ItemDetail item={selectedItem} showComparison={false} showIcon valueLabel="Vendor value" />
          ) : (
            <EmptyState>Select a listing to inspect it.</EmptyState>
          )}
        </aside>
      </div>
    </Panel>
  )
}

function ListingTable({
  listings,
  selectedId,
  actionLabel,
  actionTestId,
  disabled,
  disabledReason,
  testId,
  onSelect,
  onAction,
}: {
  listings: MarketListingResponse[]
  selectedId: string | null
  actionLabel: string
  actionTestId: (listing: MarketListingResponse) => string
  disabled: (listing: MarketListingResponse) => boolean
  disabledReason: (listing: MarketListingResponse) => string | undefined
  testId: string
  onSelect: (id: string) => void
  onAction: (listing: MarketListingResponse) => void
}) {
  return (
    <div className="market-table-wrap">
      <table className="market-table" data-testid={testId}>
        <thead>
          <tr>
            <th>Item</th>
            <th>Rarity</th>
            <th>Type</th>
            <th>Qty</th>
            <th title="Coming later">Lvl</th>
            <th title="Coming later">Stats</th>
            <th>Price</th>
            <th>Seller</th>
            <th title="Coming later">Time left</th>
            <th>
              <span className="visually-hidden">Action</span>
            </th>
          </tr>
        </thead>
        <tbody>
          {listings.map((listing) => (
            <MarketListingRow
              key={listing.id}
              listing={listing}
              selected={listing.id === selectedId}
              actionLabel={actionLabel}
              actionTestId={actionTestId(listing)}
              disabled={disabled(listing)}
              disabledReason={disabledReason(listing)}
              onSelect={() => onSelect(listing.id)}
              onAction={() => onAction(listing)}
            />
          ))}
        </tbody>
      </table>
    </div>
  )
}

function ListingInspector({
  listing,
  inventoryItem,
  tab,
  tradeDisabled,
  travelReason,
  buyPending,
  cancelPending,
  onBuy,
  onCancel,
}: {
  listing: MarketListingResponse
  inventoryItem: InventoryItemResponse | null
  tab: MarketTab
  tradeDisabled: boolean
  travelReason: string
  buyPending: boolean
  cancelPending: boolean
  onBuy: () => void
  onCancel: () => void
}) {
  const buyDisabled = tradeDisabled || listing.ownListing || buyPending
  const buyReason = !listing.ownListing && tradeDisabled ? travelReason : listing.ownListing ? 'You cannot buy your own listing.' : undefined

  return (
    <>
      {inventoryItem ? (
        <ItemDetail item={inventoryItem} showComparison={Boolean(inventoryItem.comparison)} showIcon />
      ) : (
        <div className="item-detail">
          <header className="item-tooltip-header">
            <ItemIcon item={listingIconSource(listing.itemType)} className="item-icon item-icon-inspector" />
            <div className="item-detail-heading">
              <strong className={`item-name rarity-ink-${listing.rarity.toLowerCase()}`}>{listing.itemName}</strong>
              <RarityBadge rarity={listing.rarity} />
            </div>
          </header>
          <p className="item-tooltip-meta">
            {formatItemType(listing.itemType)} · Qty {listing.quantity}
          </p>
          <p className="item-tooltip-meta">Seller {listing.sellerName}</p>
        </div>
      )}
      <dl className="stat-list">
        <StatRow label="List price" value={`${listing.price} gold`} />
        <StatRow label="Lvl" value="—" />
        <StatRow label="Durability" value="—" />
        <StatRow label="Time left" value="—" />
      </dl>
      <div className="inventory-inspector-actions">
        {tab === 'mine' ? (
          <Button
            type="button"
            className="inventory-action-primary"
            data-testid={`inspector-cancel-${listing.itemCode}`}
            disabled={tradeDisabled || cancelPending}
            title={!tradeDisabled ? undefined : travelReason}
            onClick={onCancel}
          >
            Cancel listing
          </Button>
        ) : (
          <Button
            type="button"
            className="inventory-action-primary"
            data-testid={`inspector-buy-${listing.itemCode}`}
            disabled={buyDisabled}
            title={buyReason}
            onClick={onBuy}
          >
            Buy item
          </Button>
        )}
        <ComingLaterButton className="inventory-later" data-testid="market-buy-order">
          Place buy order
        </ComingLaterButton>
        <ComingLaterButton className="inventory-later" data-testid="market-watchlist">
          Add to watchlist
        </ComingLaterButton>
      </div>
      <section className="market-orders" aria-labelledby="market-orders-heading">
        <div className="market-orders-header">
          <h3 id="market-orders-heading">Active buy orders</h3>
          <ComingLaterButton className="tab" data-testid="market-orders-view-all">
            View all
          </ComingLaterButton>
        </div>
        <p className="muted" data-testid="market-orders-count">
          0 / —
        </p>
        <EmptyState>No active buy orders.</EmptyState>
      </section>
    </>
  )
}

function LockedFilter({ testId, label, value }: { testId: string; label: string; value: string }) {
  return (
    <Field label={label}>
      <select data-testid={testId} disabled title="Coming later" className="coming-later" aria-disabled="true">
        <option>{value}</option>
      </select>
    </Field>
  )
}

function filterListings(listings: MarketListingResponse[], search: string): MarketListingResponse[] {
  const query = search.trim().toLowerCase()
  if (!query) {
    return listings
  }
  return listings.filter((listing) =>
    [listing.itemName, listing.itemCode, listing.sellerName, listing.itemType, listing.rarity]
      .join(' ')
      .toLowerCase()
      .includes(query),
  )
}

function itemLabel(item: InventoryItemResponse): string {
  const available = item.quantity - item.listedQuantity
  return `${item.displayName} · ${formatRarity(item.rarity)} · qty ${available}`
}
