// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter, useLocation } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { fetchInventory } from '../api/inventory'
import type { InventoryItemResponse, InventoryResponse } from '../api/types'
import { EquipmentPanel } from './EquipmentPanel'
import { ToastProvider } from '../ui/ToastRegion'

vi.mock('../api/inventory', () => ({
  fetchInventory: vi.fn(),
  equipItem: vi.fn(),
  unequipItem: vi.fn(),
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

function spareBlade(): InventoryItemResponse {
  return {
    ...sword(),
    id: 'item-2',
    definitionId: 'def-2',
    code: 'IRON_BLADE',
    name: 'Iron Blade',
    displayName: 'Iron Blade',
    equipped: false,
    comparison: {
      slot: 'MAIN_HAND',
      equippedItemId: 'item-1',
      verdict: 'UPGRADE',
      deltas: [{ stat: 'Damage', equippedValue: 6, candidateValue: 9, delta: 3 }],
    },
    weaponDamage: 9,
    rolledWeaponDamage: 9,
  }
}

function inventoryFixture(): InventoryResponse {
  const equipped = sword()
  return {
    capacity: 40,
    usedSlots: 2,
    items: [equipped, spareBlade()],
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

function renderPanel() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  render(
    <MemoryRouter>
      <QueryClientProvider client={queryClient}>
        <ToastProvider>
          <LocationProbe />
          <EquipmentPanel />
        </ToastProvider>
      </QueryClientProvider>
    </MemoryRouter>,
  )
}

describe('EquipmentPanel', () => {
  it('inspects equipped gear in place and keeps the player on the equipment view', async () => {
    vi.mocked(fetchInventory).mockResolvedValue(inventoryFixture())
    renderPanel()

    expect(await screen.findByTestId('equipped-weapon')).toHaveProperty('textContent', 'Rusty Sword')
    expect(screen.getByTestId('derived-damage').textContent).toBe('14')
    expect(screen.getByTestId('unequip-RUSTY_SWORD')).toBeTruthy()
    expect(screen.getByTestId('equipment-loadout')).toHaveProperty('disabled', true)
    expect(screen.getByTestId('equipment-character-stage')).toBeTruthy()

    fireEvent.click(screen.getByRole('button', { name: 'Select Main Hand slot' }))
    expect(screen.getByTestId('location-probe').textContent).toBe('')
    fireEvent.click(screen.getByTestId('equipment-open-inventory'))
    expect(screen.getByTestId('location-probe').textContent).toBe('?slot=MAIN_HAND#inventory')
  })

  it('shows bag candidates and a locked inspector for future slots', async () => {
    vi.mocked(fetchInventory).mockResolvedValue(inventoryFixture())
    renderPanel()

    expect(await screen.findByTestId('equipment-candidate-IRON_BLADE')).toBeTruthy()
    fireEvent.click(screen.getByTestId('equipment-candidate-IRON_BLADE'))
    expect(screen.getByTestId('equip-IRON_BLADE')).toBeTruthy()
    expect(screen.getByTestId('comparison-IRON_BLADE')).toBeTruthy()

    fireEvent.click(screen.getByTestId('equipment-slot-SHOULDERS'))
    expect(screen.getByTestId('equipment-slot-locked').textContent).toBe('This slot is coming later.')
  })
})
