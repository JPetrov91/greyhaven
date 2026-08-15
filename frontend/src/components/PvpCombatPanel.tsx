import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import { fetchCharacter } from '../api/character'
import {
  acceptDuel,
  acknowledgeArenaMatch,
  declineDuel,
  submitArenaAction,
  submitDuelAction,
  type PvpMatchResponse,
} from '../api/pvp'
import type { CombatAction, CombatActionPreviewResponse, CombatStatusResponse } from '../api/types'
import { Button } from '../ui/Button'
import { ErrorState } from '../ui/ErrorState'
import { Panel } from '../ui/Panel'
import { StatusBadge } from '../ui/StatusBadge'

type Props = {
  match: PvpMatchResponse
  onUpdate: (match: PvpMatchResponse | null) => void
}

export function PvpCombatPanel({ match, onUpdate }: Props) {
  const queryClient = useQueryClient()
  const characterQuery = useQuery({ queryKey: ['character'], queryFn: fetchCharacter })
  const [error, setError] = useState<string | null>(null)
  const terminal = match.status !== 'ACTIVE' && match.status !== 'PENDING'
  const arena = match.matchKind === 'ARENA'
  const youAreDefender = characterQuery.data?.id === match.defenderId

  async function act(action: CombatAction, techniqueCode?: string) {
    setError(null)
    try {
      const updated = arena
        ? await submitArenaAction(match.id, action, match.roundNumber, techniqueCode)
        : await submitDuelAction(match.id, action, match.roundNumber, techniqueCode)
      onUpdate(updated)
      void queryClient.invalidateQueries({ queryKey: ['character'] })
      void queryClient.invalidateQueries({ queryKey: ['activity'] })
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : 'Unable to resolve that action.')
    }
  }

  async function acknowledge() {
    if (!arena) {
      onUpdate(null)
      return
    }
    await acknowledgeArenaMatch(match.id)
    onUpdate(null)
    void queryClient.invalidateQueries({ queryKey: ['character'] })
  }

  return (
    <Panel
      className="game-column pvp-combat-panel"
      data-testid="pvp-combat-panel"
      title={`${match.matchKind} — ${match.attackerName} vs ${match.defenderName}`}
    >
      <p>
        Round {match.roundNumber} · {match.status}
      </p>
      <p>
        You {match.attackerHealth}/{match.attackerMaxHealth} HP · {match.attackerStamina}/{match.attackerMaxStamina} STA
      </p>
      <p>
        {match.defenderName} {match.defenderHealth}/{match.defenderMaxHealth} HP
      </p>
      <StatusList label="Your statuses" statuses={match.attackerStatuses ?? []} testId="pvp-attacker-statuses" />
      <StatusList label="Opponent statuses" statuses={match.defenderStatuses ?? []} testId="pvp-defender-statuses" />
      {match.defenderIntent ? <p>Defender intent: {match.defenderIntent.label}</p> : null}
      {match.status === 'PENDING' ? (
        <div data-testid="duel-pending">
          {youAreDefender ? (
            <>
              <p>You have been challenged to a duel.</p>
              <Button
                type="button"
                onClick={() => {
                  void acceptDuel(match.id).then(onUpdate)
                }}
              >
                Accept
              </Button>
              <Button
                type="button"
                onClick={() => {
                  void declineDuel(match.id).then(onUpdate)
                }}
              >
                Decline
              </Button>
            </>
          ) : (
            <p data-testid="pvp-waiting">Waiting for the other player to accept…</p>
          )}
        </div>
      ) : null}
      {match.waitingForOpponent ? <p data-testid="pvp-waiting">Waiting for the other player…</p> : null}
      {error ? <ErrorState>{error}</ErrorState> : null}
      {!terminal && match.status === 'ACTIVE' && !match.waitingForOpponent ? (
        <div data-testid="pvp-actions">
          {match.actionPreviews.map((preview) => (
            <Button
              key={`${preview.action}-${preview.techniqueCode ?? ''}`}
              type="button"
              disabled={!!preview.disabledReason}
              data-testid={`pvp-action-${preview.techniqueCode ?? preview.action}`}
              onClick={() => void act(preview.action, preview.techniqueCode ?? undefined)}
            >
              {previewLabel(preview)}
            </Button>
          ))}
        </div>
      ) : null}
      {terminal ? (
        <div>
          {match.settlement ? (
            <p data-testid="pvp-settlement">
              Rating {match.settlement.attackerRatingDelta >= 0 ? '+' : ''}
              {match.settlement.attackerRatingDelta} · Marks {match.settlement.attackerMarks}
            </p>
          ) : null}
          <Button type="button" onClick={() => void acknowledge()}>
            Continue
          </Button>
        </div>
      ) : null}
      <ol className="pvp-combat-log">
        {match.events.map((event) => (
          <li key={`${event.roundNumber}-${event.sequenceNumber}`}>{event.message}</li>
        ))}
      </ol>
    </Panel>
  )
}

function previewLabel(preview: CombatActionPreviewResponse): string {
  const cost = preview.staminaCost > 0 ? ` (${preview.staminaCost})` : ''
  const hit = preview.hitChancePercent != null ? ` ${preview.hitChancePercent}%` : ''
  const blocked = preview.disabledReason ? ` — ${preview.disabledReason}` : ''
  return `${preview.name}${cost}${hit}${blocked}`
}

function StatusList({
  label,
  statuses,
  testId,
}: {
  label: string
  statuses: CombatStatusResponse[]
  testId: string
}) {
  return (
    <p data-testid={testId}>
      {label}:{' '}
      {statuses.length === 0 ? (
        <span className="muted">none</span>
      ) : (
        statuses.map((status) => (
          <StatusBadge key={`${status.type}-${status.remainingRounds}`} tone="neutral">
            {status.type}
            {status.stacks > 1 ? ` ×${status.stacks}` : ''}
          </StatusBadge>
        ))
      )}
    </p>
  )
}
