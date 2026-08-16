import { useEffect, useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useSearchParams } from 'react-router-dom'
import { ApiError } from '../api/client'
import { fetchInventory } from '../api/inventory'
import {
  buyMarketListing,
  buyMerchantItem,
  cancelBuyOrder,
  cancelMarketListing,
  createBuyOrder,
  createMarketListing,
  fetchBuyOrders,
  fetchMarketListingHistory,
  fetchMarketListings,
  fetchMerchants,
  fetchOwnMarketListings,
  fulfillBuyOrder,
  type MarketListingSort,
  type SortDirection,
} from '../api/market'
import { fetchCurrentLocation } from '../api/world'
import type {
  InventoryItemResponse,
  ItemRarity,
  ItemType,
  LocationAction,
  MarketBuyOrderResponse,
  MarketListingResponse,
  MerchantResponse,
  MerchantStockItemResponse,
  WeaponFamily,
} from '../api/types'
import { Button } from '../ui/Button'
import { ComingLaterButton } from '../ui/ComingLater'
import { EmptyState } from '../ui/EmptyState'
import { ErrorState } from '../ui/ErrorState'
import { Field } from '../ui/Field'
import { SearchInput } from '../ui/SearchInput'
import { Select } from '../ui/Select'
import { TextInput } from '../ui/TextInput'
import { formatRarity } from '../ui/formatRarity'
import { gameLink } from '../ui/gameNav'
import { ItemDetail } from '../ui/ItemDetail'
import { ItemIcon } from '../ui/itemIcons'
import { LoadingState } from '../ui/LoadingState'
import { locationArtUrl } from '../ui/locationMedia'
import { merchantPortraitUrl } from '../ui/merchantMedia'
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

const RARITIES: { value: ItemRarity | ''; label: string }[] = [
  { value: '', label: 'All rarities' },
  { value: 'COMMON', label: 'Common' },
  { value: 'UNCOMMON', label: 'Uncommon' },
  { value: 'RARE', label: 'Rare' },
  { value: 'EPIC', label: 'Epic' },
]

const WEAPON_FAMILIES: { value: WeaponFamily | ''; label: string }[] = [
  { value: '', label: 'All families' },
  { value: 'SWORD', label: 'Sword' },
  { value: 'AXE', label: 'Axe' },
  { value: 'MACE', label: 'Mace' },
  { value: 'DAGGER', label: 'Dagger' },
  { value: 'BOW', label: 'Bow' },
]

type MarketTab = 'all' | 'mine' | 'orders' | 'history'
type MarketHub = 'merchants' | 'player' | 'listings'

type Props = {
  onClose?: () => void
}

export function MarketPanel({ onClose }: Props) {
  const queryClient = useQueryClient()
  const [searchParams] = useSearchParams()
  const initialHub = hubFromSearch(searchParams)
  const [itemType, setItemType] = useState<ItemType | ''>('')
  const [rarity, setRarity] = useState<ItemRarity | ''>('')
  const [weaponFamily, setWeaponFamily] = useState<WeaponFamily | ''>('')
  const [minLevel, setMinLevel] = useState('')
  const [maxLevel, setMaxLevel] = useState('')
  const [minPrice, setMinPrice] = useState('')
  const [maxPrice, setMaxPrice] = useState('')
  const [sort, setSort] = useState<MarketListingSort>('CREATED_AT')
  const [direction, setDirection] = useState<SortDirection>('DESC')
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [hub, setHub] = useState<MarketHub>(initialHub)
  const [tab, setTab] = useState<MarketTab>(initialHub === 'listings' ? 'mine' : 'all')
  const [selectedListingId, setSelectedListingId] = useState<string | null>(null)
  const [selectedOrderId, setSelectedOrderId] = useState<string | null>(null)
  const [selectedItemId, setSelectedItemId] = useState(searchParams.get('listItem') ?? '')
  const [selectedMerchantId, setSelectedMerchantId] = useState<string | null>(null)
  const [selectedStockCode, setSelectedStockCode] = useState<string | null>(null)
  const [merchantQuantity, setMerchantQuantity] = useState(1)
  const [quantity, setQuantity] = useState(1)
  const [price, setPrice] = useState(10)
  const [orderDefinitionId, setOrderDefinitionId] = useState('')
  const [orderQuantity, setOrderQuantity] = useState(1)
  const [orderMaxPrice, setOrderMaxPrice] = useState(10)
  const [fulfillItemId, setFulfillItemId] = useState('')
  const [fulfillQuantity, setFulfillQuantity] = useState(1)
  const [error, setError] = useState<string | null>(null)

  const listingsQuery = useQuery({
    queryKey: ['market-listings', itemType, rarity, weaponFamily, minLevel, maxLevel, minPrice, maxPrice, sort, direction, page],
    queryFn: () =>
      fetchMarketListings({
        itemType,
        rarity,
        weaponFamily,
        minLevel: minLevel === '' ? '' : Number(minLevel),
        maxLevel: maxLevel === '' ? '' : Number(maxLevel),
        minPrice: minPrice === '' ? '' : Number(minPrice),
        maxPrice: maxPrice === '' ? '' : Number(maxPrice),
        sort,
        direction,
        page,
        size: 20,
      }),
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

  const historyQuery = useQuery({
    queryKey: ['market-history', page],
    queryFn: () => fetchMarketListingHistory(page, 20),
    retry: false,
    enabled: tab === 'history',
  })

  const buyOrdersQuery = useQuery({
    queryKey: ['market-buy-orders'],
    queryFn: () => fetchBuyOrders(false, 0, 20),
    retry: false,
    enabled: tab === 'orders',
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
      queryClient.invalidateQueries({ queryKey: ['market-history'] }),
      queryClient.invalidateQueries({ queryKey: ['market-buy-orders'] }),
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

  const createOrderMutation = useMutation({
    mutationFn: () => createBuyOrder(orderDefinitionId, orderQuantity, orderMaxPrice),
    onSuccess: async () => {
      setError(null)
      await refreshAfterChange()
    },
    onError: (cause) => {
      setError(cause instanceof ApiError ? cause.message : 'Unable to create that buy order.')
    },
  })

  const fulfillOrderMutation = useMutation({
    mutationFn: (orderId: string) => fulfillBuyOrder(orderId, fulfillItemId, fulfillQuantity),
    onSuccess: async () => {
      setError(null)
      await refreshAfterChange()
    },
    onError: (cause) => {
      setError(cause instanceof ApiError ? cause.message : 'Unable to fulfill that buy order.')
    },
  })

  const cancelOrderMutation = useMutation({
    mutationFn: cancelBuyOrder,
    onSuccess: async () => {
      setError(null)
      await refreshAfterChange()
    },
    onError: (cause) => {
      setError(cause instanceof ApiError ? cause.message : 'Unable to cancel that buy order.')
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
  const listingTotal = listingsQuery.data?.total ?? listings.length
  const listingFeePercent = listingsQuery.data?.listingFeePercent ?? 0.02
  const saleFeePercent = listingsQuery.data?.saleFeePercent ?? 0.08
  const ownListings = ownListingsQuery.data?.listings ?? []
  const historyListings = historyQuery.data?.listings ?? []
  const buyOrders = buyOrdersQuery.data?.orders ?? []
  const busy =
    createMutation.isPending ||
    buyMutation.isPending ||
    cancelMutation.isPending ||
    merchantBuyMutation.isPending ||
    createOrderMutation.isPending ||
    fulfillOrderMutation.isPending ||
    cancelOrderMutation.isPending
  const tradeDisabled = busy || !atMarket
  const travelReason = 'Travel to the Market to buy, sell, or cancel listings.'

  const visibleListings = useMemo(() => filterListings(listings, search), [listings, search])
  const visibleOwnListings = useMemo(() => filterListings(ownListings, search), [ownListings, search])
  const visibleHistory = useMemo(() => filterListings(historyListings, search), [historyListings, search])
  const tableListings =
    tab === 'mine' ? visibleOwnListings : tab === 'history' ? visibleHistory : visibleListings
  const pageCount = Math.max(1, Math.ceil(listingTotal / 20))

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

  const selectedOrder = buyOrders.find((order) => order.id === selectedOrderId) ?? buyOrders[0] ?? null
  const listingFeePreview = Math.ceil(Math.max(0, price) * listingFeePercent)
  const reservedPreview = orderQuantity * orderMaxPrice
  const buyOrderFeePreview = Math.ceil(Math.max(0, reservedPreview) * listingFeePercent)

  const filterActive = Boolean(itemType || rarity || weaponFamily || minLevel || maxLevel || minPrice || maxPrice || search.trim())
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
      id="market"
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
        <SearchInput
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          data-testid="market-search"
          placeholder="Search items, e.g. longsword, iron…"
        />
      </Field>

      <div className="market-filter-row">
        <Field label="Item type">
          <Select
            data-testid="market-type-filter"
            value={itemType}
            onChange={(event) => {
              setItemType(event.target.value as ItemType | '')
              setPage(0)
            }}
          >
            {ITEM_TYPES.map((option) => (
              <option key={option.label} value={option.value}>
                {option.label}
              </option>
            ))}
          </Select>
        </Field>
        <Field label="Rarity">
          <Select
            data-testid="market-rarity-filter"
            value={rarity}
            onChange={(event) => {
              setRarity(event.target.value as ItemRarity | '')
              setPage(0)
            }}
          >
            {RARITIES.map((option) => (
              <option key={option.label} value={option.value}>
                {option.label}
              </option>
            ))}
          </Select>
        </Field>
        <Field label="Weapon family">
          <Select
            data-testid="market-family-filter"
            value={weaponFamily}
            onChange={(event) => {
              setWeaponFamily(event.target.value as WeaponFamily | '')
              setPage(0)
            }}
          >
            {WEAPON_FAMILIES.map((option) => (
              <option key={option.label} value={option.value}>
                {option.label}
              </option>
            ))}
          </Select>
        </Field>
        <Field label="Min level">
          <TextInput
            data-testid="market-level-filter"
            type="number"
            min={1}
            value={minLevel}
            placeholder="Min"
            onChange={(event) => {
              setMinLevel(event.target.value)
              setPage(0)
            }}
          />
        </Field>
        <Field label="Max level">
          <TextInput
            type="number"
            min={1}
            value={maxLevel}
            placeholder="Max"
            onChange={(event) => {
              setMaxLevel(event.target.value)
              setPage(0)
            }}
          />
        </Field>
        <Field label="Min price">
          <TextInput
            data-testid="market-price-filter"
            type="number"
            min={1}
            value={minPrice}
            placeholder="Min"
            onChange={(event) => {
              setMinPrice(event.target.value)
              setPage(0)
            }}
          />
        </Field>
        <Field label="Max price">
          <TextInput
            type="number"
            min={1}
            value={maxPrice}
            placeholder="Max"
            onChange={(event) => {
              setMaxPrice(event.target.value)
              setPage(0)
            }}
          />
        </Field>
        <LockedFilter testId="market-seller-filter" label="Seller" value="All sellers" />
        <Field label="Sort by">
          <Select
            data-testid="market-sort"
            value={`${sort}:${direction}`}
            onChange={(event) => {
              const [nextSort, nextDirection] = event.target.value.split(':') as [MarketListingSort, SortDirection]
              setSort(nextSort)
              setDirection(nextDirection)
              setPage(0)
            }}
          >
            <option value="CREATED_AT:DESC">Newest</option>
            <option value="CREATED_AT:ASC">Oldest</option>
            <option value="PRICE:ASC">Price: Low to High</option>
            <option value="PRICE:DESC">Price: High to Low</option>
          </Select>
        </Field>
        <Button
          type="button"
          variant="secondary"
          className="market-refresh"
          data-testid="market-refresh"
          aria-label="Refresh listings"
          onClick={() => {
            void listingsQuery.refetch()
            void ownListingsQuery.refetch()
          }}
        >
          Refresh
        </Button>
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
        <button
          type="button"
          role="tab"
          className={tab === 'orders' ? 'tab tab-active' : 'tab'}
          aria-selected={tab === 'orders'}
          data-testid="market-tab-orders"
          onClick={() => setTab('orders')}
        >
          Buy orders
        </button>
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
        <button
          type="button"
          role="tab"
          className={tab === 'history' ? 'tab tab-active' : 'tab'}
          aria-selected={tab === 'history'}
          data-testid="market-tab-history"
          onClick={() => setTab('history')}
        >
          My sales history
        </button>
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
                    <Select
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
                    </Select>
                  </Field>
                  <Field label="Quantity">
                    <TextInput
                      data-testid="market-quantity-input"
                      type="number"
                      min={1}
                      max={maxQuantity}
                      value={quantity}
                      onChange={(event) => setQuantity(Number(event.target.value))}
                    />
                  </Field>
                  <Field label="Price (gold)">
                    <TextInput
                      data-testid="market-price-input"
                      type="number"
                      min={1}
                      value={price}
                      onChange={(event) => setPrice(Number(event.target.value))}
                    />
                  </Field>
                  <p className="muted" data-testid="listing-fee-preview">
                    Listing fee: {listingFeePreview} gold ({Math.round(listingFeePercent * 100)}%). Sale fee is{' '}
                    {Math.round(saleFeePercent * 100)}% when the item sells.
                  </p>
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
          ) : tab === 'history' ? (
            historyQuery.isLoading ? (
              <LoadingState>Loading history…</LoadingState>
            ) : visibleHistory.length === 0 ? (
              <EmptyState testId="market-history-empty">No sold or cancelled listings yet.</EmptyState>
            ) : (
              <ListingTable
                listings={visibleHistory}
                selectedId={selectedListingId}
                actionLabel="Closed"
                actionTestId={(listing) => `history-listing-${listing.itemCode}`}
                disabled={() => true}
                disabledReason={() => 'This listing is closed.'}
                testId="market-history"
                onSelect={setSelectedListingId}
                onAction={() => undefined}
              />
            )
          ) : tab === 'orders' ? (
            <BuyOrdersBoard
              orders={buyOrders}
              selectedId={selectedOrder?.id ?? null}
              listableItems={listableItems}
              orderDefinitionId={orderDefinitionId}
              orderQuantity={orderQuantity}
              orderMaxPrice={orderMaxPrice}
              reservedPreview={reservedPreview}
              buyOrderFeePreview={buyOrderFeePreview}
              listingFeePercent={listingFeePercent}
              fulfillItemId={fulfillItemId}
              fulfillQuantity={fulfillQuantity}
              tradeDisabled={tradeDisabled}
              travelReason={travelReason}
              onSelect={setSelectedOrderId}
              onDefinitionId={setOrderDefinitionId}
              onQuantity={setOrderQuantity}
              onMaxPrice={setOrderMaxPrice}
              onFulfillItemId={setFulfillItemId}
              onFulfillQuantity={setFulfillQuantity}
              onCreate={() => {
                if (!orderDefinitionId) {
                  setError('Choose an item definition to bid on.')
                  return
                }
                createOrderMutation.mutate()
              }}
              onFulfill={(orderId) => {
                if (!fulfillItemId) {
                  setError('Choose an item to sell into this order.')
                  return
                }
                fulfillOrderMutation.mutate(orderId)
              }}
              onCancel={(orderId) => cancelOrderMutation.mutate(orderId)}
            />
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
              {tab === 'all'
                ? `Showing ${visibleListings.length} of ${listingTotal} listing${listingTotal === 1 ? '' : 's'}.`
                : `Showing ${tableListings.length} listing${tableListings.length === 1 ? '' : 's'}.`}
              {listingsTruncated && tab === 'all' ? ' More pages available.' : ''}
            </p>
            {tab === 'all' ? (
              <div className="market-pagination" aria-label="Listing pages">
                {Array.from({ length: Math.min(pageCount, 8) }, (_, index) => (
                  <button
                    key={index}
                    type="button"
                    className={page === index ? 'tab tab-active' : 'tab'}
                    data-testid={`market-page-${index + 1}`}
                    onClick={() => setPage(index)}
                  >
                    {index + 1}
                  </button>
                ))}
              </div>
            ) : null}
          </div>
        </div>

        <aside className="market-inspector" aria-label="Selected listing">
          {tab === 'orders' && selectedOrder ? (
            <OrderInspector order={selectedOrder} />
          ) : selectedListing ? (
            <ListingInspector
              listing={selectedListing}
              inventoryItem={inspectedInventoryItem}
              tab={tab}
              tradeDisabled={tradeDisabled}
              travelReason={travelReason}
              saleFeePercent={saleFeePercent}
              buyPending={buyMutation.isPending}
              cancelPending={cancelMutation.isPending}
              onBuy={() => buyMutation.mutate(selectedListing.id)}
              onCancel={() => cancelMutation.mutate(selectedListing.id)}
              onPlaceBuyOrder={() => {
                if (selectedListing.itemDefinitionId) {
                  setOrderDefinitionId(selectedListing.itemDefinitionId)
                }
                setTab('orders')
              }}
            />
          ) : tab === 'mine' && selectedItem ? (
            <article className="market-item-card">
              <h3 className="item-section-label">Item details</h3>
              <ItemDetail
                item={selectedItem}
                variant="market"
                showComparison={false}
                hideValue
                valueLabel="Vendor value"
              />
            </article>
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
            <th>Lvl</th>
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
  saleFeePercent,
  buyPending,
  cancelPending,
  onBuy,
  onCancel,
  onPlaceBuyOrder,
}: {
  listing: MarketListingResponse
  inventoryItem: InventoryItemResponse | null
  tab: MarketTab
  tradeDisabled: boolean
  travelReason: string
  saleFeePercent: number
  buyPending: boolean
  cancelPending: boolean
  onBuy: () => void
  onCancel: () => void
  onPlaceBuyOrder: () => void
}) {
  const buyDisabled = tradeDisabled || listing.ownListing || buyPending
  const buyReason = !listing.ownListing && tradeDisabled ? travelReason : listing.ownListing ? 'You cannot buy your own listing.' : undefined

  return (
    <>
      <MarketSellerBlock name={listing.sellerName} title="Player seller" testId="listing-seller" />
      <article className="market-item-card">
        <h3 className="item-section-label">Item details</h3>
        {inventoryItem ? (
          <ItemDetail
            item={inventoryItem}
            variant="market"
            showComparison={Boolean(inventoryItem.comparison)}
            hideValue
          />
        ) : (
          <div className="item-detail item-detail-market">
            <header className="item-tooltip-header">
              <span className={`item-icon-frame rarity-frame-${listing.rarity.toLowerCase()}`}>
                <ItemIcon item={listingIconSource(listing.itemType, listing.itemCode)} className="item-icon item-icon-inspector" />
              </span>
              <div className="item-detail-heading">
                <div className="item-detail-title-row">
                  <strong className={`item-name rarity-ink-${listing.rarity.toLowerCase()}`}>
                    {listing.displayName ?? listing.itemName}
                  </strong>
                  <RarityBadge rarity={listing.rarity} />
                </div>
                <p className="item-detail-kicker">
                  {formatRarity(listing.rarity)} {formatItemType(listing.itemType)}
                </p>
              </div>
            </header>
            <dl className="stat-list item-requirement-list">
              <StatRow label="Quantity" value={listing.quantity} />
            </dl>
          </div>
        )}
      </article>
      <section className="market-actions" aria-label="Listing actions">
        <dl className="stat-list">
          <StatRow
            label="List price"
            value={
              <span className="market-gold">
                <img src="/chrome/currency-gold.webp" alt="" />
                {listing.price} gold
              </span>
            }
          />
          <StatRow
            label="Sale fee"
            value={`${Math.ceil(listing.price * saleFeePercent)} gold (${Math.round(saleFeePercent * 100)}%)`}
          />
          <StatRow label="Time left" value="—" />
        </dl>
        <div className="inventory-inspector-actions">
          {tab === 'mine' ? (
            <MarketActionButton
              testId={`inspector-cancel-${listing.itemCode}`}
              icon="cancel"
              label="Cancel listing"
              hint="Remove this offer from the hall."
              disabled={tradeDisabled || cancelPending}
              title={!tradeDisabled ? undefined : travelReason}
              onClick={onCancel}
            />
          ) : (
            <MarketActionButton
              testId={`inspector-buy-${listing.itemCode}`}
              icon="buy"
              label="Buy item"
              hint="Purchase this item now."
              trailing={`${listing.price}g`}
              disabled={buyDisabled}
              title={buyReason}
              onClick={onBuy}
            />
          )}
          <Button
            type="button"
            className="market-action-row"
            data-testid="market-buy-order"
            disabled={tradeDisabled || !listing.itemDefinitionId}
            onClick={onPlaceBuyOrder}
          >
            <ActionGlyph name="order" />
            <span className="market-action-copy">
              <strong>Place buy order</strong>
              <small>Bid on this item definition.</small>
            </span>
            <ActionChevron />
          </Button>
          <ComingLaterButton className="market-action-row" data-testid="market-watchlist">
            <ActionGlyph name="watch" />
            <span className="market-action-copy">
              <strong>Add item to watchlist</strong>
              <small>Get price alerts for this item.</small>
            </span>
            <ActionChevron />
          </ComingLaterButton>
        </div>
      </section>
      <section className="market-orders" aria-labelledby="market-orders-heading">
        <div className="market-orders-header">
          <h3 id="market-orders-heading">Active buy orders</h3>
          <ComingLaterButton className="tab" data-testid="market-orders-view-all">
            View all
          </ComingLaterButton>
        </div>
        <p className="muted" data-testid="market-orders-count">
          Open the Buy orders tab to post or fill bids. Gold is escrowed when you create an order.
        </p>
      </section>
    </>
  )
}

function BuyOrdersBoard({
  orders,
  selectedId,
  listableItems,
  orderDefinitionId,
  orderQuantity,
  orderMaxPrice,
  reservedPreview,
  buyOrderFeePreview,
  listingFeePercent,
  fulfillItemId,
  fulfillQuantity,
  tradeDisabled,
  travelReason,
  onSelect,
  onDefinitionId,
  onQuantity,
  onMaxPrice,
  onFulfillItemId,
  onFulfillQuantity,
  onCreate,
  onFulfill,
  onCancel,
}: {
  orders: MarketBuyOrderResponse[]
  selectedId: string | null
  listableItems: InventoryItemResponse[]
  orderDefinitionId: string
  orderQuantity: number
  orderMaxPrice: number
  reservedPreview: number
  buyOrderFeePreview: number
  listingFeePercent: number
  fulfillItemId: string
  fulfillQuantity: number
  tradeDisabled: boolean
  travelReason: string
  onSelect: (id: string) => void
  onDefinitionId: (id: string) => void
  onQuantity: (value: number) => void
  onMaxPrice: (value: number) => void
  onFulfillItemId: (id: string) => void
  onFulfillQuantity: (value: number) => void
  onCreate: () => void
  onFulfill: (orderId: string) => void
  onCancel: (orderId: string) => void
}) {
  const definitionChoices = listableItems.filter(
    (item, index, all) => all.findIndex((candidate) => candidate.definitionId === item.definitionId) === index,
  )
  const selectedOrder = orders.find((order) => order.id === selectedId) ?? null
  const fulfillChoices = listableItems.filter((item) => !selectedOrder || item.code === selectedOrder.itemCode)

  return (
    <section className="market-sell" data-testid="buy-orders-board">
      <h3>Create buy order</h3>
      <form
        className="market-create-form"
        onSubmit={(event) => {
          event.preventDefault()
          onCreate()
        }}
      >
        <Field label="Item">
          <Select
            data-testid="buy-order-item-select"
            value={orderDefinitionId}
            onChange={(event) => onDefinitionId(event.target.value)}
          >
            <option value="">Select an item you know</option>
            {definitionChoices.map((item) => (
              <option key={item.definitionId} value={item.definitionId}>
                {item.displayName}
              </option>
            ))}
          </Select>
        </Field>
        <Field label="Quantity">
          <TextInput
            data-testid="buy-order-quantity"
            type="number"
            min={1}
            value={orderQuantity}
            onChange={(event) => onQuantity(Number(event.target.value))}
          />
        </Field>
        <Field label="Max unit price">
          <TextInput
            data-testid="buy-order-price"
            type="number"
            min={1}
            value={orderMaxPrice}
            onChange={(event) => onMaxPrice(Number(event.target.value))}
          />
        </Field>
        <p className="muted" data-testid="buy-order-fee-preview">
          Escrow {reservedPreview} gold plus a {buyOrderFeePreview} gold posting fee (
          {Math.round(listingFeePercent * 100)}%). The posting fee is not refunded.
        </p>
        <Button type="submit" data-testid="create-buy-order" disabled={tradeDisabled || !orderDefinitionId} title={!tradeDisabled ? undefined : travelReason}>
          Post buy order
        </Button>
      </form>

      {orders.length === 0 ? (
        <EmptyState testId="buy-orders-empty">No active buy orders.</EmptyState>
      ) : (
        <div className="market-table-wrap">
          <table className="market-table" data-testid="buy-orders">
            <thead>
              <tr>
                <th>Item</th>
                <th>Qty</th>
                <th>Bid</th>
                <th>Escrow</th>
                <th>Buyer</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.id} className={order.id === selectedId ? 'market-row-selected' : undefined}>
                  <td>
                    <button type="button" className="market-row-select" onClick={() => onSelect(order.id)}>
                      {order.itemName}
                    </button>
                  </td>
                  <td>
                    {order.remainingQuantity}/{order.originalQuantity}
                  </td>
                  <td>{order.maxUnitPrice}g</td>
                  <td>{order.reservedGold}g</td>
                  <td>{order.buyerName}</td>
                  <td>
                    {order.ownOrder ? (
                      <Button type="button" data-testid={`cancel-order-${order.itemCode}`} disabled={tradeDisabled} onClick={() => onCancel(order.id)}>
                        Cancel
                      </Button>
                    ) : (
                      <span className="muted">Fill below</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {selectedOrder && !selectedOrder.ownOrder ? (
        <form
          className="market-create-form"
          onSubmit={(event) => {
            event.preventDefault()
            onFulfill(selectedOrder.id)
          }}
        >
          <h3>Fulfill {selectedOrder.itemName}</h3>
          <Field label="Your item">
            <Select data-testid="fulfill-item-select" value={fulfillItemId} onChange={(event) => onFulfillItemId(event.target.value)}>
              <option value="">Select a matching stack</option>
              {fulfillChoices.map((item) => (
                <option key={item.id} value={item.id}>
                  {itemLabel(item)}
                </option>
              ))}
            </Select>
          </Field>
          <Field label="Quantity">
            <TextInput
              data-testid="fulfill-quantity"
              type="number"
              min={1}
              max={selectedOrder.remainingQuantity}
              value={fulfillQuantity}
              onChange={(event) => onFulfillQuantity(Number(event.target.value))}
            />
          </Field>
          <Button type="submit" data-testid="fulfill-buy-order" disabled={tradeDisabled || !fulfillItemId}>
            Sell to order
          </Button>
        </form>
      ) : null}
    </section>
  )
}

function OrderInspector({ order }: { order: MarketBuyOrderResponse }) {
  return (
    <article className="market-item-card" data-testid="order-inspector">
      <h3>{order.itemName}</h3>
      <dl className="stat-list">
        <StatRow label="Remaining" value={`${order.remainingQuantity} / ${order.originalQuantity}`} />
        <StatRow label="Max unit price" value={`${order.maxUnitPrice} gold`} />
        <StatRow label="Reserved gold" value={`${order.reservedGold} gold`} />
        <StatRow label="Buyer" value={order.buyerName} />
        <StatRow label="Status" value={order.status} />
      </dl>
    </article>
  )
}

function LockedFilter({ testId, label, value }: { testId: string; label: string; value: string }) {
  return (
    <Field label={label}>
      <Select data-testid={testId} disabled title="Coming later" className="coming-later" aria-disabled="true">
        <option>{value}</option>
      </Select>
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
              <MerchantPortrait code={merchant.portraitCode} name={merchant.name} />
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
          <SearchInput
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
                        <ItemIcon item={listingIconSource(item.itemType, item.itemCode)} />
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
            <MarketSellerBlock
              name={selectedMerchant.name}
              title={selectedMerchant.title}
              description={selectedMerchant.description}
              portraitCode={selectedMerchant.portraitCode}
              testId="merchant-identity"
            />
            <article className="market-item-card">
              <h3 className="item-section-label">Item details</h3>
              <ItemDetail
                item={stockAsInventoryItem(selectedStock)}
                variant="market"
                showComparison={false}
                hideValue
                showQuantity={false}
              />
            </article>
            <section className="market-actions" aria-label="Merchant purchase">
              <dl className="stat-list">
                <StatRow
                  label="Price"
                  value={
                    <span className="market-gold">
                      <img src="/chrome/currency-gold.webp" alt="" />
                      {selectedStock.sellPrice} gold
                    </span>
                  }
                />
              </dl>
              <Field label="Quantity">
                <TextInput
                  data-testid="merchant-buy-quantity"
                  type="number"
                  min={1}
                  max={merchantMaxQuantity}
                  value={merchantQuantity}
                  onChange={(event) => onQuantity(Number(event.target.value))}
                />
              </Field>
              <div className="inventory-inspector-actions">
                <MarketActionButton
                  testId={`buy-merchant-${selectedStock.itemCode}`}
                  icon="buy"
                  label={buyPending ? 'Buying…' : 'Buy item'}
                  hint="Purchase this item now."
                  trailing={`${selectedStock.sellPrice * merchantQuantity}g`}
                  disabled={tradeDisabled || buyPending}
                  title={buyDisabledReason}
                  onClick={onBuy}
                />
                <ComingLaterButton className="market-action-row" data-testid="merchant-buy-order">
                  <ActionGlyph name="order" />
                  <span className="market-action-copy">
                    <strong>Place buy order</strong>
                    <small>Buy this item at your price.</small>
                  </span>
                  <ActionChevron />
                </ComingLaterButton>
                <ComingLaterButton className="market-action-row" data-testid="merchant-watchlist">
                  <ActionGlyph name="watch" />
                  <span className="market-action-copy">
                    <strong>Add item to watchlist</strong>
                    <small>Get price alerts for this item.</small>
                  </span>
                  <ActionChevron />
                </ComingLaterButton>
              </div>
            </section>
          </>
        ) : (
          <EmptyState>Select a merchant good to inspect it.</EmptyState>
        )}
      </aside>
    </div>
  )
}

function MarketSellerBlock({
  name,
  title,
  description,
  portraitCode,
  testId,
}: {
  name: string
  title?: string
  description?: string
  portraitCode?: string
  testId?: string
}) {
  return (
    <section className="market-seller" data-testid={testId} aria-label="Seller">
      <div className="market-seller-head">
        <span className="merchant-portrait" aria-hidden="true">
          <MerchantPortrait code={portraitCode} name={name} />
        </span>
        <div>
          <strong>{name}</strong>
          {title ? <span className="muted">{title}</span> : null}
        </div>
      </div>
      {description ? <p className="market-seller-blurb">{description}</p> : null}
    </section>
  )
}

function MerchantPortrait({ code, name }: { code?: string; name: string }) {
  const src = merchantPortraitUrl(code)
  if (!src) {
    return <>{initials(name)}</>
  }
  return <img src={src} alt="" />
}

function MarketActionButton({
  testId,
  icon,
  label,
  hint,
  trailing,
  disabled,
  title,
  onClick,
}: {
  testId: string
  icon: 'buy' | 'cancel'
  label: string
  hint: string
  trailing?: string
  disabled?: boolean
  title?: string
  onClick: () => void
}) {
  return (
    <Button
      type="button"
      variant="secondary"
      className={icon === 'buy' ? 'market-action-row market-action-buy' : 'market-action-row'}
      data-testid={testId}
      disabled={disabled}
      title={title}
      onClick={onClick}
    >
      <ActionGlyph name={icon} />
      <span className="market-action-copy">
        <strong>{label}</strong>
        <small>{hint}</small>
      </span>
      {trailing ? <span className="market-action-trailing">{trailing}</span> : <ActionChevron />}
    </Button>
  )
}

function ActionGlyph({ name }: { name: 'buy' | 'cancel' | 'order' | 'watch' }) {
  if (name === 'watch') {
    return (
      <svg className="market-action-glyph" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
        <path d="M12 6c-5 0-9 4.2-10 6 1 1.8 5 6 10 6s9-4.2 10-6c-1-1.8-5-6-10-6Z" fill="none" stroke="currentColor" strokeWidth="1.7" />
        <circle cx="12" cy="12" r="2.4" fill="none" stroke="currentColor" strokeWidth="1.7" />
      </svg>
    )
  }
  if (name === 'order') {
    return (
      <svg className="market-action-glyph" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
        <path d="M6 7h12l-1.2 11.2H7.2Z" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinejoin="round" />
        <path d="M9 7V5.6A3 3 0 0 1 12 2.8 3 3 0 0 1 15 5.6V7" fill="none" stroke="currentColor" strokeWidth="1.7" />
      </svg>
    )
  }
  if (name === 'cancel') {
    return (
      <svg className="market-action-glyph" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
        <path d="M7 7 17 17M17 7 7 17" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
      </svg>
    )
  }
  return (
    <svg className="market-action-glyph" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
      <path d="M4 10h16l-1.4 9H5.4Z" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinejoin="round" />
      <path d="M8 10V8.2A4 4 0 0 1 12 4.4 4 4 0 0 1 16 8.2V10" fill="none" stroke="currentColor" strokeWidth="1.7" />
    </svg>
  )
}

function ActionChevron() {
  return (
    <svg className="market-action-chevron" viewBox="0 0 16 16" aria-hidden="true" focusable="false">
      <path d="M6 3.2 11 8 6 12.8" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
    </svg>
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
    accuracy: item.accuracy,
    criticalChance: item.criticalChance,
    dodge: item.dodge,
    strength: item.strength,
    agility: item.agility,
    endurance: item.endurance,
    perception: item.perception,
    staminaCostReduction: item.staminaCostReduction,
    affixes: [],
    comparison: null,
  }
}
