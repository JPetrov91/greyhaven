// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { fetchCurrentCraftingJob, fetchProfessions, fetchRecipes } from '../api/crafting'
import { fetchCurrentLocation } from '../api/world'
import { CraftingPanel } from './CraftingPanel'

vi.mock('../api/crafting', () => ({
  fetchProfessions: vi.fn(),
  fetchRecipes: vi.fn(),
  fetchCurrentCraftingJob: vi.fn(),
  startCraftingJob: vi.fn(),
  claimCraftingJob: vi.fn(),
}))

vi.mock('../api/world', () => ({
  fetchCurrentLocation: vi.fn(),
}))

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

describe('CraftingPanel', () => {
  it('shows profession ranks and locked recipes until the player is at the ward', async () => {
    vi.mocked(fetchCurrentLocation).mockResolvedValue({
      id: 'loc-1',
      code: 'CITY_SQUARE',
      name: 'City Square',
      description: '',
      safety: 'SAFE',
      region: 'Greyhaven',
      actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY'],
    })
    vi.mocked(fetchProfessions).mockResolvedValue([
      { profession: 'BLACKSMITH', rank: 1, xp: 0, xpToNextRank: 40, maxRank: false },
    ])
    vi.mocked(fetchRecipes).mockResolvedValue([
      {
        code: 'SMELT_IRON_INGOT',
        name: 'Smelt Iron Ingot',
        profession: 'BLACKSMITH',
        requiredProfessionRank: 1,
        requiredCharacterLevel: 1,
        goldCost: 0,
        durationSeconds: 30,
        outputItemCode: 'IRON_INGOT',
        outputItemName: 'Iron Ingot',
        outputQuantity: 1,
        minRarity: null,
        maxRarity: null,
        professionXp: 10,
        available: true,
        unavailableReason: null,
        inputs: [{ itemCode: 'IRON_ORE', itemName: 'Iron Ore', quantity: 3, availableQuantity: 3 }],
      },
    ])
    vi.mocked(fetchCurrentCraftingJob).mockResolvedValue(null)

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <MemoryRouter>
        <QueryClientProvider client={queryClient}>
          <CraftingPanel />
        </QueryClientProvider>
      </MemoryRouter>,
    )

    expect(await screen.findByTestId('profession-BLACKSMITH')).toBeTruthy()
    expect(screen.getByTestId('crafting-travel-hint')).toBeTruthy()
    expect(screen.getByTestId('start-recipe-SMELT_IRON_INGOT')).toHaveProperty('disabled', true)
  })
})
