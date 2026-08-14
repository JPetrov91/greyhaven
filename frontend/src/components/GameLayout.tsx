import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { ApiError } from '../api/client'
import { fetchCurrentCombat } from '../api/combat'
import { fetchCurrentEncounter, searchEncounter } from '../api/encounter'
import { fetchCurrentExpedition } from '../api/expedition'
import { ActivityPanel } from './ActivityPanel'
import { CharacterSummaryPanel } from './CharacterSummaryPanel'
import { ChatPanel } from './ChatPanel'
import { CombatPanel } from './CombatPanel'
import { EncounterPrompt } from './EncounterPrompt'
import { EquipmentOverviewCard } from './EquipmentOverviewCard'
import { ExpeditionPanel } from './ExpeditionPanel'
import { GameLeftNav } from './GameLeftNav'
import { GameTopBar } from './GameTopBar'
import { GuildPlaceholder } from './GuildPlaceholder'
import { EquipmentPanel } from './EquipmentPanel'
import { InventoryPanel } from './InventoryPanel'
import { LocationPanel } from './LocationPanel'
import { MarketPanel } from './MarketPanel'
import { MasteryPanel } from './MasteryPanel'
import { ErrorState } from '../ui/ErrorState'
import { gameLink, gameViewFromLocation } from '../ui/gameNav'
import { LoadingState } from '../ui/LoadingState'

const MARKET_PANEL = 'market'

export function GameLayout() {
  const queryClient = useQueryClient()
  const location = useLocation()
  const navigate = useNavigate()
  const view = gameViewFromLocation(location)
  const [searchError, setSearchError] = useState<string | null>(null)
  const [searching, setSearching] = useState(false)

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
          next.delete('listItem')
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
  const claimableExpedition = expeditionQuery.data?.status === 'COMPLETED'

  const locationHandlers = {
    onSearchEncounter: () => void handleSearchEncounter(),
    searchBusy: searching,
    searchError,
    onOpenExpedition: () => navigate(gameLink('expeditions')),
    onOpenMarket: () => toggleMarket(true),
    onOpenChat: () => document.getElementById('global-chat')?.scrollIntoView({ block: 'nearest' }),
    onOpenWorld: () => navigate(gameLink('world')),
  }

  let mainContent
  if (resumeLoading) {
    mainContent = (
      <section className="game-column game-column-center" data-testid="gameplay-resume-loading">
        <h2>Greyhaven</h2>
        <LoadingState>Restoring combat and encounter state…</LoadingState>
      </section>
    )
  } else if (resumeFailed) {
    mainContent = (
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
    )
  } else if (showCombat) {
    mainContent = null
  } else if (showEncounter) {
    mainContent = (
      <EncounterPrompt
        encounter={encounter}
        onCleared={() => queryClient.setQueryData(['encounter'], null)}
        onCombatStarted={(started) => {
          queryClient.setQueryData(['encounter'], null)
          queryClient.setQueryData(['combat'], started)
          void queryClient.invalidateQueries({ queryKey: ['dungeon'] })
        }}
      />
    )
  } else if (view === 'character') {
    mainContent = <CharacterSummaryPanel mutationsDisabled={showCombat} />
  } else if (view === 'inventory') {
    mainContent = (
      <InventoryPanel
        mutationsDisabled={showCombat}
        onMutated={showCombat ? () => void refreshCombatFromServer() : undefined}
      />
    )
  } else if (view === 'equipment') {
    mainContent = <EquipmentPanel mutationsDisabled={showCombat} />
  } else if (view === 'mastery') {
    mainContent = <MasteryPanel mutationsDisabled={showCombat} />
  } else if (view === 'market' || showMarket) {
    mainContent = <MarketPanel onClose={() => toggleMarket(false)} />
  } else if (view === 'expeditions') {
    mainContent = <ExpeditionPanel />
  } else if (view === 'world') {
    mainContent = <LocationPanel variant="full" {...locationHandlers} />
  } else {
    mainContent = (
      <div className="home-dashboard">
        <LocationPanel variant="hero" {...locationHandlers} />
        <div className="home-mid-row">
          <CharacterSummaryPanel variant="overview" mutationsDisabled={showCombat} />
          <EquipmentOverviewCard />
          <ExpeditionPanel variant="card" />
        </div>
        <div className="home-bottom-row">
          <div id="global-chat">
            <ChatPanel />
          </div>
          <GuildPlaceholder />
        </div>
      </div>
    )
  }

  return (
    <section
      className="game-shell"
      aria-label="Game workspace"
      data-testid="game-layout"
      data-combat-active={showCombat ? 'true' : undefined}
    >
      <GameTopBar
        combatContext={
          showCombat && combat?.monster
            ? { monsterName: combat.monster.name, roundNumber: combat.roundNumber }
            : null
        }
      />
      {showCombat && combat ? (
        <div className="game-shell-body game-shell-body-combat">
          <CombatPanel
            combat={combat}
            onCombatUpdate={(updated) => {
              queryClient.setQueryData(['combat'], updated)
              void queryClient.invalidateQueries({ queryKey: ['activity'] })
              void queryClient.invalidateQueries({ queryKey: ['dungeon'] })
            }}
          />
        </div>
      ) : (
        <div className="game-shell-body">
          <GameLeftNav />
          <div className="game-shell-main">{mainContent}</div>
          <ActivityPanel
            claimableExpedition={claimableExpedition}
            combatActive={false}
            encounterActive={showEncounter}
          />
        </div>
      )}
    </section>
  )
}
