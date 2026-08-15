import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import {
  challengeArena,
  challengeDuel,
  fetchArenaOpponents,
  fetchArenaProfile,
  fetchPvpHistory,
  fetchPublicCharacter,
  updateArenaDefense,
  type ArenaDefense,
  type PublicCharacterResponse,
} from '../api/pvp'
import type { CombatAction } from '../api/types'
import { Button } from '../ui/Button'
import { EmptyState } from '../ui/EmptyState'
import { ErrorState } from '../ui/ErrorState'
import { Field } from '../ui/Field'
import { LoadingState } from '../ui/LoadingState'
import { Panel } from '../ui/Panel'

type Props = {
  onMatchStarted: () => void
}

export function ArenaPanel({ onMatchStarted }: Props) {
  const queryClient = useQueryClient()
  const [error, setError] = useState<string | null>(null)
  const [inspected, setInspected] = useState<PublicCharacterResponse | null>(null)
  const profileQuery = useQuery({ queryKey: ['arena-profile'], queryFn: fetchArenaProfile })
  const opponentsQuery = useQuery({ queryKey: ['arena-opponents'], queryFn: () => fetchArenaOpponents(0) })
  const historyQuery = useQuery({ queryKey: ['pvp-history'], queryFn: () => fetchPvpHistory(0) })

  const profile = profileQuery.data

  async function saveDefense(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!profile) {
      return
    }
    const form = new FormData(event.currentTarget)
    const defense: ArenaDefense = {
      preferredAction: String(form.get('preferredAction')) as CombatAction,
      preferredTechniqueCode: String(form.get('preferredTechniqueCode') || '') || null,
      healWhenHpPercentBelow: Number(form.get('healWhenHpPercentBelow')),
      defendWhenStaminaPercentBelow: Number(form.get('defendWhenStaminaPercentBelow')),
      finisherWhenEnemyHpPercentBelow: Number(form.get('finisherWhenEnemyHpPercentBelow')),
      finisherTechniqueCode: String(form.get('finisherTechniqueCode') || '') || null,
    }
    setError(null)
    try {
      await updateArenaDefense(defense)
      await queryClient.invalidateQueries({ queryKey: ['arena-profile'] })
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : 'Unable to save defense.')
    }
  }

  async function startArena(defenderId: string) {
    setError(null)
    try {
      await challengeArena(defenderId)
      onMatchStarted()
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : 'Unable to start the challenge.')
    }
  }

  async function startDuel(defenderId: string) {
    setError(null)
    try {
      await challengeDuel(defenderId)
      await queryClient.invalidateQueries({ queryKey: ['duel'] })
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : 'Unable to send the duel challenge.')
    }
  }

  async function inspect(id: string) {
    setError(null)
    try {
      setInspected(await fetchPublicCharacter(id))
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : 'Unable to inspect that character.')
    }
  }

  if (profileQuery.isPending) {
    return <LoadingState>Opening the Arena…</LoadingState>
  }
  if (profileQuery.isError || !profile) {
    return <ErrorState onRetry={() => void profileQuery.refetch()}>Arena profile could not be loaded.</ErrorState>
  }

  return (
    <Panel id="pvp" className="game-column pvp-panel" data-testid="arena-panel" title="Arena">
      <p data-testid="arena-rating">
        Rating {profile.rating} · Marks {profile.marks}
      </p>
      {error ? (
        <ErrorState>{error}</ErrorState>
      ) : null}

      <form onSubmit={(event) => void saveDefense(event)} data-testid="arena-defense-form">
        <h3>Defense strategy</h3>
        <Field label="Preferred action">
          <select name="preferredAction" defaultValue={profile.defense.preferredAction}>
            {profile.preferredActionOptions.map((action) => (
              <option key={action} value={action}>
                {action}
              </option>
            ))}
          </select>
        </Field>
        <Field label="Preferred technique">
          <input name="preferredTechniqueCode" defaultValue={profile.defense.preferredTechniqueCode ?? ''} />
        </Field>
        <Field label="Heal when HP below %">
          <input
            name="healWhenHpPercentBelow"
            type="number"
            min={0}
            max={100}
            defaultValue={profile.defense.healWhenHpPercentBelow}
          />
        </Field>
        <Field label="Defend when stamina below %">
          <input
            name="defendWhenStaminaPercentBelow"
            type="number"
            min={0}
            max={100}
            defaultValue={profile.defense.defendWhenStaminaPercentBelow}
          />
        </Field>
        <Field label="Finisher when enemy HP below %">
          <input
            name="finisherWhenEnemyHpPercentBelow"
            type="number"
            min={0}
            max={100}
            defaultValue={profile.defense.finisherWhenEnemyHpPercentBelow}
          />
        </Field>
        <Field label="Finisher technique">
          <input name="finisherTechniqueCode" defaultValue={profile.defense.finisherTechniqueCode ?? ''} />
        </Field>
        <Button type="submit">Save defense</Button>
      </form>

      <h3>Opponents</h3>
      {opponentsQuery.isError ? (
        <ErrorState onRetry={() => void opponentsQuery.refetch()}>
          Travel to the Arena to see available opponents.
        </ErrorState>
      ) : opponentsQuery.data?.opponents.length ? (
        <ul data-testid="arena-opponents">
          {opponentsQuery.data.opponents.map((opponent) => (
            <li key={opponent.id}>
              <button type="button" onClick={() => void inspect(opponent.id)}>
                {opponent.name} · {opponent.level} · {opponent.rating}
              </button>
              <Button type="button" onClick={() => void startArena(opponent.id)}>
                Challenge
              </Button>
              <Button type="button" onClick={() => void startDuel(opponent.id)}>
                Duel
              </Button>
            </li>
          ))}
        </ul>
      ) : (
        <EmptyState testId="arena-opponents-empty">No opponents are listed yet.</EmptyState>
      )}

      {inspected ? (
        <aside data-testid="public-inspect">
          <h3>{inspected.name}</h3>
          <p>
            Level {inspected.level} · Rating {inspected.arenaRating}
          </p>
          <p>
            STR {inspected.strength} AGI {inspected.agility} END {inspected.endurance} PER {inspected.perception}
          </p>
          <p>
            {inspected.weaponFamily ?? 'Unarmed'} mastery {inspected.weaponMasteryLevel ?? 0}
          </p>
          <ul>
            {inspected.equipment.map((item) => (
              <li key={item.slot}>
                {item.slot}: {item.displayName}
              </li>
            ))}
          </ul>
        </aside>
      ) : null}

      <h3>Recent battles</h3>
      {historyQuery.data?.entries.length ? (
        <ul data-testid="pvp-history">
          {historyQuery.data.entries.map((entry) => (
            <li key={`${entry.matchId}-${entry.createdAt}`}>
              {entry.result} vs {entry.opponentName}
              {entry.ratingDelta ? ` Rating ${entry.ratingDelta > 0 ? '+' : ''}${entry.ratingDelta}` : ''}
            </li>
          ))}
        </ul>
      ) : (
        <EmptyState>No battles recorded yet.</EmptyState>
      )}
    </Panel>
  )
}

