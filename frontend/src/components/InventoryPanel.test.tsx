// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, useLocation } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { fetchCharacter } from '../api/character'
import { fetchInventory } from '../api/inventory'
import { sellToMerchant } from '../api/market'
import type { CharacterResponse, InventoryItemResponse, InventoryResponse, LocationResponse } from '../api/types'
import { fetchCurrentLocation } from '../api/world'
import { InventoryPanel } from './InventoryPanel'
import { ToastProvider } from '../ui/ToastRegion'

vi.mock('../api/inventory', () => ({
  fetchInventory: vi.fn(),
  equipItem: vi.fn(),
  unequipItem: vi.fn(),
  useItem: vi.fn(),
}))

vi.mock('../api/character', () => ({
  fetchCharacter: vi.fn(),
}))

vi.mock('../api/market', () => ({
  sellToMerchant: vi.fn(),
}))

vi.mock('../api/world', () => ({
  fetchCurrentLocation: vi.fn(),
}))

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

beforeEach(() => {
  vi.mocked(fetchCurrentLocation).mockResolvedValue({
    id: 'loc-1',
    code: 'CITY_SQUARE',
    name: 'City Square',
    description: 'Safe square',
    safety: 'SAFE',
    region: 'Greyhaven',
    actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY'],
  })
})

function LocationProbe() {
  const location = useLocation()
  return <div data-testid="location-probe">{`${location.search}${location.hash}`}</div>
}

function characterFixture(): CharacterResponse {
  return {
    id: 'char-1',
    accountId: 'acc-1',
    name: 'Hero',
    level: 1,
    experience: 0,
    strength: 5,
    agility: 5,
    endurance: 5,
    perception: 5,
    currentHealth: 100,
    maxHealth: 100,
    currentStamina: 50,
    maxStamina: 50,
    gold: 1250,
    unspentAttributePoints: 0,
    currentLocationId: 'loc-1',
    derivedStats: {
      physicalDamage: 14,
      accuracy: 83,
      dodge: 8,
      criticalChance: 7,
      armor: 3,
    },
    progression: {
      level: 1,
      totalExperience: 0,
      experienceIntoCurrentLevel: 0,
      experienceRequiredForNextLevel: 100,
      experienceRemaining: 100,
      progressPercent: 0,
      maxLevel: false,
    },
    createdAt: '2026-08-14T00:00:00Z',
    updatedAt: '2026-08-14T00:00:00Z',
  }
}

function item(overrides: Partial<InventoryItemResponse>): InventoryItemResponse {
  return {
    id: 'item-1',
    definitionId: 'def-1',
    code: 'RUSTY_SWORD',
    name: 'Rusty Sword',
    displayName: 'Rusty Sword',
    description: 'A blade',
    type: 'WEAPON',
    rarity: 'COMMON',
    quantity: 1,
    requiredLevel: 1,
    requiredStrength: 0,
    requiredAgility: 0,
    requiredEndurance: 0,
    requiredPerception: 0,
    baseValue: 5,
    merchantBuyPrice: 3,
    equipped: false,
    canEquip: true,
    twoHanded: false,
    legacy: true,
    equipmentSlot: 'MAIN_HAND',
    weaponFamily: 'SWORD',
    armorCategory: null,
    usable: false,
    listedQuantity: 0,
    rolledWeaponDamage: 6,
    rolledArmorValue: null,
    weaponDamage: 6,
    armorValue: null,
    healAmount: null,
    affixes: [],
    comparison: {
      slot: 'MAIN_HAND',
      equippedItemId: 'item-2',
      verdict: 'UPGRADE',
      deltas: [{ stat: 'Damage', equippedValue: 6, candidateValue: 13, delta: 7 }],
    },
    ...overrides,
  }
}

function inventoryFixture(items: InventoryItemResponse[]): InventoryResponse {
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
        MAIN_HAND: items.find((entry) => entry.equipped && entry.equipmentSlot === 'MAIN_HAND')?.id ?? null,
        OFF_HAND: null,
        AMULET: null,
        RING: null,
      },
    },
    derivedStats: {
      physicalDamage: 14,
      accuracy: 83,
      dodge: 8,
      criticalChance: 7,
      armor: 3,
    },
  }
}

function renderPanel() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <MemoryRouter>
      <QueryClientProvider client={queryClient}>
        <ToastProvider>
          <LocationProbe />
          <InventoryPanel />
        </ToastProvider>
      </QueryClientProvider>
    </MemoryRouter>,
  )
}

describe('InventoryPanel', () => {
  it('filters by type and sorts by rarity', async () => {
    vi.mocked(fetchCharacter).mockResolvedValue(characterFixture())
    vi.mocked(fetchInventory).mockResolvedValue(
      inventoryFixture([
        item({
          id: 'sword',
          code: 'RUSTY_SWORD',
          displayName: 'Rusty Sword',
          type: 'WEAPON',
          rarity: 'COMMON',
        }),
        item({
          id: 'axe',
          code: 'IRON_AXE',
          displayName: 'Sharp Iron Axe',
          type: 'WEAPON',
          rarity: 'RARE',
          legacy: false,
        }),
        item({
          id: 'amulet',
          code: 'COPPER_AMULET',
          displayName: 'Copper Amulet',
          type: 'ACCESSORY',
          rarity: 'UNCOMMON',
          equipmentSlot: 'AMULET',
          weaponFamily: null,
          weaponDamage: null,
        }),
      ]),
    )

    renderPanel()

    expect(await screen.findByTestId('inventory-item-RUSTY_SWORD')).toBeTruthy()
    expect(screen.getByRole('group', { name: 'Item type' })).toBeTruthy()
    expect(screen.getByTestId('inventory-item-COPPER_AMULET')).toBeTruthy()

    fireEvent.change(screen.getByTestId('inventory-type-select'), { target: { value: 'WEAPON' } })
    expect(screen.getByTestId('inventory-item-RUSTY_SWORD')).toBeTruthy()
    expect(screen.getByTestId('inventory-item-IRON_AXE')).toBeTruthy()
    expect(screen.queryByTestId('inventory-item-COPPER_AMULET')).toBeNull()

    fireEvent.change(screen.getByTestId('inventory-sort'), { target: { value: 'rarity' } })
    const weapons = screen.getAllByTestId(/inventory-item-/)
    expect(weapons[0].getAttribute('data-testid')).toBe('inventory-item-IRON_AXE')
    expect(weapons[1].getAttribute('data-testid')).toBe('inventory-item-RUSTY_SWORD')
  })

  it('shows server comparison when an item is selected', async () => {
    vi.mocked(fetchCharacter).mockResolvedValue(characterFixture())
    vi.mocked(fetchInventory).mockResolvedValue(
      inventoryFixture([
        item({
          id: 'axe',
          code: 'IRON_AXE',
          displayName: 'Iron Axe',
          rarity: 'UNCOMMON',
          weaponDamage: 13,
          canEquip: true,
          equipped: false,
        }),
      ]),
    )

    renderPanel()

    fireEvent.click(await screen.findByLabelText(/Iron Axe/))
    expect(screen.getByTestId('comparison-IRON_AXE').textContent).toContain('Damage')
    expect(screen.getByTestId('comparison-IRON_AXE').textContent).toContain('6 → 13')
    expect(screen.getByTestId('comparison-IRON_AXE').textContent).toContain('+7')
    expect(screen.getByTestId('comparison-IRON_AXE').textContent).toContain('Upgrade')
    expect(screen.getByTestId('equip-IRON_AXE')).toBeTruthy()
  })

  it('marks requirement-locked gear as unusable', async () => {
    vi.mocked(fetchCharacter).mockResolvedValue(characterFixture())
    vi.mocked(fetchInventory).mockResolvedValue(
      inventoryFixture([
        item({
          id: 'helm',
          code: 'IRON_HELM',
          displayName: 'Iron Helm',
          type: 'ARMOR',
          rarity: 'UNCOMMON',
          equipmentSlot: 'HEAD',
          weaponFamily: null,
          armorCategory: 'HEAVY',
          canEquip: false,
          weaponDamage: null,
          armorValue: 4,
          requiredStrength: 8,
          requiredEndurance: 6,
        }),
      ]),
    )

    renderPanel()

    const row = await screen.findByTestId('inventory-item-IRON_HELM')
    expect(row.className).toContain('inventory-item-unusable')
    expect(row.textContent).toContain('Unusable')
    fireEvent.click(screen.getByLabelText(/Iron Helm/))
    expect(screen.getByTestId('equip-IRON_HELM')).toHaveProperty('disabled', true)
  })

  it('exposes rarity on the selected slot', async () => {
    vi.mocked(fetchCharacter).mockResolvedValue(characterFixture())
    vi.mocked(fetchInventory).mockResolvedValue(
      inventoryFixture([
        item({
          id: 'axe',
          code: 'IRON_AXE',
          displayName: 'Iron Axe',
          rarity: 'RARE',
        }),
      ]),
    )

    renderPanel()

    const row = await screen.findByTestId('inventory-item-IRON_AXE')
    expect(row.className).toContain('inventory-slot-rare')
    expect(row.textContent).toContain('Rare')
  })

  it('filters by equipment slot from the slot control', async () => {
    vi.mocked(fetchCharacter).mockResolvedValue(characterFixture())
    vi.mocked(fetchInventory).mockResolvedValue(
      inventoryFixture([
        item({
          id: 'sword',
          code: 'RUSTY_SWORD',
          displayName: 'Rusty Sword',
          type: 'WEAPON',
          equipmentSlot: 'MAIN_HAND',
        }),
        item({
          id: 'helm',
          code: 'IRON_HELM',
          displayName: 'Iron Helm',
          type: 'ARMOR',
          equipmentSlot: 'HEAD',
          weaponFamily: null,
          weaponDamage: null,
          armorValue: 4,
        }),
      ]),
    )

    renderPanel()

    expect(await screen.findByTestId('inventory-item-RUSTY_SWORD')).toBeTruthy()
    fireEvent.change(screen.getByTestId('inventory-slot-filter'), { target: { value: 'HEAD' } })
    expect(screen.queryByTestId('inventory-item-RUSTY_SWORD')).toBeNull()
    expect(screen.getByTestId('inventory-item-IRON_HELM')).toBeTruthy()
  })

  it('shows an empty quest tab and coming-later bag actions', async () => {
    vi.mocked(fetchCharacter).mockResolvedValue(characterFixture())
    vi.mocked(fetchInventory).mockResolvedValue(inventoryFixture([item({})]))

    renderPanel()

    fireEvent.click(await screen.findByRole('button', { name: 'Quest' }))
    expect(screen.getByTestId('inventory-empty').textContent).toBe('No quest items.')
    expect((screen.getByTestId('inventory-open-stash') as HTMLButtonElement).disabled).toBe(true)
    expect((screen.getByTestId('inventory-loadouts') as HTMLButtonElement).disabled).toBe(true)
    expect((screen.getByTestId('inventory-stack-all') as HTMLButtonElement).disabled).toBe(true)
    expect(screen.getByTestId('inventory-gold').textContent).toContain('1,250')
  })

  it('sends a selected item to the marketplace sell form', async () => {
    vi.mocked(fetchCharacter).mockResolvedValue(characterFixture())
    vi.mocked(fetchInventory).mockResolvedValue(
      inventoryFixture([
        item({
          id: 'axe',
          code: 'IRON_AXE',
          displayName: 'Iron Axe',
          equipped: false,
          comparison: null,
        }),
      ]),
    )

    renderPanel()

    fireEvent.click(await screen.findByLabelText(/Iron Axe/))
    fireEvent.click(screen.getByTestId('sell-IRON_AXE'))
    expect(screen.getByTestId('location-probe').textContent).toContain('panel=market')
    expect(screen.getByTestId('location-probe').textContent).toContain('listItem=axe')
  })

  it('shows a merchant offer and sells immediately', async () => {
    vi.mocked(fetchCharacter).mockResolvedValue(characterFixture())
    vi.mocked(fetchCurrentLocation).mockResolvedValue({
      id: 'loc-market',
      code: 'MARKET',
      name: 'Market',
      description: 'Safe market',
      safety: 'SAFE',
      region: 'Greyhaven',
      actions: ['CREATE_LISTING', 'BUY_ITEM'],
    } satisfies LocationResponse)
    vi.mocked(fetchInventory).mockResolvedValue(
      inventoryFixture([
        item({
          id: 'axe',
          code: 'IRON_AXE',
          displayName: 'Iron Axe',
          equipped: false,
          comparison: null,
          merchantBuyPrice: 16,
        }),
      ]),
    )
    vi.mocked(sellToMerchant).mockResolvedValue({
      itemInstanceId: 'axe',
      itemCode: 'IRON_AXE',
      itemName: 'Iron Axe',
      quantity: 1,
      goldAwarded: 16,
      goldRemaining: 1266,
    })

    renderPanel()

    fireEvent.click(await screen.findByLabelText(/Iron Axe/))
    expect(screen.getByTestId('merchant-offer-IRON_AXE').textContent).toContain('16 Gold')
    const sellNow = await screen.findByRole('button', { name: 'Sell Now' })
    await waitFor(() => expect(sellNow).toHaveProperty('disabled', false))
    fireEvent.click(sellNow)
    await waitFor(() => expect(sellToMerchant).toHaveBeenCalledWith('axe', 1))
  })

  it('auto-sorts by rarity', async () => {
    vi.mocked(fetchCharacter).mockResolvedValue(characterFixture())
    vi.mocked(fetchInventory).mockResolvedValue(
      inventoryFixture([
        item({ id: 'sword', code: 'RUSTY_SWORD', displayName: 'Rusty Sword', rarity: 'COMMON' }),
        item({ id: 'axe', code: 'IRON_AXE', displayName: 'Iron Axe', rarity: 'RARE', comparison: null }),
      ]),
    )

    renderPanel()
    await screen.findByTestId('inventory-item-RUSTY_SWORD')
    fireEvent.click(screen.getByTestId('inventory-auto-sort'))
    const rows = screen.getAllByTestId(/inventory-item-/)
    expect(rows[0].getAttribute('data-testid')).toBe('inventory-item-IRON_AXE')
  })

  it('fills remaining bag slots and skips self-comparison', async () => {
    vi.mocked(fetchCharacter).mockResolvedValue(characterFixture())
    vi.mocked(fetchInventory).mockResolvedValue(
      inventoryFixture([
        item({
          id: 'sword',
          code: 'RUSTY_SWORD',
          displayName: 'Rusty Sword',
          equipped: true,
          comparison: {
            slot: 'MAIN_HAND',
            equippedItemId: 'sword',
            verdict: 'SAME',
            deltas: [{ stat: 'Damage', equippedValue: 6, candidateValue: 6, delta: 0 }],
          },
        }),
        item({
          id: 'axe',
          code: 'IRON_AXE',
          displayName: 'Iron Axe',
          rarity: 'RARE',
          equipped: false,
        }),
      ]),
    )

    renderPanel()

    expect(await screen.findByTestId('inventory-item-RUSTY_SWORD')).toBeTruthy()
    expect(screen.getAllByTestId('inventory-empty-slot')).toHaveLength(38)
    expect(screen.queryByTestId('comparison-RUSTY_SWORD')).toBeNull()
    fireEvent.click(screen.getByLabelText(/Iron Axe/))
    expect(screen.getByTestId('comparison-IRON_AXE')).toBeTruthy()
  })
})
