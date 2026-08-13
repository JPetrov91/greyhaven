import { useState } from 'react'
import { ApiError } from '../api/client'
import { fightEncounter, ignoreEncounter } from '../api/encounter'
import type { CombatResponse, EncounterSearchResponse } from '../api/types'

type Props = {
  encounter: EncounterSearchResponse
  onCleared: () => void
  onCombatStarted: (combat: CombatResponse) => void
}

export function EncounterPrompt({ encounter, onCleared, onCombatStarted }: Props) {
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  if (!encounter.found || !encounter.encounterId || !encounter.monster) {
    return (
      <section className="encounter-prompt" data-testid="encounter-nothing">
        <h3>Search result</h3>
        <p>Nothing found this time.</p>
        <button type="button" className="travel-button" data-testid="encounter-dismiss" onClick={onCleared}>
          Continue
        </button>
      </section>
    )
  }

  async function handleFight() {
    if (!encounter.encounterId) {
      return
    }
    setBusy(true)
    setError(null)
    try {
      const combat = await fightEncounter(encounter.encounterId)
      onCombatStarted(combat)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Unable to start combat.')
    } finally {
      setBusy(false)
    }
  }

  async function handleIgnore() {
    if (!encounter.encounterId) {
      return
    }
    setBusy(true)
    setError(null)
    try {
      await ignoreEncounter(encounter.encounterId)
      onCleared()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Unable to ignore encounter.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <section className="encounter-prompt" data-testid="encounter-prompt" aria-label="Encounter">
      <h3 data-testid="encounter-monster-name">{encounter.monster.name}</h3>
      <p className="muted">Level {encounter.monster.level}</p>
      <p>A hostile presence blocks the path. Fight or ignore?</p>
      <div className="encounter-actions">
        <button
          type="button"
          className="travel-button"
          data-testid="encounter-fight"
          disabled={busy}
          onClick={() => void handleFight()}
        >
          Fight
        </button>
        <button
          type="button"
          className="nav-button"
          data-testid="encounter-ignore"
          disabled={busy}
          onClick={() => void handleIgnore()}
        >
          Ignore
        </button>
      </div>
      {error ? (
        <p className="form-error" role="alert" data-testid="encounter-error">
          {error}
        </p>
      ) : null}
    </section>
  )
}
