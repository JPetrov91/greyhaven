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
import type { CombatAction } from '../api/types'
import { Button } from '../ui/Button'

type Props = {
  match: PvpMatchResponse
  onUpdate: (match: PvpMatchResponse | null) => void
}

const ACTIONS: CombatAction[] = ['QUICK_ATTACK', 'HEAVY_ATTACK', 'PRECISE_ATTACK', 'DEFEND', 'USE_POTION']

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
    <section className="game-column" data-testid="pvp-combat-panel">
      <h2>
        {match.matchKind} — {match.attackerName} vs {match.defenderName}
      </h2>
      <p>
        Round {match.roundNumber} · {match.status}
      </p>
      <p>
        You {match.attackerHealth}/{match.attackerMaxHealth} HP · {match.attackerStamina}/{match.attackerMaxStamina} STA
      </p>
      <p>
        {match.defenderName} {match.defenderHealth}/{match.defenderMaxHealth} HP
      </p>
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
      {error ? (
        <p className="form-error" role="alert">
          {error}
        </p>
      ) : null}
      {!terminal && match.status === 'ACTIVE' && !match.waitingForOpponent ? (
        <div>
          {ACTIONS.map((action) => (
            <Button key={action} type="button" onClick={() => void act(action)}>
              {action}
            </Button>
          ))}
          {match.techniques.map((technique) => (
            <Button
              key={technique.code}
              type="button"
              disabled={!!technique.disabledReason}
              onClick={() => void act('USE_TECHNIQUE', technique.code)}
            >
              {technique.name}
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
      <ol>
        {match.events.map((event) => (
          <li key={`${event.roundNumber}-${event.sequenceNumber}`}>{event.message}</li>
        ))}
      </ol>
    </section>
  )
}
