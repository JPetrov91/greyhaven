// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
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
      <MemoryRouter>
        <QueryClientProvider client={queryClient}>
          <ActivityPanel />
        </QueryClientProvider>
      </MemoryRouter>,
    )

    expect(await screen.findByText('Forest Patrol')).toBeTruthy()
    expect(screen.getByText(/returned/)).toBeTruthy()
    expect(screen.getByTestId('activity-EXPEDITION_COMPLETED')).toBeTruthy()
    expect(screen.getByTestId('activity-EXPEDITION_COMPLETED').querySelector('img')?.getAttribute('src')).toBe(
      '/icons/activity/chest.webp',
    )
    expect(screen.getByText('Activity & Notifications')).toBeTruthy()
    expect(screen.queryByTestId('activity-view-all')).toBeNull()
    fireEvent.click(screen.getByTestId('activity-filter'))
    fireEvent.click(screen.getByRole('option', { name: 'Events' }))
    expect(screen.getByTestId('activity-filter').textContent).toBe('Events')
  })

  it('shows a claimable expedition action without inventing world events', async () => {
    vi.mocked(fetchActivity).mockResolvedValue([])
    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    })

    render(
      <MemoryRouter>
        <QueryClientProvider client={queryClient}>
          <ActivityPanel claimableExpedition combatActive />
        </QueryClientProvider>
      </MemoryRouter>,
    )

    expect(await screen.findByTestId('activity-claimable')).toBeTruthy()
    expect(screen.getByTestId('rail-claim-expedition')).toBeTruthy()
    expect(screen.getByTestId('activity-alerts').textContent).toContain('Combat is in progress')
    expect(screen.queryByText(/rift/i)).toBeNull()
    expect(screen.queryByTestId('activity-view-all')).toBeNull()
  })
})
