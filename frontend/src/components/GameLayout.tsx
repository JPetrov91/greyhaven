import { useEffect, useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import { fetchCurrentCombat } from '../api/combat'
import { fetchCurrentEncounter, searchEncounter } from '../api/encounter'
import { fetchCurrentExpedition } from '../api/expedition'
import { ActivityPanel } from './ActivityPanel'
import { CharacterSummaryPanel } from './CharacterSummaryPanel'
import { CombatPanel } from './CombatPanel'
import { EncounterPrompt } from './EncounterPrompt'
import { ExpeditionPanel } from './ExpeditionPanel'
import { InventoryPanel } from './InventoryPanel'
import { LocationPanel } from './LocationPanel'

export function GameLayout() {
  const queryClient = useQueryClient()
  const [searchError, setSearchError] = useState<string | null>(null)
  const [searching, setSearching] = useState(false)
  const [showExpedition, setShowExpedition] = useState(false)

  const combatQuery = useQuery({
    queryKey: ['combat'],
    queryFn: fetchCurrentCombat,
    retry: 2,
    refetchOnReconnect: true,
    refetchOnWindowFocus: true,
  })

  const encounterQuery = useQuery({
    queryKey: ['encounter'],
    queryFn: fetchCurrentEncounter,
    retry: 2,
    refetchOnReconnect: true,
    refetchOnWindowFocus: true,
  })

  const expeditionQuery = useQuery({
    queryKey: ['expedition'],
    queryFn: fetchCurrentExpedition,
    retry: false,
    refetchOnWindowFocus: true,
  })

  useEffect(() => {
    if (expeditionQuery.data && expeditionQuery.data.status !== 'CLAIMED') {
      setShowExpedition(true)
    }
  }, [expeditionQuery.data])

  const combat = combatQuery.data ?? null
  const encounter = encounterQuery.data ?? null

  async function handleSearchEncounter() {
    setSearchError(null)
    setSearching(true)
    try {
      const result = await searchEncounter()
      queryClient.setQueryData(['encounter'], result)
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
    await queryClient.invalidateQueries({ queryKey: ['combat'] })
  }

  const showCombat = combat !== null
  const showEncounter = !showCombat && encounter !== null
  const resumeLoading = combatQuery.isPending || encounterQuery.isPending
  const resumeFailed = combatQuery.isError || encounterQuery.isError

  return (
    <section className="game-layout" aria-label="Game workspace" data-testid="game-layout">
      <CharacterSummaryPanel mutationsDisabled={showCombat} />
      <div className="game-center-stack">
        {resumeLoading ? (
          <section className="game-column game-column-center" data-testid="gameplay-resume-loading">
            <h2>Greyhaven</h2>
            <p className="muted">Restoring combat and encounter state…</p>
          </section>
        ) : resumeFailed ? (
          <section className="game-column game-column-center" role="alert" data-testid="gameplay-resume-error">
            <h2>Unable to restore gameplay</h2>
            <p className="form-error">
              Combat or encounter state could not be loaded. Reconnect and retry before continuing.
            </p>
            <button
              type="button"
              className="travel-button"
              onClick={() => {
                void combatQuery.refetch()
                void encounterQuery.refetch()
              }}
            >
              Retry
            </button>
          </section>
        ) : showCombat ? (
          <CombatPanel
            combat={combat}
            onCombatUpdate={(updated) => {
              queryClient.setQueryData(['combat'], updated)
              void queryClient.invalidateQueries({ queryKey: ['activity'] })
            }}
          />
        ) : (
          <>
            <LocationPanel
              onSearchEncounter={() => void handleSearchEncounter()}
              searchBusy={searching}
              searchError={searchError}
              onOpenExpedition={() => setShowExpedition(true)}
            />
            {showExpedition ? <ExpeditionPanel onClose={() => setShowExpedition(false)} /> : null}
            {showEncounter ? (
              <EncounterPrompt
                encounter={encounter}
                onCleared={() => queryClient.setQueryData(['encounter'], null)}
                onCombatStarted={(started) => {
                  queryClient.setQueryData(['encounter'], null)
                  queryClient.setQueryData(['combat'], started)
                }}
              />
            ) : null}
          </>
        )}
        <InventoryPanel
          mutationsDisabled={showCombat}
          onMutated={showCombat ? () => void refreshCombatFromServer() : undefined}
        />
      </div>

      <ActivityPanel />
    </section>
  )
}
