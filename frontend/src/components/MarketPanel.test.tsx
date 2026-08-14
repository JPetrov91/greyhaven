// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { fetchInventory } from '../api/inventory'
import { fetchBuyOrders, fetchMarketListings, fetchMerchants, fetchOwnMarketListings } from '../api/market'
import type { InventoryItemResponse, InventoryResponse, LocationResponse, MarketListingResponse } from '../api/types'
import { fetchCurrentLocation } from '../api/world'
import { MarketPanel } from './MarketPanel'

vi.mock('../api/inventory', () => ({
  fetchInventory: vi.fn(),
}))

vi.mock('../api/market', () => ({
  fetchMarketListings: vi.fn(),
  fetchOwnMarketListings: vi.fn(),
  fetchMarketListingHistory: vi.fn(),
  fetchBuyOrders: vi.fn(),
  createMarketListing: vi.fn(),
  buyMarketListing: vi.fn(),
  cancelMarketListing: vi.fn(),
  createBuyOrder: vi.fn(),
  fulfillBuyOrder: vi.fn(),
  cancelBuyOrder: vi.fn(),
  fetchMerchants: vi.fn(),
  buyMerchantItem: vi.fn(),
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
  actions: [
    'INSPECT',
    'MOVE',
    'VIEW_NEARBY',
    'BROWSE_MARKET',
    'CREATE_LISTING',
    'BUY_ITEM',
    'CANCEL_LISTING',
    'CREATE_BUY_ORDER',
    'FULFILL_BUY_ORDER',
  ],
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
    itemDefinitionId: 'def-leather',
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

async function openPlayerMarket() {
  fireEvent.click(await screen.findByTestId('market-hub-player'))
}

describe('MarketPanel', () => {
  it('renders listings with item, rarity, seller, price, buy, and filters', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue(marketLocation)
    vi.mocked(fetchMarketListings).mockResolvedValue({ truncated: false, listings: [listing()] })
    vi.mocked(fetchOwnMarketListings).mockResolvedValue({ listings: [], truncated: false })
    vi.mocked(fetchInventory).mockResolvedValue(inventory())
    vi.mocked(fetchMerchants).mockResolvedValue({ merchants: [] })

    renderPanel()
    await openPlayerMarket()

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
    vi.mocked(fetchMerchants).mockResolvedValue({ merchants: [] })

    renderPanel()
    await openPlayerMarket()

    expect(await screen.findByTestId('inspector-buy-LEATHER_ARMOR')).toBeTruthy()
    expect(screen.getByTestId('listing-seller').textContent).toContain('Bram')
    expect(screen.getByTestId('listing-seller').className).toContain('market-seller')
    expect(screen.getByTestId('market-buy-order')).toHaveProperty('disabled', false)
    expect(screen.getByTestId('market-watchlist')).toHaveProperty('disabled', true)
    expect(screen.getByTestId('market-tab-orders')).toHaveProperty('disabled', false)
    expect(screen.getByTestId('market-tab-history')).toHaveProperty('disabled', false)
    expect(screen.getByText('List price')).toBeTruthy()
    expect(screen.queryByText('23h')).toBeNull()
  })

  it('tells the player to travel to the Market before trading', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue(squareLocation)
    vi.mocked(fetchMarketListings).mockResolvedValue({ listings: [], truncated: false })
    vi.mocked(fetchOwnMarketListings).mockResolvedValue({ listings: [], truncated: false })
    vi.mocked(fetchInventory).mockResolvedValue(inventory())
    vi.mocked(fetchMerchants).mockResolvedValue({ merchants: [] })

    renderPanel()
    await openPlayerMarket()

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
    vi.mocked(fetchMerchants).mockResolvedValue({ merchants: [] })

    renderPanel()
    await openPlayerMarket()

    expect(await screen.findAllByText('Leather Armor')).not.toHaveLength(0)
    fireEvent.change(screen.getByTestId('market-search'), { target: { value: 'iron' } })
    expect(screen.queryByText('Leather Armor')).toBeNull()
    expect(screen.getAllByText('Iron Ore').length).toBeGreaterThan(0)
    expect(screen.getByTestId('market-rarity-filter')).toHaveProperty('disabled', false)
    expect(screen.queryByTestId('market-listings-empty')).toBeNull()
  })

  it('opens the buy-order board with reserved gold', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue(marketLocation)
    vi.mocked(fetchMarketListings).mockResolvedValue({ truncated: false, listings: [] })
    vi.mocked(fetchOwnMarketListings).mockResolvedValue({ listings: [], truncated: false })
    vi.mocked(fetchInventory).mockResolvedValue(inventory([axeItem()]))
    vi.mocked(fetchMerchants).mockResolvedValue({ merchants: [] })
    vi.mocked(fetchBuyOrders).mockResolvedValue({
      truncated: false,
      page: 0,
      size: 20,
      total: 1,
      orders: [
        {
          id: 'order-1',
          buyerCharacterId: 'buyer-1',
          buyerName: 'Nira',
          itemDefinitionId: 'def-1',
          itemCode: 'IRON_ORE',
          itemName: 'Iron Ore',
          itemType: 'MATERIAL',
          remainingQuantity: 8,
          originalQuantity: 10,
          maxUnitPrice: 12,
          reservedGold: 96,
          postingFeePaid: 2,
          status: 'ACTIVE',
          createdAt: '2026-08-15T10:00:00Z',
          ownOrder: false,
        },
      ],
    })

    renderPanel()
    await openPlayerMarket()
    fireEvent.click(await screen.findByTestId('market-tab-orders'))

    expect(await screen.findByTestId('buy-orders')).toBeTruthy()
    expect(screen.getByTestId('buy-orders').textContent).toContain('Iron Ore')
    expect(screen.getByTestId('buy-orders').textContent).toContain('96g')
    expect(screen.getByTestId('create-buy-order')).toHaveProperty('disabled', true)
    fireEvent.change(screen.getByTestId('buy-order-item-select'), { target: { value: 'def-1' } })
    expect(screen.getByTestId('create-buy-order')).toHaveProperty('disabled', false)
  })

  it('preselects an inventory item from the listItem query', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue(marketLocation)
    vi.mocked(fetchMarketListings).mockResolvedValue({ listings: [], truncated: false })
    vi.mocked(fetchOwnMarketListings).mockResolvedValue({ listings: [], truncated: false })
    vi.mocked(fetchInventory).mockResolvedValue(inventory([axeItem()]))
    vi.mocked(fetchMerchants).mockResolvedValue({ merchants: [] })

    renderPanel('/?panel=market&listItem=axe')

    expect(await screen.findByTestId('market-item-select')).toHaveProperty('value', 'axe')
    expect(screen.getByTestId('market-tab-mine').getAttribute('aria-selected')).toBe('true')
  })

  it('opens weaponsmith stock inside the merchants hub', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue(marketLocation)
    vi.mocked(fetchMarketListings).mockResolvedValue({ listings: [], truncated: false })
    vi.mocked(fetchOwnMarketListings).mockResolvedValue({ listings: [], truncated: false })
    vi.mocked(fetchInventory).mockResolvedValue(inventory())
    vi.mocked(fetchMerchants).mockResolvedValue({
      merchants: [
        {
          id: 'm-1',
          code: 'WEAPONSMITH',
          name: 'Edric Varn',
          title: 'Greyhaven Weaponsmith',
          description: 'Honest steel.',
          merchantType: 'WEAPONSMITH',
          portraitCode: 'edric-varn',
          stock: [
            {
              itemDefinitionId: 'def-sword',
              itemCode: 'RUSTY_SWORD',
              itemName: 'Rusty Sword',
              description: 'A notched starter blade.',
              itemType: 'WEAPON',
              rarity: 'COMMON',
              sellPrice: 7,
              availabilityType: 'UNLIMITED',
              requiredLevel: 1,
              weaponDamage: 6,
              armorValue: null,
              healAmount: null,
              twoHanded: false,
              equipmentSlot: 'MAIN_HAND',
              weaponFamily: 'SWORD',
              armorCategory: null,
              requiredStrength: 0,
              requiredAgility: 0,
              requiredEndurance: 0,
              requiredPerception: 0,
              accuracy: 4,
              criticalChance: 1,
            },
          ],
        },
      ],
    })

    renderPanel()

    expect(await screen.findByTestId('merchant-WEAPONSMITH')).toBeTruthy()
    expect(screen.getByText('Edric Varn')).toBeTruthy()
    expect(screen.getByTestId('merchant-stock-RUSTY_SWORD')).toBeTruthy()
    expect(await screen.findByTestId('buy-merchant-RUSTY_SWORD')).toBeTruthy()
    expect(screen.getAllByText('7g').length).toBeGreaterThan(0)
    const seller = screen.getByTestId('merchant-identity')
    expect(seller.textContent).toContain('Edric Varn')
    expect(seller.textContent).toContain('Greyhaven Weaponsmith')
    expect(seller.textContent).toContain('Honest steel.')
    expect(seller.className).toContain('market-seller')
    expect(screen.getByText('Rusty Sword', { selector: '.item-name' }).closest('.market-item-card')).toBeTruthy()
    expect(screen.getByText('Damage')).toBeTruthy()
    expect(screen.getByText('+4 Accuracy')).toBeTruthy()
    expect(screen.getByText('Required Level')).toBeTruthy()
    expect(screen.queryByTestId('market-listings')).toBeNull()
  })
})
