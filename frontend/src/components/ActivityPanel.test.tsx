// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { fetchActivity } from '../api/activity'
import { ActivityPanel } from './ActivityPanel'

vi.mock('../api/activity', () => ({
  fetchActivity: vi.fn(),
}))

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

describe('ActivityPanel', () => {
  it('renders the persistent activity summary returned by the server', async () => {
    vi.mocked(fetchActivity).mockResolvedValue([
      {
        id: 'activity-1',
        type: 'EXPEDITION_COMPLETED',
        message: 'Your Forest Patrol returned.',
        createdAt: '2026-08-13T10:20:00Z',
        readAt: null,
      },
    ])
    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    })

    render(
      <QueryClientProvider client={queryClient}>
        <ActivityPanel />
      </QueryClientProvider>,
    )

    expect(await screen.findByText('Your Forest Patrol returned.')).toBeTruthy()
    expect(screen.getByTestId('activity-EXPEDITION_COMPLETED')).toBeTruthy()
  })
})
