import { useEffect, useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useSearchParams } from 'react-router-dom'
import { ApiError } from '../api/client'
import { fetchCurrentCombat } from '../api/combat'
import { fetchCurrentEncounter, searchEncounter } from '../api/encounter'
import { fetchCurrentExpedition } from '../api/expedition'
import { ActivityPanel } from './ActivityPanel'
import { CharacterSummaryPanel } from './CharacterSummaryPanel'
import { ChatPanel } from './ChatPanel'
import { CombatPanel } from './CombatPanel'
import { EncounterPrompt } from './EncounterPrompt'
import { ExpeditionPanel } from './ExpeditionPanel'
import { InventoryPanel } from './InventoryPanel'
import { LocationPanel } from './LocationPanel'
import { MarketPanel } from './MarketPanel'
import { ErrorState } from '../ui/ErrorState'
import { LoadingState } from '../ui/LoadingState'

const MARKET_PANEL = 'market'

export function GameLayout() {
  const queryClient = useQueryClient()
  const [searchError, setSearchError] = useState<string | null>(null)
  const [searching, setSearching] = useState(false)
  const [showExpedition, setShowExpedition] = useState(false)

  // The marketplace lives in the address bar so the Market navigation entry can open it and a
  // refresh keeps the player where they were.
  const [searchParams, setSearchParams] = useSearchParams()
  const showMarket = searchParams.get('panel') === MARKET_PANEL

  function toggleMarket(open: boolean) {
    setSearchParams(
      (current) => {
        const next = new URLSearchParams(current)
        if (open) {
          next.set('panel', MARKET_PANEL)
        } else {
          next.delete('panel')
        }
        return next
      },
      { replace: true },
    )
  }

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
    <section className="game-workspace" aria-label="Game workspace" data-testid="game-layout">
      <div className="game-layout">
        <CharacterSummaryPanel mutationsDisabled={showCombat} />
        <div className="game-center-stack">
        {resumeLoading ? (
          <section className="game-column game-column-center" data-testid="gameplay-resume-loading">
            <h2>Greyhaven</h2>
            <LoadingState>Restoring combat and encounter state…</LoadingState>
          </section>
        ) : resumeFailed ? (
          <section className="game-column game-column-center" role="alert" data-testid="gameplay-resume-error">
            <h2>Unable to restore gameplay</h2>
            <ErrorState
              onRetry={() => {
                void combatQuery.refetch()
                void encounterQuery.refetch()
              }}
            >
              Combat or encounter state could not be loaded. Reconnect and retry before continuing.
            </ErrorState>
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
              onOpenMarket={() => toggleMarket(true)}
              onOpenChat={() => document.getElementById('global-chat')?.scrollIntoView({ block: 'nearest' })}
            />
            {showExpedition ? <ExpeditionPanel onClose={() => setShowExpedition(false)} /> : null}
            {showMarket ? <MarketPanel onClose={() => toggleMarket(false)} /> : null}
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
      </div>
      <div id="global-chat">
        <ChatPanel />
      </div>
    </section>
  )
}
