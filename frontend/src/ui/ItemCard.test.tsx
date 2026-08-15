// @vitest-environment jsdom

import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import type { InventoryItemResponse } from '../api/types'
import { ItemCard } from './ItemCard'

afterEach(() => {
  cleanup()
})

function sword(overrides: Partial<InventoryItemResponse> = {}): InventoryItemResponse {
  return {
    id: 'item-1',
    definitionId: 'def-1',
    code: 'IRON_AXE',
    name: 'Iron Axe',
    displayName: 'Iron Axe',
    description: 'A reliable blade',
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
    comparison: {
      slot: 'MAIN_HAND',
      equippedItemId: 'item-2',
      verdict: 'UPGRADE',
      deltas: [{ stat: 'Damage', equippedValue: 6, candidateValue: 13, delta: 7 }],
    },
    ...overrides,
  }
}

describe('ItemCard', () => {
  it('shows current vs candidate comparison on hover', () => {
    render(
      <ul>
        <ItemCard item={sword()} selected={false} onSelect={() => undefined} equippedName="Rusty Sword" />
      </ul>,
    )

    fireEvent.mouseEnter(screen.getByTestId('inventory-item-IRON_AXE'))
    const tooltip = screen.getByRole('tooltip')
    expect(tooltip.textContent).toContain('Iron Axe')
    expect(tooltip.textContent).toContain('Rare')
    expect(screen.getByTestId('comparison-IRON_AXE').textContent).toContain('Rusty Sword vs Iron Axe')
    expect(screen.getByTestId('comparison-IRON_AXE').textContent).toContain('6 → 13')
    expect(screen.getByTestId('comparison-IRON_AXE').textContent).toContain('+7')
    expect(screen.getByTestId('comparison-IRON_AXE').textContent).toContain('Upgrade')
    expect(screen.getByRole('button', { name: /Iron Axe/ }).getAttribute('aria-describedby')).toBeTruthy()
  })

  it('pins comparison on select without using a dialog role', () => {
    render(
      <ul>
        <ItemCard item={sword()} selected onSelect={() => undefined} equippedName="Rusty Sword" />
      </ul>,
    )
    expect(screen.getByTestId('comparison-IRON_AXE').textContent).toContain('Upgrade')
    expect(screen.queryByRole('dialog')).toBeNull()
    expect(screen.getByRole('button', { name: /Iron Axe/ }).getAttribute('aria-expanded')).toBe('true')
  })

  it('flips the tooltip when it would overflow the viewport', () => {
    const original = HTMLElement.prototype.getBoundingClientRect
    HTMLElement.prototype.getBoundingClientRect = function getBoundingClientRect() {
      return {
        x: 0,
        y: 0,
        top: 400,
        left: 10,
        bottom: window.innerHeight + 80,
        right: window.innerWidth + 80,
        width: 220,
        height: 90,
        toJSON() {
          return {}
        },
      }
    }

    try {
      render(
        <ul>
          <ItemCard item={sword()} selected={false} onSelect={() => undefined} />
        </ul>,
      )
      fireEvent.mouseEnter(screen.getByTestId('inventory-item-IRON_AXE'))
      expect(screen.getByRole('tooltip').className).toMatch(/tooltip-(top|left)/)
    } finally {
      HTMLElement.prototype.getBoundingClientRect = original
    }
  })

  it('closes a pinned comparison with Escape', () => {
    const onSelect = vi.fn()
    render(
      <ul>
        <ItemCard item={sword()} selected onSelect={onSelect} />
      </ul>,
    )

    fireEvent.keyDown(screen.getByTestId('inventory-item-IRON_AXE'), { key: 'Escape' })
    expect(onSelect).toHaveBeenCalled()
  })
})
