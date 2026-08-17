// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { challengeDuel } from '../api/pvp'
import { fetchSparringBots, startSparringDrill } from '../api/sparring'
import { fetchNearbyCharacters } from '../api/world'
import { SparringYardPanel } from './SparringYardPanel'

vi.mock('../api/world', () => ({
  fetchNearbyCharacters: vi.fn(),
}))

vi.mock('../api/pvp', () => ({
  challengeDuel: vi.fn(),
  fetchPublicCharacter: vi.fn(),
}))

vi.mock('../api/sparring', () => ({
  fetchSparringBots: vi.fn(),
  startSparringDrill: vi.fn(),
}))

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

function renderYard() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  const onMatchStarted = vi.fn()
  render(
    <QueryClientProvider client={queryClient}>
      <SparringYardPanel onMatchStarted={onMatchStarted} />
    </QueryClientProvider>,
  )
  return { onMatchStarted }
}

describe('SparringYardPanel', () => {
  it('challenges a nearby low-level fighter', async () => {
    vi.mocked(fetchNearbyCharacters).mockResolvedValue({
      characters: [{ id: 'opp-1', name: 'Pip', level: 3, avatarCode: 'male_unyielding' }],
      truncated: false,
      limit: 50,
      totalCount: 1,
    })
    vi.mocked(fetchSparringBots).mockResolvedValue([{ level: 1, name: 'Green Recruit', code: 'SPARRING_BOT_L01' }])
    vi.mocked(challengeDuel).mockResolvedValue({} as never)
    const { onMatchStarted } = renderYard()

    expect(await screen.findByTestId('sparring-yard-panel')).toBeTruthy()
    fireEvent.click(await screen.findByTestId('sparring-challenge-opp-1'))
    await waitFor(() => expect(challengeDuel).toHaveBeenCalledWith('opp-1'))
    expect(onMatchStarted).toHaveBeenCalled()
  })

  it('starts a drill against the selected bot level', async () => {
    vi.mocked(fetchNearbyCharacters).mockResolvedValue({
      characters: [],
      truncated: false,
      limit: 50,
      totalCount: 0,
    })
    vi.mocked(fetchSparringBots).mockResolvedValue([
      { level: 1, name: 'Green Recruit', code: 'SPARRING_BOT_L01' },
      { level: 4, name: 'Yard Regular', code: 'SPARRING_BOT_L04' },
    ])
    vi.mocked(startSparringDrill).mockResolvedValue({} as never)
    const { onMatchStarted } = renderYard()

    fireEvent.change(await screen.findByTestId('sparring-bot-level'), { target: { value: '4' } })
    expect(screen.getByTestId('sparring-bot-preview').textContent).toContain('Yard Regular')
    expect(screen.getByTestId('sparring-bot-preview').querySelector('img')?.getAttribute('src')).toBe(
      '/sparring/full/sparring_bot_l04.webp',
    )
    fireEvent.click(screen.getByTestId('sparring-start-drill'))
    await waitFor(() => expect(startSparringDrill).toHaveBeenCalledWith(4))
    expect(onMatchStarted).toHaveBeenCalled()
  })
})
