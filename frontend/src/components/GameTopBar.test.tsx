// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
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

function renderTopBar(combatContext?: { monsterName: string; roundNumber: number }) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <MemoryRouter>
      <QueryClientProvider client={queryClient}>
        <GameTopBar combatContext={combatContext ?? null} />
      </QueryClientProvider>
    </MemoryRouter>,
  )
}

describe('GameTopBar', () => {
  it('shows live gold and locked placeholder currencies', async () => {
    vi.mocked(fetchCharacter).mockResolvedValue({
      id: 'char-1',
      accountId: 'acc',
      name: 'Ragnar',
      level: 11,
      gold: 4320,
      arenaMarks: 12,
    } as never)
    vi.mocked(fetchInventory).mockResolvedValue({ usedSlots: 3, capacity: 40, items: [], equipment: { slots: {} } } as never)

    renderTopBar()

    expect(await screen.findByText('Ragnar')).toBeTruthy()
    expect(screen.getByTestId('topbar-gold').textContent?.replace(/\s+/g, ' ').trim()).toBe('Gold 4,320')
    const silver = screen.getByTestId('topbar-silver')
    expect(silver.getAttribute('title')).toBe('Coming later')
    expect(silver.textContent?.replace('Coming later', '').trim()).toBe('Silver')
    expect(silver.querySelector('.coming-later-hint')).toBeNull()
    expect(screen.getByTestId('topbar-marks').textContent?.replace(/\s+/g, ' ').trim()).toBe('Marks 12')
    expect(screen.getByTestId('topbar-mail').getAttribute('aria-label')).toBe('Mail')
    expect((screen.getByTestId('topbar-mail') as HTMLButtonElement).disabled).toBe(true)
    expect(screen.queryByTestId('logout-button')).toBeNull()
    fireEvent.click(screen.getByTestId('topbar-menu'))
    expect(screen.getByTestId('logout-button').textContent).toContain('Logout')
  })

  it('shows a visible tooltip for icon-only utilities', async () => {
    vi.mocked(fetchCharacter).mockResolvedValue({
      id: 'char-1',
      accountId: 'acc',
      name: 'Ragnar',
      level: 11,
      gold: 4320,
      arenaMarks: 12,
    } as never)
    vi.mocked(fetchInventory).mockResolvedValue({ usedSlots: 0, capacity: 40, items: [], equipment: { slots: {} } } as never)

    renderTopBar()
    await screen.findByText('Ragnar')
    fireEvent.mouseEnter(screen.getByTestId('topbar-mail').closest('.chrome-hint') as HTMLElement)
    expect(screen.getByRole('tooltip').textContent).toContain('Mail')
    expect(screen.getByRole('tooltip').textContent).toContain('Coming later')
  })

  it('keeps identity visible and overlays combat context during a fight', async () => {
    vi.mocked(fetchCharacter).mockResolvedValue({
      id: 'char-1',
      accountId: 'acc',
      name: 'Ragnar',
      level: 11,
      gold: 4320,
      arenaMarks: 12,
    } as never)
    vi.mocked(fetchInventory).mockResolvedValue({ usedSlots: 0, capacity: 40, items: [], equipment: { slots: {} } } as never)

    renderTopBar({ monsterName: 'Street Thug', roundNumber: 5 })

    expect(await screen.findByTestId('topbar-identity')).toBeTruthy()
    expect(screen.getByText('Ragnar')).toBeTruthy()
    expect(screen.getByTestId('topbar-combat-context').textContent).toContain('COMBAT — Street Thug')
    expect(screen.getByTestId('topbar-combat-context').textContent).toContain('Round 5')
  })
})
