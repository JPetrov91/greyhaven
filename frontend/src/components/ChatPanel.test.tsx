// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/client'
import { fetchChatMessages, postChatMessage } from '../api/chat'
import { ChatPanel } from './ChatPanel'

vi.mock('../api/chat', () => ({
  fetchChatMessages: vi.fn(),
  postChatMessage: vi.fn(),
  chatStreamUrl: (after?: string | null) => (after ? `/api/v1/chat/stream?after=${after}` : '/api/v1/chat/stream'),
}))

class MockEventSource {
  static instances: MockEventSource[] = []
  onerror: ((event: Event) => void) | null = null
  readonly listeners = new Map<string, ((event: MessageEvent<string>) => void)[]>()

  readonly url: string

  constructor(url: string, _init?: EventSourceInit) {
    this.url = url
    MockEventSource.instances.push(this)
  }

  addEventListener(type: string, listener: (event: MessageEvent<string>) => void) {
    const current = this.listeners.get(type) ?? []
    current.push(listener)
    this.listeners.set(type, current)
  }

  close() {}

  emit(data: string) {
    for (const listener of this.listeners.get('message') ?? []) {
      listener({ data } as MessageEvent<string>)
    }
  }

  error() {
    this.onerror?.(new Event('error'))
  }
}

afterEach(() => {
  cleanup()
  vi.useRealTimers()
  vi.clearAllMocks()
  MockEventSource.instances = []
})

function renderChat() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={queryClient}>
      <ChatPanel />
    </QueryClientProvider>,
  )
}

describe('ChatPanel', () => {
  it('renders history, live frames, and send errors without interpreting HTML', async () => {
    vi.stubGlobal('EventSource', MockEventSource)
    vi.mocked(fetchChatMessages).mockResolvedValue([
      {
        id: 'm1',
        characterId: 'c1',
        characterName: 'Alden',
        body: 'Hello <b>Greyhaven</b>',
        createdAt: '2026-08-14T10:00:00Z',
      },
    ])

    renderChat()

    expect(await screen.findByText('Hello <b>Greyhaven</b>')).toBeTruthy()
    expect((screen.getByTestId('chat-tab-trade') as HTMLButtonElement).disabled).toBe(true)
    expect((screen.getByTestId('chat-tab-guild') as HTMLButtonElement).disabled).toBe(true)
    expect((screen.getByTestId('chat-tab-party') as HTMLButtonElement).disabled).toBe(true)
    expect(document.querySelector('b')).toBeNull()

    await waitFor(() => expect(MockEventSource.instances[0]?.url).toContain('after=m1'))
    MockEventSource.instances[0]?.emit(
      JSON.stringify({
        id: 'm2',
        characterId: 'c2',
        characterName: 'Bryn',
        body: 'Market is open',
        createdAt: '2026-08-14T10:01:00Z',
      }),
    )
    expect(await screen.findByText('Market is open')).toBeTruthy()

    vi.mocked(postChatMessage).mockRejectedValue(
      new ApiError(429, {
        code: 'CHAT_RATE_LIMITED',
        message: 'Wait a moment before sending another chat message.',
        timestamp: '2026-08-14T10:02:00Z',
      }),
    )

    fireEvent.change(screen.getByTestId('chat-input'), { target: { value: 'too soon' } })
    fireEvent.click(screen.getByTestId('chat-send'))
    expect(await screen.findByTestId('chat-send-error')).toBeTruthy()
    vi.unstubAllGlobals()
  })

  it('connects after history and reconnects with the last seen message id', async () => {
    vi.stubGlobal('EventSource', MockEventSource)
    vi.mocked(fetchChatMessages).mockResolvedValue([
      {
        id: 'm1',
        characterId: 'c1',
        characterName: 'Alden',
        body: 'Hello',
        createdAt: '2026-08-14T10:00:00Z',
      },
    ])

    renderChat()
    await waitFor(() => expect(MockEventSource.instances[0]?.url).toBe('/api/v1/chat/stream?after=m1'))

    MockEventSource.instances[0]?.emit(
      JSON.stringify({
        id: 'm2',
        characterId: 'c2',
        characterName: 'Bryn',
        body: 'Still here',
        createdAt: '2026-08-14T10:01:00Z',
      }),
    )
    expect(await screen.findByText('Still here')).toBeTruthy()

    vi.useFakeTimers()
    MockEventSource.instances.at(-1)?.error()
    await vi.advanceTimersByTimeAsync(500)
    expect(MockEventSource.instances.at(-1)?.url).toBe('/api/v1/chat/stream?after=m2')
    vi.unstubAllGlobals()
  })
})
