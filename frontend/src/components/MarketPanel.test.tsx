// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
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
      <QueryClientProvider client={queryClient}>
        <MarketPanel />
      </QueryClientProvider>,
    )

    expect(await screen.findByText('Leather Armor')).toBeTruthy()
    expect(screen.getByText('UNCOMMON')).toBeTruthy()
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
      <QueryClientProvider client={queryClient}>
        <MarketPanel />
      </QueryClientProvider>,
    )

    expect(await screen.findByTestId('market-travel-hint')).toBeTruthy()
  })
})
