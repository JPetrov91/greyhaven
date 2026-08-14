// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { fetchInventory } from '../api/inventory'
import type { InventoryItemResponse, InventoryResponse } from '../api/types'
import { InventoryPanel } from './InventoryPanel'
import { ToastProvider } from '../ui/ToastRegion'

vi.mock('../api/inventory', () => ({
  fetchInventory: vi.fn(),
  equipItem: vi.fn(),
  unequipItem: vi.fn(),
  useItem: vi.fn(),
}))

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

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
        deltas: [
          { stat: 'Damage', equippedValue: 6, candidateValue: 13, delta: 7 },
        ],
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
          <InventoryPanel />
        </ToastProvider>
      </QueryClientProvider>
    </MemoryRouter>,
  )
}

describe('InventoryPanel', () => {
  it('filters by type and sorts by rarity', async () => {
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

    fireEvent.click(screen.getByRole('button', { name: 'Weapon' }))
    expect(screen.getByTestId('inventory-item-RUSTY_SWORD')).toBeTruthy()
    expect(screen.getByTestId('inventory-item-IRON_AXE')).toBeTruthy()
    expect(screen.queryByTestId('inventory-item-COPPER_AMULET')).toBeNull()

    fireEvent.change(screen.getByTestId('inventory-sort'), { target: { value: 'rarity' } })
    const weapons = screen.getAllByTestId(/inventory-item-/)
    expect(weapons[0].getAttribute('data-testid')).toBe('inventory-item-IRON_AXE')
    expect(weapons[1].getAttribute('data-testid')).toBe('inventory-item-RUSTY_SWORD')
  })

  it('shows server comparison when an item is selected', async () => {
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

    fireEvent.click(await screen.findByText('Iron Axe'))
    expect(screen.getByTestId('comparison-IRON_AXE').textContent).toContain('Damage')
    expect(screen.getByTestId('comparison-IRON_AXE').textContent).toContain('6 → 13')
    expect(screen.getByTestId('comparison-IRON_AXE').textContent).toContain('+7')
    expect(screen.getByTestId('comparison-IRON_AXE').textContent).toContain('Upgrade')
  })

  it('marks requirement-locked gear as unusable', async () => {
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
    expect(screen.getByTestId('equip-IRON_HELM')).toHaveProperty('disabled', true)
  })

  it('exposes rarity as a labeled chip', async () => {
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
    expect(row.querySelector('.rarity-rare')?.textContent).toBe('Rare')
    expect(row.querySelector('.rarity-ink-rare')).toBeTruthy()
  })

  it('filters by equipment slot from the slot control', async () => {
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
})
