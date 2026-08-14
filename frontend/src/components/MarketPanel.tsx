import { useEffect, useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useSearchParams } from 'react-router-dom'
import { ApiError } from '../api/client'
import { fetchInventory } from '../api/inventory'
import {
  buyMarketListing,
  buyMerchantItem,
  cancelMarketListing,
  createMarketListing,
  fetchMarketListings,
  fetchMerchants,
  fetchOwnMarketListings,
} from '../api/market'
import { fetchCurrentLocation } from '../api/world'
import type {
  InventoryItemResponse,
  ItemType,
  LocationAction,
  MarketListingResponse,
  MerchantResponse,
  MerchantStockItemResponse,
} from '../api/types'
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
type MarketHub = 'merchants' | 'player' | 'listings'

type Props = {
  onClose?: () => void
}

export function MarketPanel({ onClose }: Props) {
  const queryClient = useQueryClient()
  const [searchParams] = useSearchParams()
  const initialHub = hubFromSearch(searchParams)
  const [itemType, setItemType] = useState<ItemType | ''>('')
  const [search, setSearch] = useState('')
  const [hub, setHub] = useState<MarketHub>(initialHub)
  const [tab, setTab] = useState<MarketTab>(initialHub === 'listings' ? 'mine' : 'all')
  const [selectedListingId, setSelectedListingId] = useState<string | null>(null)
  const [selectedItemId, setSelectedItemId] = useState(searchParams.get('listItem') ?? '')
  const [selectedMerchantId, setSelectedMerchantId] = useState<string | null>(null)
  const [selectedStockCode, setSelectedStockCode] = useState<string | null>(null)
  const [merchantQuantity, setMerchantQuantity] = useState(1)
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

  const merchantsQuery = useQuery({
    queryKey: ['market-merchants'],
    queryFn: fetchMerchants,
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
    setHub(hubFromSearch(searchParams))
  }, [searchParams])

  useEffect(() => {
    const listItem = searchParams.get('listItem')
    if (listItem && listableItems.some((item) => item.id === listItem)) {
      setSelectedItemId(listItem)
      setTab('mine')
    }
  }, [searchParams, listableItems])

  const merchants = merchantsQuery.data?.merchants ?? []
  useEffect(() => {
    if (selectedMerchantId && merchants.some((merchant) => merchant.id === selectedMerchantId)) {
      return
    }
    const requested = searchParams.get('merchant')
    const match = merchants.find((merchant) => merchant.code === requested || merchant.id === requested)
    setSelectedMerchantId(match?.id ?? merchants[0]?.id ?? null)
  }, [merchants, selectedMerchantId, searchParams])

  const selectedMerchant = merchants.find((merchant) => merchant.id === selectedMerchantId) ?? null
  const visibleStock = useMemo(
    () => (selectedMerchant ? filterStock(selectedMerchant.stock, search) : []),
    [selectedMerchant, search],
  )
  useEffect(() => {
    if (visibleStock.some((item) => item.itemCode === selectedStockCode)) {
      return
    }
    setSelectedStockCode(visibleStock[0]?.itemCode ?? null)
  }, [visibleStock, selectedStockCode])
  useEffect(() => {
    setMerchantQuantity(1)
  }, [selectedStockCode])
  const selectedStock = visibleStock.find((item) => item.itemCode === selectedStockCode) ?? null
  const merchantMaxQuantity = selectedStock?.itemType === 'CONSUMABLE' || selectedStock?.itemType === 'MATERIAL' ? 99 : 1

  async function refreshAfterChange() {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['market-listings'] }),
      queryClient.invalidateQueries({ queryKey: ['market-own-listings'] }),
      queryClient.invalidateQueries({ queryKey: ['market-merchants'] }),
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

  const merchantBuyMutation = useMutation({
    mutationFn: () => {
      if (!selectedMerchant || !selectedStock) {
        throw new Error('Choose a merchant item first.')
      }
      return buyMerchantItem(selectedMerchant.id, selectedStock.itemDefinitionId, merchantQuantity)
    },
    onSuccess: async () => {
      setError(null)
      await refreshAfterChange()
    },
    onError: (cause) => {
      setError(cause instanceof ApiError ? cause.message : 'Unable to buy from that merchant.')
    },
  })

  const listings = listingsQuery.data?.listings ?? []
  const listingsTruncated = listingsQuery.data?.truncated ?? false
  const ownListings = ownListingsQuery.data?.listings ?? []
  const busy =
    createMutation.isPending ||
    buyMutation.isPending ||
    cancelMutation.isPending ||
    merchantBuyMutation.isPending
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
      aria-label="Greyhaven Market"
      title="Greyhaven Market"
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
          <p className="muted">Trade District — NPC merchants and the player market share this hall.</p>
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

      <div className="market-tabs market-hub-tabs" role="tablist" aria-label="Greyhaven Market">
        <button
          type="button"
          role="tab"
          className={hub === 'merchants' ? 'tab tab-active' : 'tab'}
          aria-selected={hub === 'merchants'}
          data-testid="market-hub-merchants"
          onClick={() => setHub('merchants')}
        >
          Merchants
        </button>
        <button
          type="button"
          role="tab"
          className={hub === 'player' ? 'tab tab-active' : 'tab'}
          aria-selected={hub === 'player'}
          data-testid="market-hub-player"
          onClick={() => {
            setHub('player')
            setTab('all')
          }}
        >
          Player Market
        </button>
        <button
          type="button"
          role="tab"
          className={hub === 'listings' ? 'tab tab-active' : 'tab'}
          aria-selected={hub === 'listings'}
          data-testid="market-hub-listings"
          onClick={() => {
            setHub('listings')
            setTab('mine')
          }}
        >
          My Listings
        </button>
      </div>

      {hub === 'merchants' ? (
        <MerchantHub
          merchants={merchants}
          selectedMerchant={selectedMerchant}
          visibleStock={visibleStock}
          selectedStock={selectedStock}
          merchantQuantity={merchantQuantity}
          merchantMaxQuantity={merchantMaxQuantity}
          loading={merchantsQuery.isLoading}
          loadError={
            merchantsQuery.error
              ? merchantsQuery.error instanceof ApiError
                ? merchantsQuery.error.message
                : 'Unable to load merchants.'
              : null
          }
          search={search}
          tradeDisabled={tradeDisabled}
          buyPending={merchantBuyMutation.isPending}
          buyDisabledReason={!atMarket ? travelReason : undefined}
          onSearch={setSearch}
          onSelectMerchant={(id) => {
            setSelectedMerchantId(id)
            setSelectedStockCode(null)
          }}
          onSelectStock={setSelectedStockCode}
          onQuantity={setMerchantQuantity}
          onBuy={() => merchantBuyMutation.mutate()}
        />
      ) : (
        <>
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
        </>
      )}
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

function hubFromSearch(searchParams: URLSearchParams): MarketHub {
  if (searchParams.get('listItem')) {
    return 'listings'
  }
  const hub = searchParams.get('hub')
  if (hub === 'player' || hub === 'listings' || hub === 'merchants') {
    return hub
  }
  return 'merchants'
}

function filterStock(stock: MerchantStockItemResponse[], search: string): MerchantStockItemResponse[] {
  const query = search.trim().toLowerCase()
  if (!query) {
    return stock
  }
  return stock.filter((item) =>
    [item.itemName, item.itemCode, item.itemType, item.rarity].join(' ').toLowerCase().includes(query),
  )
}

function MerchantHub({
  merchants,
  selectedMerchant,
  visibleStock,
  selectedStock,
  merchantQuantity,
  merchantMaxQuantity,
  loading,
  loadError,
  search,
  tradeDisabled,
  buyPending,
  buyDisabledReason,
  onSearch,
  onSelectMerchant,
  onSelectStock,
  onQuantity,
  onBuy,
}: {
  merchants: MerchantResponse[]
  selectedMerchant: MerchantResponse | null
  visibleStock: MerchantStockItemResponse[]
  selectedStock: MerchantStockItemResponse | null
  merchantQuantity: number
  merchantMaxQuantity: number
  loading: boolean
  loadError: string | null
  search: string
  tradeDisabled: boolean
  buyPending: boolean
  buyDisabledReason?: string
  onSearch: (value: string) => void
  onSelectMerchant: (id: string) => void
  onSelectStock: (code: string) => void
  onQuantity: (value: number) => void
  onBuy: () => void
}) {
  return (
    <div className="market-workspace merchant-workspace">
      <div className="merchant-list" data-testid="merchant-list">
        {merchants.map((merchant) => (
          <button
            key={merchant.id}
            type="button"
            className={merchant.id === selectedMerchant?.id ? 'merchant-card merchant-card-selected' : 'merchant-card'}
            data-testid={`merchant-${merchant.code}`}
            onClick={() => onSelectMerchant(merchant.id)}
          >
            <span className="merchant-portrait" aria-hidden="true">
              {initials(merchant.name)}
            </span>
            <span>
              <strong>{merchant.name}</strong>
              <span className="muted">{merchant.title}</span>
            </span>
          </button>
        ))}
      </div>
      <div className="market-browse">
        <Field label="Search" className="market-search-field">
          <input
            type="search"
            value={search}
            onChange={(event) => onSearch(event.target.value)}
            data-testid="merchant-search"
            placeholder="Search merchant goods…"
          />
        </Field>
        {loading ? (
          <LoadingState>Loading merchants…</LoadingState>
        ) : loadError ? (
          <ErrorState testId="merchant-load-error">{loadError}</ErrorState>
        ) : !selectedMerchant ? (
          <EmptyState testId="merchant-empty">No merchants are trading today.</EmptyState>
        ) : visibleStock.length === 0 ? (
          <EmptyState testId="merchant-stock-empty">This merchant has no matching goods.</EmptyState>
        ) : (
          <div className="market-table-wrap">
            <table className="market-table" data-testid="merchant-stock">
              <thead>
                <tr>
                  <th>Item</th>
                  <th>Rarity</th>
                  <th>Type</th>
                  <th>Price</th>
                  <th>Stock</th>
                </tr>
              </thead>
              <tbody>
                {visibleStock.map((item) => (
                  <tr
                    key={item.itemDefinitionId}
                    className={item.itemCode === selectedStock?.itemCode ? 'market-row market-row-selected' : 'market-row'}
                  >
                    <td>
                      <button
                        type="button"
                        className="market-row-select"
                        data-testid={`merchant-stock-${item.itemCode}`}
                        onClick={() => onSelectStock(item.itemCode)}
                      >
                        <ItemIcon item={listingIconSource(item.itemType)} />
                        {item.itemName}
                      </button>
                    </td>
                    <td>
                      <RarityBadge rarity={item.rarity} />
                    </td>
                    <td>{formatItemType(item.itemType)}</td>
                    <td>{item.sellPrice}g</td>
                    <td>Unlimited</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
      <aside className="market-inspector" aria-label="Selected merchant goods">
        {selectedMerchant && selectedStock ? (
          <>
            <p className="muted" data-testid="merchant-identity">
              {selectedMerchant.name} · {selectedMerchant.title}
            </p>
            <p>{selectedMerchant.description}</p>
            <ItemDetail item={stockAsInventoryItem(selectedStock)} showComparison={false} showIcon valueLabel="Merchant price" />
            <StatRow label="Price" value={`${selectedStock.sellPrice} gold`} />
            <Field label="Quantity">
              <input
                data-testid="merchant-buy-quantity"
                type="number"
                min={1}
                max={merchantMaxQuantity}
                value={merchantQuantity}
                onChange={(event) => onQuantity(Number(event.target.value))}
              />
            </Field>
            <Button
              type="button"
              className="inventory-action-primary"
              data-testid={`buy-merchant-${selectedStock.itemCode}`}
              disabled={tradeDisabled || buyPending}
              title={buyDisabledReason}
              onClick={onBuy}
            >
              {buyPending ? 'Buying…' : `Buy for ${selectedStock.sellPrice * merchantQuantity} gold`}
            </Button>
          </>
        ) : (
          <EmptyState>Select a merchant good to inspect it.</EmptyState>
        )}
      </aside>
    </div>
  )
}

function initials(name: string): string {
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? '')
    .join('')
}

function stockAsInventoryItem(item: MerchantStockItemResponse): InventoryItemResponse {
  return {
    id: item.itemDefinitionId,
    definitionId: item.itemDefinitionId,
    code: item.itemCode,
    name: item.itemName,
    displayName: item.itemName,
    description: item.description,
    type: item.itemType,
    rarity: item.rarity,
    quantity: 1,
    requiredLevel: item.requiredLevel,
    requiredStrength: item.requiredStrength,
    requiredAgility: item.requiredAgility,
    requiredEndurance: item.requiredEndurance,
    requiredPerception: item.requiredPerception,
    baseValue: item.sellPrice,
    equipped: false,
    canEquip: item.equipmentSlot != null,
    twoHanded: item.twoHanded,
    legacy: false,
    equipmentSlot: item.equipmentSlot,
    weaponFamily: item.weaponFamily,
    armorCategory: item.armorCategory,
    usable: item.healAmount != null,
    listedQuantity: 0,
    rolledWeaponDamage: item.weaponDamage,
    rolledArmorValue: item.armorValue,
    weaponDamage: item.weaponDamage,
    armorValue: item.armorValue,
    healAmount: item.healAmount,
    affixes: [],
    comparison: null,
  }
}
