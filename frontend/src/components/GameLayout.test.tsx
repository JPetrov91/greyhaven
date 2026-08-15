// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { fetchCurrentCombat } from '../api/combat'
import { fetchCurrentEncounter } from '../api/encounter'
import { fetchCurrentExpedition } from '../api/expedition'
import { GameLayout } from './GameLayout'

vi.mock('../api/combat', () => ({
  fetchCurrentCombat: vi.fn(),
}))

vi.mock('../api/encounter', () => ({
  fetchCurrentEncounter: vi.fn(),
  searchEncounter: vi.fn(),
}))

vi.mock('../api/expedition', () => ({
  fetchCurrentExpedition: vi.fn(),
}))

vi.mock('./ActivityPanel', () => ({
  ActivityPanel: () => <aside>activity</aside>,
}))

vi.mock('./CharacterSummaryPanel', () => ({
  CharacterSummaryPanel: () => <aside>character</aside>,
}))

vi.mock('./CombatPanel', () => ({
  CombatPanel: () => <div>combat</div>,
}))

vi.mock('./EncounterPrompt', () => ({
  EncounterPrompt: () => <div>encounter</div>,
}))

vi.mock('./ExpeditionPanel', () => ({
  ExpeditionPanel: () => <div>expedition</div>,
}))

vi.mock('./CraftingPanel', () => ({
  CraftingPanel: () => <div>crafting</div>,
}))

vi.mock('../api/pvp', () => ({
  fetchCurrentArenaMatch: vi.fn().mockResolvedValue(null),
  fetchCurrentDuel: vi.fn().mockResolvedValue(null),
}))

vi.mock('./InventoryPanel', () => ({
  InventoryPanel: () => <div>inventory</div>,
}))

vi.mock('./EquipmentPanel', () => ({
  EquipmentPanel: () => <div>equipment</div>,
}))

vi.mock('./MasteryPanel', () => ({
  MasteryPanel: () => <div>mastery</div>,
}))

vi.mock('./LocationPanel', () => ({
  LocationPanel: () => <div>location</div>,
}))

vi.mock('./MarketPanel', () => ({
  MarketPanel: () => <section data-testid="market-panel">Marketplace</section>,
}))

vi.mock('./ChatPanel', () => ({
  ChatPanel: () => <section data-testid="chat-panel">chat</section>,
}))

vi.mock('./GameTopBar', () => ({
  GameTopBar: () => <header>top</header>,
}))

vi.mock('./GameLeftNav', () => ({
  GameLeftNav: () => <nav>nav</nav>,
}))

vi.mock('./EquipmentOverviewCard', () => ({
  EquipmentOverviewCard: () => <div>equipment-overview</div>,
}))

vi.mock('./GuildPlaceholder', () => ({
  GuildPlaceholder: () => <div data-testid="guild-placeholder">guild</div>,
}))

vi.mock('./ArenaPanel', () => ({
  ArenaPanel: () => <div>arena</div>,
}))

vi.mock('./PvpCombatPanel', () => ({
  PvpCombatPanel: () => <div>pvp</div>,
}))

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

function renderGame(path: string) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <MemoryRouter initialEntries={[path]}>
      <QueryClientProvider client={queryClient}>
        <GameLayout />
      </QueryClientProvider>
    </MemoryRouter>,
  )
}

describe('GameLayout', () => {
  it('opens the marketplace from the Market navigation query', async () => {
    vi.mocked(fetchCurrentCombat).mockResolvedValue(null)
    vi.mocked(fetchCurrentEncounter).mockResolvedValue(null)
    vi.mocked(fetchCurrentExpedition).mockResolvedValue(null)

    renderGame('/game?panel=market')

    expect(await screen.findByTestId('market-panel')).toBeTruthy()
    expect(screen.queryByTestId('chat-panel')).toBeNull()
  })

  it('shows the home dashboard with chat when idle', async () => {
    vi.mocked(fetchCurrentCombat).mockResolvedValue(null)
    vi.mocked(fetchCurrentEncounter).mockResolvedValue(null)
    vi.mocked(fetchCurrentExpedition).mockResolvedValue(null)

    renderGame('/game')

    expect(await screen.findByTestId('chat-panel')).toBeTruthy()
    expect(screen.getByTestId('guild-placeholder')).toBeTruthy()
    expect(screen.getByText('location')).toBeTruthy()
  })

  it('replaces the dashboard with combat when a session is active', async () => {
    vi.mocked(fetchCurrentCombat).mockResolvedValue({ id: 'combat-1' } as never)
    vi.mocked(fetchCurrentEncounter).mockResolvedValue(null)
    vi.mocked(fetchCurrentExpedition).mockResolvedValue(null)

    renderGame('/game')

    expect(await screen.findByText('combat')).toBeTruthy()
    expect(screen.queryByText('nav')).toBeNull()
    expect(screen.queryByText('activity')).toBeNull()
    expect(screen.queryByTestId('chat-panel')).toBeNull()
  })

  it('opens inventory and equipment as separate views', async () => {
    vi.mocked(fetchCurrentCombat).mockResolvedValue(null)
    vi.mocked(fetchCurrentEncounter).mockResolvedValue(null)
    vi.mocked(fetchCurrentExpedition).mockResolvedValue(null)

    renderGame('/game#inventory')
    expect(await screen.findByText('inventory')).toBeTruthy()
    expect(screen.queryByText('equipment')).toBeNull()

    cleanup()
    renderGame('/game#equipment')
    expect(await screen.findByText('equipment')).toBeTruthy()
    expect(screen.queryByText('inventory')).toBeNull()
    expect(screen.getByTestId('chat-panel')).toBeTruthy()
    expect(screen.getByText('activity')).toBeTruthy()
    expect(screen.getByText('nav')).toBeTruthy()
  })
})
