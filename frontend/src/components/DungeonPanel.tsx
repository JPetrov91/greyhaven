import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import { abandonDungeon, advanceDungeon, enterDungeon, fetchCurrentDungeon, leaveDungeon } from '../api/dungeon'
import type { DungeonRunResponse } from '../api/types'
import { Button } from '../ui/Button'
import { ErrorState } from '../ui/ErrorState'
import { LoadingState } from '../ui/LoadingState'

type Props = {
  atEntrance: boolean
}

export function DungeonPanel({ atEntrance }: Props) {
  const queryClient = useQueryClient()
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const dungeonQuery = useQuery({
    queryKey: ['dungeon'],
    queryFn: fetchCurrentDungeon,
    retry: false,
  })

  async function run(action: () => Promise<DungeonRunResponse | void>) {
    setBusy(true)
    setError(null)
    try {
      const result = await action()
      if (result) {
        queryClient.setQueryData(['dungeon'], result)
      }
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['dungeon'] }),
        queryClient.invalidateQueries({ queryKey: ['encounter'] }),
        queryClient.invalidateQueries({ queryKey: ['combat'] }),
      ])
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Unable to update the dungeon expedition.')
    } finally {
      setBusy(false)
    }
  }

  if (dungeonQuery.isLoading) {
    return (
      <section className="location-section" data-testid="dungeon-panel">
        <h3>Ruined Keep</h3>
        <LoadingState>Loading dungeon…</LoadingState>
      </section>
    )
  }

  if (dungeonQuery.error instanceof ApiError) {
    return (
      <section className="location-section" data-testid="dungeon-panel">
        <h3>Ruined Keep</h3>
        <ErrorState onRetry={() => void dungeonQuery.refetch()}>{dungeonQuery.error.message}</ErrorState>
      </section>
    )
  }

  const dungeon = dungeonQuery.data
  const active = dungeon?.status === 'ACTIVE'

  return (
    <section className="location-section" data-testid="dungeon-panel" aria-label="Dungeon">
      <h3>Ruined Keep</h3>
      {active && dungeon ? (
        <>
          <p data-testid="dungeon-room-name">
            <strong>{dungeon.currentRoomName}</strong>
            {dungeon.paused ? ' · Paused' : ''}
          </p>
          <p className="muted" data-testid="dungeon-room-description">
            {dungeon.currentRoomDescription}
          </p>
          {dungeon.chosenBranch ? (
            <p className="muted">Branch taken: {dungeon.chosenBranch.toLowerCase()}</p>
          ) : null}
          {atEntrance && !dungeon.paused ? (
            <ul className="action-list" data-testid="dungeon-choices">
              {dungeon.choices.map((choice) => (
                <li key={`${choice.edgeCode}-${choice.roomCode}`}>
                  <span>
                    {choice.roomName}
                    {choice.optional ? ' (optional)' : ''}
                  </span>
                  <Button
                    type="button"
                    data-testid={`dungeon-advance-${choice.edgeCode}`}
                    disabled={busy}
                    onClick={() => void run(() => advanceDungeon(choice.edgeCode))}
                  >
                    {choice.optional ? 'Optional path' : 'Advance'}
                  </Button>
                </li>
              ))}
            </ul>
          ) : (
            <p className="muted">Return to the Ancient Ruins to resume the keep.</p>
          )}
          <div className="encounter-actions">
            {atEntrance ? (
              <Button
                type="button"
                variant="ghost"
                data-testid="dungeon-leave"
                disabled={busy}
                onClick={() => void run(() => leaveDungeon())}
              >
                Leave keep
              </Button>
            ) : null}
            <Button
              type="button"
              variant="ghost"
              data-testid="dungeon-abandon"
              disabled={busy}
              onClick={() => void run(() => abandonDungeon())}
            >
              Abandon expedition
            </Button>
          </div>
        </>
      ) : dungeon?.status === 'COMPLETED' ? (
        <p data-testid="dungeon-complete">The Warden has fallen. The keep remembers your passage.</p>
      ) : (
        <p className="muted">A sealed keep waits beneath the colonnades. Progress is saved if you leave.</p>
      )}
      {atEntrance && (!active || dungeon?.paused) ? (
        <Button
          type="button"
          data-testid="dungeon-enter"
          disabled={busy}
          onClick={() => void run(() => enterDungeon())}
        >
          {active ? 'Resume keep' : 'Enter the keep'}
        </Button>
      ) : null}
      {error ? (
        <p className="form-error" role="alert">
          {error}
        </p>
      ) : null}
    </section>
  )
}
