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

vi.mock('./InventoryPanel', () => ({
  InventoryPanel: () => <div>inventory</div>,
}))

vi.mock('./LocationPanel', () => ({
  LocationPanel: () => <div>location</div>,
}))

vi.mock('./MarketPanel', () => ({
  MarketPanel: () => <section data-testid="market-panel">Marketplace</section>,
}))

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

describe('GameLayout', () => {
  it('opens the marketplace from the Market navigation query', async () => {
    vi.mocked(fetchCurrentCombat).mockResolvedValue(null)
    vi.mocked(fetchCurrentEncounter).mockResolvedValue(null)
    vi.mocked(fetchCurrentExpedition).mockResolvedValue(null)

    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    })

    render(
      <MemoryRouter initialEntries={['/game?panel=market']}>
        <QueryClientProvider client={queryClient}>
          <GameLayout />
        </QueryClientProvider>
      </MemoryRouter>,
    )

    expect(await screen.findByTestId('market-panel')).toBeTruthy()
  })
})
