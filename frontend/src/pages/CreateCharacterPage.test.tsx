// @vitest-environment jsdom

import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { fetchCharacterRoster } from '../api/character'
import { CreateCharacterPage } from './CreateCharacterPage'

const refreshMe = vi.fn()
const logout = vi.fn()

vi.mock('../auth/AuthContext', () => ({
  useAuth: () => ({
    refreshMe,
    logout,
  }),
}))

vi.mock('../api/character', () => ({
  createCharacter: vi.fn(),
  selectCharacter: vi.fn(),
  fetchCharacterRoster: vi.fn().mockResolvedValue({
    slots: [0, 1, 2].map((slotIndex) => ({
      slotIndex,
      empty: true,
      characterId: null,
      name: null,
      gender: null,
      avatarCode: null,
      level: 0,
      gold: 0,
      currentLocationId: null,
      locationName: null,
    })),
  }),
  checkCharacterNameAvailable: vi.fn().mockResolvedValue({ available: true }),
}))

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

function renderPage() {
  return render(
    <MemoryRouter>
      <CreateCharacterPage />
    </MemoryRouter>,
  )
}

describe('CreateCharacterPage', () => {
  beforeEach(() => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: true,
        json: async () => ({ available: true }),
      }),
    )
  })

  it('keeps the e2e contract and disables enter until a name is typed', () => {
    renderPage()

    expect(screen.getByTestId('create-character-page')).toBeTruthy()
    expect(screen.getByTestId('character-slot-bar')).toBeTruthy()
    expect(screen.getByTestId('create-character-form')).toBeTruthy()
    expect(screen.getByTestId('character-name')).toBeTruthy()
    expect(screen.getByTestId('create-character-submit')).toBeTruthy()
    expect((screen.getByTestId('create-character-submit') as HTMLButtonElement).disabled).toBe(true)

    fireEvent.change(screen.getByTestId('character-name'), { target: { value: 'RagnarIronfist' } })
    expect((screen.getByTestId('create-character-submit') as HTMLButtonElement).disabled).toBe(false)
    expect(screen.getByTestId('create-character-summary-name').textContent).toBe('RagnarIronfist')
  })

  it('fills a thematic name from the randomizer', () => {
    renderPage()
    fireEvent.click(screen.getByTestId('character-name-randomize'))
    const value = (screen.getByTestId('character-name') as HTMLInputElement).value
    expect(value).toMatch(/^[\p{L}\p{N}]+(?: [\p{L}\p{N}]+)*$/u)
    expect(value.length).toBeGreaterThanOrEqual(3)
    expect(value).toContain(' ')
  })

  it('randomizes a female name after switching gender', () => {
    renderPage()
    fireEvent.click(screen.getByTestId('character-gender-female'))
    fireEvent.click(screen.getByTestId('character-name-randomize'))
    const value = (screen.getByTestId('character-name') as HTMLInputElement).value
    expect(value).toMatch(
      /^(Morwen|Seren|Veyra|Isolde|Lyra|Maelis|Nyx|Elara|Rowena|Ysabel) /,
    )
  })

  it('swaps the avatar catalog with gender and updates the selected portrait', () => {
    renderPage()
    expect(screen.getByTestId('create-character-avatar-title').textContent).toBe('The Unyielding')
    expect(screen.getByTestId('character-avatar-male_unyielding')).toBeTruthy()

    fireEvent.click(screen.getByTestId('character-gender-female'))
    expect(screen.getByTestId('create-character-summary-gender').textContent).toBe('Female')
    expect(screen.getByTestId('create-character-avatar-title').textContent).toBe('The Veiled')
    expect(screen.getByTestId('character-avatar-female_veiled')).toBeTruthy()
    expect(screen.queryByTestId('character-avatar-male_unyielding')).toBeNull()

    fireEvent.click(screen.getByTestId('character-avatar-next'))
    fireEvent.click(screen.getByTestId('character-avatar-female_silver_thorn'))
    expect(screen.getByTestId('create-character-avatar-title').textContent).toBe('The Silver Thorn')
    expect(screen.getByTestId('create-character-summary-avatar').textContent).toBe('The Silver Thorn')
  })

  it('shows an inspect card for an occupied slot', async () => {
    vi.mocked(fetchCharacterRoster).mockResolvedValueOnce({
      slots: [
        {
          slotIndex: 0,
          empty: false,
          characterId: 'char-1',
          name: 'Ragnar Ironfist',
          gender: 'MALE',
          avatarCode: 'male_unyielding',
          level: 4,
          gold: 220,
          currentLocationId: 'loc-1',
          locationName: 'City Square',
          strength: 7,
          agility: 5,
          endurance: 6,
          perception: 5,
          currentHealth: 180,
          maxHealth: 180,
          currentStamina: 90,
          maxStamina: 90,
          physicalDamage: 16,
          accuracy: 12,
          dodge: 8,
          criticalChance: 6,
          armor: 5,
          healingPotions: 2,
          equipped: [
            { slot: 'MAIN_HAND', displayName: 'Rusty Sword', rarity: 'COMMON' },
            { slot: 'CHEST', displayName: 'Worn Leather Armor', rarity: 'COMMON' },
          ],
        },
        {
          slotIndex: 1,
          empty: true,
          characterId: null,
          name: null,
          gender: null,
          avatarCode: null,
          level: 0,
          gold: 0,
          currentLocationId: null,
          locationName: null,
          strength: 0,
          agility: 0,
          endurance: 0,
          perception: 0,
          currentHealth: 0,
          maxHealth: 0,
          currentStamina: 0,
          maxStamina: 0,
          physicalDamage: 0,
          accuracy: 0,
          dodge: 0,
          criticalChance: 0,
          armor: 0,
          healingPotions: 0,
          equipped: [],
        },
        {
          slotIndex: 2,
          empty: true,
          characterId: null,
          name: null,
          gender: null,
          avatarCode: null,
          level: 0,
          gold: 0,
          currentLocationId: null,
          locationName: null,
          strength: 0,
          agility: 0,
          endurance: 0,
          perception: 0,
          currentHealth: 0,
          maxHealth: 0,
          currentStamina: 0,
          maxStamina: 0,
          physicalDamage: 0,
          accuracy: 0,
          dodge: 0,
          criticalChance: 0,
          armor: 0,
          healingPotions: 0,
          equipped: [],
        },
      ],
    })

    renderPage()

    await waitFor(() => {
      expect(screen.getByTestId('character-inspect-card')).toBeTruthy()
    })
    expect(screen.getByTestId('character-inspect-level').textContent).toBe('4')
    expect(screen.getByTestId('character-inspect-location').textContent).toBe('City Square')
    expect(screen.getByTestId('create-character-summary-name').textContent).toBe('Ragnar Ironfist')
    expect(screen.getByText('Rusty Sword')).toBeTruthy()
    expect(screen.getByText('Strength').parentElement?.textContent).toContain('7')
    expect((screen.getByTestId('create-character-submit') as HTMLButtonElement).disabled).toBe(false)

    fireEvent.click(screen.getByTestId('character-slot-1'))
    expect(screen.getByTestId('character-name')).toBeTruthy()
    expect((screen.getByTestId('create-character-submit') as HTMLButtonElement).disabled).toBe(true)
  })
})
