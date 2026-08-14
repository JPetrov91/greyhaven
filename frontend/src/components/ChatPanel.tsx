import { useEffect, useRef, useState, type FormEvent } from 'react'
import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import { chatStreamUrl, fetchChatMessages, postChatMessage } from '../api/chat'
import type { ChatMessageResponse } from '../api/types'
import { ComingLaterButton } from '../ui/ComingLater'
import { Button } from '../ui/Button'
import { EmptyState } from '../ui/EmptyState'
import { ErrorState } from '../ui/ErrorState'
import { LoadingState } from '../ui/LoadingState'

const HISTORY_CAP = 100
const MAX_BODY_LENGTH = 500

function formatWhen(iso: string): string {
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) {
    return iso
  }
  return date.toLocaleString(undefined, {
    hour: '2-digit',
    minute: '2-digit',
  })
}

function mergeMessage(current: ChatMessageResponse[], incoming: ChatMessageResponse): ChatMessageResponse[] {
  if (current.some((message) => message.id === incoming.id)) {
    return current
  }
  return [...current, incoming].slice(-HISTORY_CAP)
}

export function ChatPanel() {
  const [draft, setDraft] = useState('')
  const [sendError, setSendError] = useState<string | null>(null)
  const [sending, setSending] = useState(false)
  const [liveMessages, setLiveMessages] = useState<ChatMessageResponse[]>([])
  const [streamState, setStreamState] = useState<'connecting' | 'live' | 'reconnecting'>('connecting')
  const lastIdRef = useRef<string | null>(null)
  const listRef = useRef<HTMLUListElement | null>(null)

  const historyQuery = useQuery({
    queryKey: ['chat-messages'],
    queryFn: fetchChatMessages,
    retry: false,
    refetchOnWindowFocus: false,
  })

  useEffect(() => {
    if (historyQuery.isPending) {
      return
    }
    if (historyQuery.data && historyQuery.data.length > 0) {
      lastIdRef.current = historyQuery.data[historyQuery.data.length - 1]?.id ?? null
    }

    let closed = false
    let source: EventSource | null = null
    let timer: number | undefined
    let attempt = 0

    function connect() {
      if (closed) {
        return
      }
      source = new EventSource(chatStreamUrl(lastIdRef.current), { withCredentials: true })
      source.addEventListener('message', (event: MessageEvent<string>) => {
        attempt = 0
        setStreamState('live')
        try {
          const parsed = JSON.parse(event.data) as ChatMessageResponse
          if (!parsed.id || typeof parsed.body !== 'string') {
            return
          }
          lastIdRef.current = parsed.id
          setLiveMessages((current) => mergeMessage(current, parsed))
        } catch {
          // Ignore malformed frames; the next reconnect will refresh from history.
        }
      })
      source.onerror = () => {
        source?.close()
        source = null
        if (closed) {
          return
        }
        setStreamState('reconnecting')
        const delay = Math.min(8_000, 500 * 2 ** attempt)
        attempt += 1
        timer = window.setTimeout(connect, delay)
      }
    }

    connect()
    return () => {
      closed = true
      source?.close()
      if (timer !== undefined) {
        window.clearTimeout(timer)
      }
    }
  }, [historyQuery.isPending, historyQuery.data])

  const history = historyQuery.data ?? []
  const messages = [...history, ...liveMessages.filter((message) => !history.some((entry) => entry.id === message.id))]
    .slice(-HISTORY_CAP)

  useEffect(() => {
    const last = listRef.current?.lastElementChild
    if (last && typeof last.scrollIntoView === 'function') {
      last.scrollIntoView({ block: 'nearest' })
    }
  }, [messages.length])

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const body = draft.trim()
    if (!body) {
      return
    }
    setSendError(null)
    setSending(true)
    try {
      const posted = await postChatMessage(body)
      lastIdRef.current = posted.id
      setLiveMessages((current) => mergeMessage(current, posted))
      setDraft('')
    } catch (error) {
      setSendError(error instanceof ApiError ? error.message : 'Unable to send that message.')
    } finally {
      setSending(false)
    }
  }

  return (
    <section className="chat-panel" data-testid="chat-panel" aria-label="Global chat">
      <div className="chat-panel-header">
        <h2>Global chat</h2>
        <p className="muted" data-testid="chat-stream-status">
          {streamState === 'live'
            ? 'Live'
            : streamState === 'reconnecting'
              ? 'Reconnecting…'
              : 'Connecting…'}
        </p>
      </div>
      <div className="chat-channel-tabs" role="tablist" aria-label="Chat channels">
        <button type="button" className="tab tab-active" role="tab" aria-selected="true">
          GLOBAL
        </button>
        <ComingLaterButton className="tab" data-testid="chat-tab-trade" role="tab" aria-selected={false}>
          TRADE
        </ComingLaterButton>
        <ComingLaterButton className="tab" data-testid="chat-tab-guild" role="tab" aria-selected={false}>
          GUILD
        </ComingLaterButton>
        <ComingLaterButton className="tab" data-testid="chat-tab-party" role="tab" aria-selected={false}>
          PARTY
        </ComingLaterButton>
      </div>

      {historyQuery.isLoading ? (
        <LoadingState testId="chat-loading">Loading recent messages…</LoadingState>
      ) : historyQuery.error instanceof ApiError ? (
        <ErrorState testId="chat-load-error">{historyQuery.error.message}</ErrorState>
      ) : messages.length === 0 ? (
        <EmptyState testId="chat-empty">No messages yet. Say hello to Greyhaven.</EmptyState>
      ) : (
        <ul className="chat-list" data-testid="chat-list" ref={listRef}>
          {messages.map((message) => (
            <li key={message.id} data-testid={`chat-message-${message.id}`}>
              <div className="chat-message-meta">
                <strong>{message.characterName}</strong>
                <time className="muted" dateTime={message.createdAt}>
                  {formatWhen(message.createdAt)}
                </time>
              </div>
              <p>{message.body}</p>
            </li>
          ))}
        </ul>
      )}

      <form className="chat-form" onSubmit={(event) => void handleSubmit(event)}>
        <label className="chat-input-label">
          <span className="visually-hidden">Message</span>
          <input
            type="text"
            name="body"
            maxLength={MAX_BODY_LENGTH}
            value={draft}
            onChange={(event) => setDraft(event.target.value)}
            placeholder="Write a message…"
            data-testid="chat-input"
            autoComplete="off"
          />
        </label>
        <Button type="submit" data-testid="chat-send" disabled={sending || !draft.trim()}>
          {sending ? 'Sending…' : 'Send'}
        </Button>
      </form>
      {sendError ? (
        <p className="form-error" role="alert" data-testid="chat-send-error">
          {sendError}
        </p>
      ) : null}
    </section>
  )
}
