import { Link } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import { claimExpedition, fetchCurrentExpedition, startExpedition } from '../api/expedition'
import type { ExpeditionResponse, ExpeditionStrategy } from '../api/types'
import { Button } from '../ui/Button'
import { ErrorState } from '../ui/ErrorState'
import { LoadingState } from '../ui/LoadingState'
import { Panel } from '../ui/Panel'
import { gameLink } from '../ui/gameNav'

const STRATEGIES: { strategy: ExpeditionStrategy; label: string; blurb: string }[] = [
  { strategy: 'CAUTIOUS', label: 'Cautious', blurb: 'Lower risk, lower reward' },
  { strategy: 'BALANCED', label: 'Balanced', blurb: 'Standard risk and reward' },
  { strategy: 'AGGRESSIVE', label: 'Aggressive', blurb: 'Higher reward, higher injury risk' },
]

type Props = {
  onClose?: () => void
  variant?: 'full' | 'card'
}

function formatRemaining(completesAt: string, nowMs: number): string {
  const remainingMs = Math.max(0, Date.parse(completesAt) - nowMs)
  const totalSeconds = Math.ceil(remainingMs / 1000)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${minutes}:${seconds.toString().padStart(2, '0')}`
}

export function ExpeditionPanel({ onClose, variant = 'full' }: Props) {
  const queryClient = useQueryClient()
  const [strategy, setStrategy] = useState<ExpeditionStrategy>('BALANCED')
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const [nowMs, setNowMs] = useState(() => Date.now())
  const [completionRefreshRequested, setCompletionRefreshRequested] = useState(false)

  const expeditionQuery = useQuery({
    queryKey: ['expedition'],
    queryFn: fetchCurrentExpedition,
    retry: false,
    refetchInterval: (query) => {
      const data = query.state.data
      if (data?.status === 'ACTIVE') {
        return 5_000
      }
      return false
    },
  })

  useEffect(() => {
    const id = window.setInterval(() => setNowMs(Date.now()), 1_000)
    return () => window.clearInterval(id)
  }, [])

  useEffect(() => {
    setCompletionRefreshRequested(false)
  }, [expeditionQuery.data?.id, expeditionQuery.data?.status])

  useEffect(() => {
    if (expeditionQuery.data?.status === 'COMPLETED') {
      void queryClient.invalidateQueries({ queryKey: ['activity'] })
    }
  }, [expeditionQuery.data?.id, expeditionQuery.data?.status, queryClient])

  useEffect(() => {
    const expedition = expeditionQuery.data
    if (!expedition || expedition.status !== 'ACTIVE' || completionRefreshRequested) {
      return
    }
    if (Date.parse(expedition.completesAt) <= nowMs) {
      setCompletionRefreshRequested(true)
      void queryClient.invalidateQueries({ queryKey: ['expedition'] })
    }
  }, [completionRefreshRequested, expeditionQuery.data, nowMs, queryClient])

  async function refreshRelated() {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['expedition'] }),
      queryClient.invalidateQueries({ queryKey: ['activity'] }),
      queryClient.invalidateQueries({ queryKey: ['character'] }),
      queryClient.invalidateQueries({ queryKey: ['inventory'] }),
    ])
  }

  async function handleStart() {
    setError(null)
    setBusy(true)
    try {
      const started = await startExpedition(strategy)
      queryClient.setQueryData(['expedition'], started)
      await queryClient.invalidateQueries({ queryKey: ['activity'] })
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message)
      } else {
        setError('Unable to start the expedition.')
      }
    } finally {
      setBusy(false)
    }
  }

  async function handleClaim(expedition: ExpeditionResponse) {
    setError(null)
    setBusy(true)
    try {
      const claimed = await claimExpedition(expedition.id)
      queryClient.setQueryData(['expedition'], claimed)
      await refreshRelated()
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message)
        if (err.code === 'EXPEDITION_NOT_READY') {
          await queryClient.invalidateQueries({ queryKey: ['expedition'] })
        }
      } else {
        setError('Unable to claim expedition rewards.')
      }
    } finally {
      setBusy(false)
    }
  }

  if (expeditionQuery.isLoading) {
    return (
      <Panel className="expedition-panel" data-testid="expedition-panel" title="Expedition">
        <LoadingState>Checking expedition status…</LoadingState>
      </Panel>
    )
  }

  if (expeditionQuery.error instanceof ApiError) {
    return (
      <Panel className="expedition-panel" data-testid="expedition-panel" title="Expedition">
        <ErrorState onRetry={() => void expeditionQuery.refetch()}>{expeditionQuery.error.message}</ErrorState>
      </Panel>
    )
  }

  const card = variant === 'card'
  const expedition = expeditionQuery.data ?? null
  const visuallyComplete =
    expedition?.status === 'ACTIVE' && Date.parse(expedition.completesAt) <= nowMs

  if (card) {
    return (
      <Panel className="expedition-panel" data-testid="expedition-overview" title="Active Expeditions">
        {expedition && expedition.status !== 'CLAIMED' ? (
          <>
            <p data-testid="expedition-status">
              {expedition.expeditionName} · <strong>{expedition.status}</strong>
            </p>
            {expedition.status === 'ACTIVE' && !visuallyComplete ? (
              <p data-testid="expedition-countdown">
                Remaining: <strong>{formatRemaining(expedition.completesAt, nowMs)}</strong>
              </p>
            ) : null}
            {expedition.status === 'COMPLETED' ? (
              <Button
                type="button"
                data-testid="claim-expedition-button"
                disabled={busy}
                onClick={() => void handleClaim(expedition)}
              >
                {busy ? 'Claiming…' : 'Claim rewards'}
              </Button>
            ) : null}
          </>
        ) : (
          <p className="muted">No expedition in progress.</p>
        )}
        <Link to={gameLink('expeditions')} className="btn btn-secondary" data-testid="view-expeditions">
          {expedition && expedition.status !== 'CLAIMED' ? 'View' : 'Start New Expedition'}
        </Link>
        {error ? (
          <p className="form-error" role="alert" data-testid="expedition-error">
            {error}
          </p>
        ) : null}
      </Panel>
    )
  }

  return (
    <Panel
      id="expeditions"
      className="expedition-panel"
      data-testid="expedition-panel"
      aria-label="Expedition"
      title="Forest Patrol"
      actions={
        onClose ? (
          <Button type="button" variant="ghost" onClick={onClose} data-testid="expedition-close">
            Close
          </Button>
        ) : null
      }
    >

      {expedition && expedition.status !== 'CLAIMED' ? (
        <>
          <p data-testid="expedition-status">
            Status: <strong>{expedition.status}</strong> · Strategy: {expedition.strategy}
          </p>
          {expedition.status === 'ACTIVE' && !visuallyComplete ? (
            <p data-testid="expedition-countdown">
              Remaining: <strong>{formatRemaining(expedition.completesAt, nowMs)}</strong>
            </p>
          ) : null}
          {visuallyComplete ? (
            <p className="muted" data-testid="expedition-awaiting-server">
              Time is up. Refreshing completion from the server…
            </p>
          ) : null}
        </>
      ) : null}

      {expedition?.resultReady && expedition.rewards ? (
        <div className="expedition-rewards" data-testid="expedition-rewards">
          <h3>{expedition.status === 'CLAIMED' ? 'Rewards claimed' : 'Rewards'}</h3>
          <p>
            XP {expedition.rewards.xp} · Gold {expedition.rewards.gold}
            {expedition.rewards.injuryDamage > 0 ? ` · Injury ${expedition.rewards.injuryDamage}` : ''}
          </p>
          {expedition.rewards.items.length > 0 ? (
            <ul>
              {expedition.rewards.items.map((item) => (
                <li key={`${item.itemCode}-${item.quantity}`}>
                  {item.itemName} × {item.quantity}
                </li>
              ))}
            </ul>
          ) : (
            <p className="muted">No items this time.</p>
          )}
        </div>
      ) : null}

      {expedition?.status === 'COMPLETED' ? (
        <Button
          type="button"
          data-testid="claim-expedition-button"
          disabled={busy}
          onClick={() => void handleClaim(expedition)}
        >
          {busy ? 'Claiming…' : 'Claim rewards'}
        </Button>
      ) : null}

      {!expedition || expedition.status === 'CLAIMED' ? (
        <>
          <p>Choose a strategy and send your character on a 20-minute Forest Patrol.</p>
          <ul className="strategy-list" data-testid="expedition-strategies">
            {STRATEGIES.map((option) => (
              <li key={option.strategy}>
                <label>
                  <input
                    type="radio"
                    name="expedition-strategy"
                    value={option.strategy}
                    checked={strategy === option.strategy}
                    onChange={() => setStrategy(option.strategy)}
                    data-testid={`strategy-${option.strategy}`}
                  />
                  <span>
                    <strong>{option.label}</strong>
                    <span className="muted"> — {option.blurb}</span>
                  </span>
                </label>
              </li>
            ))}
          </ul>
          <Button
            type="button"
            data-testid="start-expedition-button"
            disabled={busy}
            onClick={() => void handleStart()}
          >
            {busy ? 'Starting…' : 'Start Forest Patrol'}
          </Button>
        </>
      ) : null}

      {error ? (
        <p className="form-error" role="alert" data-testid="expedition-error">
          {error}
        </p>
      ) : null}
    </Panel>
  )
}
