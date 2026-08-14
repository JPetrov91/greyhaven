// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { fetchInventory } from '../api/inventory'
import { fetchMarketListings, fetchOwnMarketListings } from '../api/market'
import type { InventoryItemResponse, InventoryResponse, LocationResponse, MarketListingResponse } from '../api/types'
import { fetchCurrentLocation } from '../api/world'
import { MarketPanel } from './MarketPanel'

vi.mock('../api/inventory', () => ({
  fetchInventory: vi.fn(),
}))

vi.mock('../api/market', () => ({
  fetchMarketListings: vi.fn(),
  fetchOwnMarketListings: vi.fn(),
  createMarketListing: vi.fn(),
  buyMarketListing: vi.fn(),
  cancelMarketListing: vi.fn(),
}))

vi.mock('../api/world', () => ({
  fetchCurrentLocation: vi.fn(),
}))

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

const marketLocation: LocationResponse = {
  id: 'loc-market',
  code: 'MARKET',
  name: 'Market',
  description: 'Safe market',
  safety: 'SAFE',
  region: 'Greyhaven',
  actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY', 'BROWSE_MARKET', 'CREATE_LISTING', 'BUY_ITEM', 'CANCEL_LISTING'],
}

const squareLocation: LocationResponse = {
  id: 'loc-square',
  code: 'CITY_SQUARE',
  name: 'City Square',
  description: 'Safe square',
  safety: 'SAFE',
  region: 'Greyhaven',
  actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY'],
}

function listing(overrides: Partial<MarketListingResponse> = {}): MarketListingResponse {
  return {
    id: 'listing-1',
    sellerCharacterId: 'seller-1',
    sellerName: 'Bram',
    itemInstanceId: 'item-1',
    itemCode: 'LEATHER_ARMOR',
    itemName: 'Leather Armor',
    itemType: 'ARMOR',
    rarity: 'UNCOMMON',
    quantity: 1,
    price: 80,
    status: 'ACTIVE',
    createdAt: '2026-08-13T10:00:00Z',
    soldAt: null,
    ownListing: false,
    ...overrides,
  }
}

function inventory(items: InventoryItemResponse[] = []): InventoryResponse {
  return {
    capacity: 40,
    usedSlots: items.length,
    items,
    equipment: {
      slots: {
        HEAD: null,
        CHEST: null,
        HANDS: null,
        LEGS: null,
        FEET: null,
        MAIN_HAND: null,
        OFF_HAND: null,
        AMULET: null,
        RING: null,
      },
    },
    derivedStats: {
      physicalDamage: 8,
      accuracy: 80,
      dodge: 10,
      criticalChance: 5,
      armor: 0,
    },
  }
}

function axeItem(): InventoryItemResponse {
  return {
    id: 'axe',
    definitionId: 'def-1',
    code: 'IRON_AXE',
    name: 'Iron Axe',
    displayName: 'Iron Axe',
    description: '',
    type: 'WEAPON',
    rarity: 'RARE',
    quantity: 1,
    requiredLevel: 1,
    requiredStrength: 0,
    requiredAgility: 0,
    requiredEndurance: 0,
    requiredPerception: 0,
    baseValue: 12,
    equipped: false,
    canEquip: true,
    twoHanded: false,
    legacy: false,
    equipmentSlot: 'MAIN_HAND',
    weaponFamily: 'AXE',
    armorCategory: null,
    usable: false,
    listedQuantity: 0,
    rolledWeaponDamage: 13,
    rolledArmorValue: null,
    weaponDamage: 13,
    armorValue: null,
    healAmount: null,
    affixes: [],
    comparison: null,
  }
}

function renderPanel(path = '/') {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <MemoryRouter initialEntries={[path]}>
      <QueryClientProvider client={queryClient}>
        <MarketPanel />
      </QueryClientProvider>
    </MemoryRouter>,
  )
}

describe('MarketPanel', () => {
  it('renders listings with item, rarity, seller, price, buy, and filters', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue(marketLocation)
    vi.mocked(fetchMarketListings).mockResolvedValue({ truncated: false, listings: [listing()] })
    vi.mocked(fetchOwnMarketListings).mockResolvedValue({ listings: [], truncated: false })
    vi.mocked(fetchInventory).mockResolvedValue(inventory())

    renderPanel()

    expect(await screen.findAllByText('Leather Armor')).not.toHaveLength(0)
    expect(screen.getAllByText('Uncommon').length).toBeGreaterThan(0)
    expect(screen.getAllByText(/Seller Bram/).length).toBeGreaterThan(0)
    expect(screen.getAllByText(/80 gold/).length).toBeGreaterThan(0)
    expect(screen.getByTestId('buy-listing-LEATHER_ARMOR')).toBeTruthy()
    expect(screen.getByTestId('market-type-filter')).toBeTruthy()
    expect(screen.getByTestId('market-search')).toBeTruthy()
    expect(screen.getByTestId('market-tab-all').textContent).toContain('All listings (1)')
    fireEvent.click(screen.getByTestId('market-tab-mine'))
    expect(await screen.findByTestId('own-listings-empty')).toBeTruthy()
  })

  it('inspects a selected listing without inventing stats', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue(marketLocation)
    vi.mocked(fetchMarketListings).mockResolvedValue({ truncated: false, listings: [listing()] })
    vi.mocked(fetchOwnMarketListings).mockResolvedValue({ listings: [], truncated: false })
    vi.mocked(fetchInventory).mockResolvedValue(inventory())

    renderPanel()

    expect(await screen.findByTestId('inspector-buy-LEATHER_ARMOR')).toBeTruthy()
    expect(screen.getByTestId('market-buy-order')).toHaveProperty('disabled', true)
    expect(screen.getByTestId('market-watchlist')).toHaveProperty('disabled', true)
    expect(screen.getByTestId('market-tab-orders')).toHaveProperty('disabled', true)
    expect(screen.getByTestId('market-tab-history')).toHaveProperty('disabled', true)
    expect(screen.getByText('List price')).toBeTruthy()
    expect(screen.queryByText('23h')).toBeNull()
  })

  it('tells the player to travel to the Market before trading', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue(squareLocation)
    vi.mocked(fetchMarketListings).mockResolvedValue({ listings: [], truncated: false })
    vi.mocked(fetchOwnMarketListings).mockResolvedValue({ listings: [], truncated: false })
    vi.mocked(fetchInventory).mockResolvedValue(inventory())

    renderPanel()

    expect(await screen.findByTestId('market-travel-hint')).toBeTruthy()
    expect(screen.getByTestId('market-travel-cta')).toBeTruthy()
    expect(screen.getByTestId('market-listings-empty').textContent).toContain('No player listings yet')
  })

  it('filters visible listings by search without hiding locked chrome', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue(marketLocation)
    vi.mocked(fetchMarketListings).mockResolvedValue({
      truncated: false,
      listings: [
        listing(),
        listing({
          id: 'listing-2',
          itemCode: 'IRON_ORE',
          itemName: 'Iron Ore',
          itemType: 'MATERIAL',
          rarity: 'COMMON',
          sellerName: 'Nira',
          price: 4,
        }),
      ],
    })
    vi.mocked(fetchOwnMarketListings).mockResolvedValue({ listings: [], truncated: false })
    vi.mocked(fetchInventory).mockResolvedValue(inventory())

    renderPanel()

    expect(await screen.findByText('Leather Armor')).toBeTruthy()
    fireEvent.change(screen.getByTestId('market-search'), { target: { value: 'iron' } })
    expect(screen.queryByText('Leather Armor')).toBeNull()
    expect(screen.getAllByText('Iron Ore').length).toBeGreaterThan(0)
    expect(screen.getByTestId('market-rarity-filter')).toHaveProperty('disabled', true)
    expect(screen.queryByTestId('market-listings-empty')).toBeNull()
  })

  it('preselects an inventory item from the listItem query', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue(marketLocation)
    vi.mocked(fetchMarketListings).mockResolvedValue({ listings: [], truncated: false })
    vi.mocked(fetchOwnMarketListings).mockResolvedValue({ listings: [], truncated: false })
    vi.mocked(fetchInventory).mockResolvedValue(inventory([axeItem()]))

    renderPanel('/?panel=market&listItem=axe')

    expect(await screen.findByTestId('market-item-select')).toHaveProperty('value', 'axe')
    expect(screen.getByTestId('market-tab-mine').getAttribute('aria-selected')).toBe('true')
  })
})
