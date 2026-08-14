// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter, useLocation } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { fetchInventory } from '../api/inventory'
import type { InventoryItemResponse, InventoryResponse } from '../api/types'
import { EquipmentPanel } from './EquipmentPanel'

vi.mock('../api/inventory', () => ({
  fetchInventory: vi.fn(),
}))

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

function LocationProbe() {
  const location = useLocation()
  return <div data-testid="location-probe">{`${location.search}${location.hash}`}</div>
}

function sword(): InventoryItemResponse {
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
    equipped: true,
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
    comparison: null,
  }
}

function inventoryFixture(): InventoryResponse {
  const equipped = sword()
  return {
    capacity: 40,
    usedSlots: 1,
    items: [equipped],
    equipment: {
      slots: {
        HEAD: null,
        CHEST: null,
        HANDS: null,
        LEGS: null,
        FEET: null,
        MAIN_HAND: equipped.id,
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

describe('EquipmentPanel', () => {
  it('shows the paper doll and opens inventory filtered by slot', async () => {
    vi.mocked(fetchInventory).mockResolvedValue(inventoryFixture())
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <MemoryRouter>
        <QueryClientProvider client={queryClient}>
          <LocationProbe />
          <EquipmentPanel />
        </QueryClientProvider>
      </MemoryRouter>,
    )

    expect(await screen.findByTestId('equipped-weapon')).toHaveProperty('textContent', 'Rusty Sword')
    expect(screen.getByTestId('derived-damage').textContent).toBe('14')
    fireEvent.click(screen.getByRole('button', { name: 'Filter inventory by Main Hand' }))
    expect(screen.getByTestId('location-probe').textContent).toBe('?slot=MAIN_HAND#inventory')
  })
})
