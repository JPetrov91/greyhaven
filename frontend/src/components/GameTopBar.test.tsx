// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { fetchCharacter } from '../api/character'
import { fetchInventory } from '../api/inventory'
import { GameTopBar } from './GameTopBar'

vi.mock('../api/character', () => ({
  fetchCharacter: vi.fn(),
}))

vi.mock('../api/inventory', () => ({
  fetchInventory: vi.fn(),
}))

vi.mock('../auth/AuthContext', () => ({
  useAuth: () => ({
    me: { email: 'a@b.c', hasCharacter: true, accountId: 'acc' },
    logout: vi.fn(),
  }),
}))

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

describe('GameTopBar', () => {
  it('shows live gold and locked placeholder currencies', async () => {
    vi.mocked(fetchCharacter).mockResolvedValue({
      id: 'char-1',
      accountId: 'acc',
      name: 'Ragnar',
      level: 11,
      gold: 4320,
    } as never)
    vi.mocked(fetchInventory).mockResolvedValue({ usedSlots: 3, capacity: 40, items: [], equipment: { slots: {} } } as never)

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <MemoryRouter>
        <QueryClientProvider client={queryClient}>
          <GameTopBar />
        </QueryClientProvider>
      </MemoryRouter>,
    )

    expect(await screen.findByText('Ragnar')).toBeTruthy()
    expect(screen.getByTestId('topbar-gold').textContent?.replace(/\s+/g, ' ').trim()).toBe('Gold 4,320')
    const silver = screen.getByTestId('topbar-silver')
    expect(silver.getAttribute('title')).toBe('Coming later')
    expect(silver.textContent?.replace('Coming later', '').trim()).toBe('Silver')
    expect(silver.querySelector('.coming-later-hint')).toBeNull()
    expect(screen.getByTestId('topbar-honor').textContent).not.toMatch(/0/)
    expect(screen.getByTestId('topbar-mail').getAttribute('aria-label')).toBe('Mail')
    expect((screen.getByTestId('topbar-mail') as HTMLButtonElement).disabled).toBe(true)
    expect(screen.getByTestId('logout-button').textContent).toContain('Logout')
  })

  it('replaces identity with combat context during a fight', async () => {
    vi.mocked(fetchCharacter).mockResolvedValue({
      id: 'char-1',
      accountId: 'acc',
      name: 'Ragnar',
      level: 11,
      gold: 4320,
    } as never)
    vi.mocked(fetchInventory).mockResolvedValue({ usedSlots: 0, capacity: 40, items: [], equipment: { slots: {} } } as never)

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <MemoryRouter>
        <QueryClientProvider client={queryClient}>
          <GameTopBar combatContext={{ monsterName: 'Street Thug', roundNumber: 5 }} />
        </QueryClientProvider>
      </MemoryRouter>,
    )

    expect(await screen.findByTestId('topbar-combat-context')).toBeTruthy()
    expect(screen.getByTestId('topbar-combat-context').textContent).toContain('COMBAT — Street Thug')
    expect(screen.getByTestId('topbar-combat-context').textContent).toContain('Round 5')
    expect(screen.queryByTestId('topbar-identity')).toBeNull()
  })
})
