// @vitest-environment jsdom

import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
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
})
