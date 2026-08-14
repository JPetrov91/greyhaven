// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { fetchInventory } from '../api/inventory'
import { fetchMarketListings, fetchOwnMarketListings } from '../api/market'
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

describe('MarketPanel', () => {
  it('renders listings with item, rarity, seller, price, buy, and filters', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue({
      id: 'loc-market',
      code: 'MARKET',
      name: 'Market',
      description: 'Safe market',
      safety: 'SAFE',
      region: 'Greyhaven',
      actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY', 'BROWSE_MARKET', 'CREATE_LISTING', 'BUY_ITEM', 'CANCEL_LISTING'],
    })
    vi.mocked(fetchMarketListings).mockResolvedValue({
      truncated: false,
      listings: [
        {
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
        },
      ],
    })
    vi.mocked(fetchOwnMarketListings).mockResolvedValue({ listings: [], truncated: false })
    vi.mocked(fetchInventory).mockResolvedValue({
      capacity: 40,
      usedSlots: 0,
      items: [],
      equipment: { slots: {
        HEAD: null, CHEST: null, HANDS: null, LEGS: null, FEET: null,
        MAIN_HAND: null, OFF_HAND: null, AMULET: null, RING: null,
      } },
      derivedStats: {
        physicalDamage: 8,
        accuracy: 80,
        dodge: 10,
        criticalChance: 5,
        armor: 0,
      },
    })

    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    })

    render(
      <MemoryRouter>
        <QueryClientProvider client={queryClient}>
          <MarketPanel />
        </QueryClientProvider>
      </MemoryRouter>,
    )

    expect(await screen.findByText('Leather Armor')).toBeTruthy()
    expect(screen.getByText('Uncommon')).toBeTruthy()
    expect(screen.getByText(/Seller Bram/)).toBeTruthy()
    expect(screen.getByText(/80 gold/)).toBeTruthy()
    expect(screen.getByTestId('buy-listing-LEATHER_ARMOR')).toBeTruthy()
    expect(screen.getByTestId('market-type-filter')).toBeTruthy()
    expect(screen.getByTestId('own-listings-empty')).toBeTruthy()
  })

  it('tells the player to travel to the Market before trading', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue({
      id: 'loc-square',
      code: 'CITY_SQUARE',
      name: 'City Square',
      description: 'Safe square',
      safety: 'SAFE',
      region: 'Greyhaven',
      actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY'],
    })
    vi.mocked(fetchMarketListings).mockResolvedValue({ listings: [], truncated: false })
    vi.mocked(fetchOwnMarketListings).mockResolvedValue({ listings: [], truncated: false })
    vi.mocked(fetchInventory).mockResolvedValue({
      capacity: 40,
      usedSlots: 0,
      items: [],
      equipment: { slots: {
        HEAD: null, CHEST: null, HANDS: null, LEGS: null, FEET: null,
        MAIN_HAND: null, OFF_HAND: null, AMULET: null, RING: null,
      } },
      derivedStats: {
        physicalDamage: 8,
        accuracy: 80,
        dodge: 10,
        criticalChance: 5,
        armor: 0,
      },
    })

    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    })

    render(
      <MemoryRouter>
        <QueryClientProvider client={queryClient}>
          <MarketPanel />
        </QueryClientProvider>
      </MemoryRouter>,
    )

    expect(await screen.findByTestId('market-travel-hint')).toBeTruthy()
  })

  it('preselects an inventory item from the listItem query', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue({
      id: 'loc-market',
      code: 'MARKET',
      name: 'Market',
      description: 'Safe market',
      safety: 'SAFE',
      region: 'Greyhaven',
      actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY', 'BROWSE_MARKET', 'CREATE_LISTING', 'BUY_ITEM', 'CANCEL_LISTING'],
    })
    vi.mocked(fetchMarketListings).mockResolvedValue({ listings: [], truncated: false })
    vi.mocked(fetchOwnMarketListings).mockResolvedValue({ listings: [], truncated: false })
    vi.mocked(fetchInventory).mockResolvedValue({
      capacity: 40,
      usedSlots: 1,
      items: [
        {
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
        },
      ],
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
    })

    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    })

    render(
      <MemoryRouter initialEntries={['/?panel=market&listItem=axe']}>
        <QueryClientProvider client={queryClient}>
          <MarketPanel />
        </QueryClientProvider>
      </MemoryRouter>,
    )

    expect(await screen.findByTestId('market-item-select')).toHaveProperty('value', 'axe')
  })
})
