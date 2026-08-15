import { useEffect, useRef, useState, type FormEvent } from 'react'
import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import { chatStreamUrl, fetchChatMessages, postChatMessage } from '../api/chat'
import type { ChatMessageResponse } from '../api/types'
import { classNames } from '../ui/classNames'
import { ComingLaterButton } from '../ui/ComingLater'
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
  return date.toLocaleTimeString(undefined, {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  })
}

const CHANNELS = [
  { id: 'global', label: 'GLOBAL', art: '/icons/chat/global.webp', later: false, unread: 12 },
  { id: 'trade', label: 'TRADE', art: '/icons/chat/trade.webp', later: true, testId: 'chat-tab-trade', unread: 3 },
  { id: 'guild', label: 'GUILD', art: '/icons/chat/guild.webp', later: true, testId: 'chat-tab-guild', unread: 5 },
  { id: 'party', label: 'PARTY', art: '/icons/chat/party.webp', later: true, testId: 'chat-tab-party', unread: 1 },
] as const

function nameTone(name: string): 'gold' | 'green' | 'teal' {
  if (name.toLowerCase() === 'system') {
    return 'gold'
  }
  const sum = [...name].reduce((total, char) => total + char.charCodeAt(0), 0)
  return (['green', 'gold', 'teal'] as const)[sum % 3]
}

function renderBody(body: string) {
  return body.split(/(\[[^\]]+\])/).map((part, index) =>
    part.startsWith('[') && part.endsWith(']') ? (
      <span key={index} className="chat-item-link">
        {part}
      </span>
    ) : (
      part
    ),
  )
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
  const [channelsCollapsed, setChannelsCollapsed] = useState(false)
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
      source.onopen = () => {
        attempt = 0
        setStreamState('live')
      }
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
    <section
      className={classNames('chat-panel', channelsCollapsed && 'is-collapsed')}
      data-testid="chat-panel"
      aria-label="Global chat"
    >
      <h2 className="visually-hidden">Global chat</h2>
      <p className="visually-hidden" data-testid="chat-stream-status">
        {streamState === 'live'
          ? 'Live'
          : streamState === 'reconnecting'
            ? 'Reconnecting…'
            : historyQuery.isSuccess
              ? 'History'
              : 'Connecting…'}
      </p>
      <div className="chat-shell">
        <aside className="chat-channels">
          <div className="chat-channel-list" role="tablist" aria-label="Chat channels">
            {CHANNELS.map((channel) => {
              const inner = (
                <>
                  <img className="chat-channel-icon" src={channel.art} alt="" aria-hidden="true" />
                  <span className="chat-channel-label">{channel.label}</span>
                  <span className="chat-unread" aria-hidden="true">
                    {channel.unread}
                  </span>
                </>
              )
              if (channel.later) {
                return (
                  <ComingLaterButton
                    key={channel.id}
                    className="chat-channel"
                    data-testid={channel.testId}
                    role="tab"
                    aria-selected={false}
                  >
                    {inner}
                  </ComingLaterButton>
                )
              }
              return (
                <button
                  key={channel.id}
                  type="button"
                  className="chat-channel is-active"
                  role="tab"
                  aria-selected="true"
                >
                  {inner}
                </button>
              )
            })}
          </div>
          <button
            type="button"
            className="chat-channels-collapse"
            data-testid="chat-channels-collapse"
            aria-expanded={!channelsCollapsed}
            aria-label={channelsCollapsed ? 'Expand chat channels' : 'Collapse chat channels'}
            onClick={() => setChannelsCollapsed((current) => !current)}
          >
            «
          </button>
        </aside>

        <div className="chat-main">
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
                  <time dateTime={message.createdAt}>{formatWhen(message.createdAt)}</time>
                  <strong className={`chat-name chat-name-${nameTone(message.characterName)}`}>
                    [{message.characterName}]
                  </strong>
                  <p>{renderBody(message.body)}</p>
                </li>
              ))}
            </ul>
          )}

          <form className="chat-form" onSubmit={(event) => void handleSubmit(event)}>
            <div className="chat-input-label">
              <label>
                <span className="visually-hidden">Message</span>
                <input
                  type="text"
                  name="body"
                  maxLength={MAX_BODY_LENGTH}
                  value={draft}
                  onChange={(event) => setDraft(event.target.value)}
                  placeholder="Type your message..."
                  data-testid="chat-input"
                  autoComplete="off"
                />
              </label>
              <ComingLaterButton className="chat-emoji" data-testid="chat-emoji" aria-label="Emoji">
                <svg viewBox="0 0 20 20" aria-hidden="true" focusable="false">
                  <circle cx="10" cy="10" r="7.2" fill="none" stroke="currentColor" strokeWidth="1.4" />
                  <circle cx="7.6" cy="8.4" r="0.85" fill="currentColor" />
                  <circle cx="12.4" cy="8.4" r="0.85" fill="currentColor" />
                  <path d="M7.2 12.2c.8 1.3 1.8 1.9 2.8 1.9s2-.6 2.8-1.9" fill="none" stroke="currentColor" strokeWidth="1.3" strokeLinecap="round" />
                </svg>
              </ComingLaterButton>
            </div>
            <button type="submit" className="chat-send" data-testid="chat-send" disabled={sending || !draft.trim()}>
              {sending ? 'Sending…' : 'Send'}
            </button>
          </form>
          {sendError ? (
            <p className="form-error" role="alert" data-testid="chat-send-error">
              {sendError}
            </p>
          ) : null}
        </div>
      </div>
    </section>
  )
}
