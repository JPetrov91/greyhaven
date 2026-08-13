// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import type { ExpeditionResponse } from '../api/types'
import { fetchCurrentExpedition } from '../api/expedition'
import { ExpeditionPanel } from './ExpeditionPanel'

vi.mock('../api/expedition', () => ({
  fetchCurrentExpedition: vi.fn(),
  startExpedition: vi.fn(),
  claimExpedition: vi.fn(),
}))

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

describe('ExpeditionPanel', () => {
  it('refreshes activity after the server detects expedition completion', async () => {
    const completed: ExpeditionResponse = {
      id: 'expedition-1',
      expeditionType: 'FOREST_PATROL',
      expeditionName: 'Forest Patrol',
      strategy: 'BALANCED',
      status: 'COMPLETED',
      startedAt: '2026-08-13T10:00:00Z',
      completesAt: '2026-08-13T10:20:00Z',
      claimedAt: null,
      resultReady: true,
      rewards: {
        xp: 20,
        gold: 10,
        injuryDamage: 0,
        items: [],
      },
    }
    vi.mocked(fetchCurrentExpedition).mockResolvedValue(completed)
    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    })
    const invalidate = vi.spyOn(queryClient, 'invalidateQueries')

    render(
      <QueryClientProvider client={queryClient}>
        <ExpeditionPanel />
      </QueryClientProvider>,
    )

    expect(await screen.findByText('Claim rewards')).toBeTruthy()
    await waitFor(() => {
      expect(invalidate).toHaveBeenCalledWith({ queryKey: ['activity'] })
    })
  })
})
