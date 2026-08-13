import { useEffect, useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import { fetchCurrentCombat } from '../api/combat'
import { fetchCurrentEncounter, searchEncounter } from '../api/encounter'
import type { CombatResponse, EncounterSearchResponse } from '../api/types'
import { CharacterSummaryPanel } from './CharacterSummaryPanel'
import { CombatPanel } from './CombatPanel'
import { EncounterPrompt } from './EncounterPrompt'
import { InventoryPanel } from './InventoryPanel'
import { LocationPanel } from './LocationPanel'

export function GameLayout() {
  const queryClient = useQueryClient()
  const [combat, setCombat] = useState<CombatResponse | null>(null)
  const [encounter, setEncounter] = useState<EncounterSearchResponse | null>(null)
  const [searchError, setSearchError] = useState<string | null>(null)
  const [searching, setSearching] = useState(false)

  useEffect(() => {
    let cancelled = false
    void Promise.all([fetchCurrentCombat(), fetchCurrentEncounter()])
      .then(([currentCombat, currentEncounter]) => {
        if (cancelled) {
          return
        }
        if (currentCombat) {
          setCombat(currentCombat)
          setEncounter(null)
          return
        }
        if (currentEncounter?.found) {
          setEncounter(currentEncounter)
        }
      })
      .catch(() => {
        /* no resumable combat/encounter */
      })
    return () => {
      cancelled = true
    }
  }, [])

  async function handleSearchEncounter() {
    setSearchError(null)
    setSearching(true)
    try {
      const result = await searchEncounter()
      setEncounter(result)
      await queryClient.invalidateQueries({ queryKey: ['character'] })
    } catch (error) {
      if (error instanceof ApiError) {
        setSearchError(error.message)
      } else {
        setSearchError('Unable to search for an encounter.')
      }
    } finally {
      setSearching(false)
    }
  }

  async function refreshCombatFromServer() {
    try {
      const current = await fetchCurrentCombat()
      setCombat(current)
    } catch {
      /* keep existing combat snapshot */
    }
  }

  const showCombat = combat !== null
  const showEncounter = !showCombat && encounter !== null

  return (
    <section className="game-layout" aria-label="Game workspace" data-testid="game-layout">
      <CharacterSummaryPanel />
      <div className="game-center-stack">
        {showCombat ? (
          <CombatPanel combat={combat} onCombatUpdate={setCombat} />
        ) : (
          <>
            <LocationPanel
              onSearchEncounter={() => void handleSearchEncounter()}
              searchBusy={searching}
              searchError={searchError}
            />
            {showEncounter ? (
              <EncounterPrompt
                encounter={encounter}
                onCleared={() => setEncounter(null)}
                onCombatStarted={(started) => {
                  setEncounter(null)
                  setCombat(started)
                }}
              />
            ) : null}
          </>
        )}
        <InventoryPanel onMutated={showCombat ? () => void refreshCombatFromServer() : undefined} />
      </div>

      <aside className="game-column game-column-right">
        <h2>Activity</h2>
        <p>Feed and chat will appear here.</p>
      </aside>
    </section>
  )
}
