import { useEffect, useState, type ReactNode } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import { fetchCurrentLocation, fetchDestinations, fetchNearbyCharacters, moveToLocation } from '../api/world'
import { fetchNpcs } from '../api/npcs'
import { fetchPublicCharacter, type PublicCharacterResponse } from '../api/pvp'
import type { DestinationResponse, LocationResponse } from '../api/types'
import { Button } from '../ui/Button'
import { ComingLaterButton } from '../ui/ComingLater'
import { EmptyState } from '../ui/EmptyState'
import { ErrorState } from '../ui/ErrorState'
import { LoadingState } from '../ui/LoadingState'
import {
  LocationCrest,
  LocationIcon,
  locationActionArtUrl,
  locationArtUrl,
  locationWeather,
  noticeBoardArtUrl,
} from '../ui/locationMedia'
import type { LocationActionIconName } from '../ui/locationMedia'
import { DungeonPanel } from './DungeonPanel'
import { HereNowList } from './HereNowList'
import { LocationQuestAction } from './LocationQuestAction'
import { NoticeBoard } from './noticeBoard/NoticeBoard'
import { NpcDialogue } from './NpcDialogue'
import { NpcStrip } from './NpcStrip'
import { SparringYardPanel } from './SparringYardPanel'

type Props = {
  onSearchEncounter?: () => void
  searchBusy?: boolean
  searchError?: string | null
  onOpenExpedition?: () => void
  onOpenMarket?: () => void
  onOpenChat?: () => void
  onOpenWorld?: () => void
  onOpenTravel?: () => void
  onTravelClose?: () => void
  travelOpen?: boolean
  onOpenArena?: () => void
  onOpenSparring?: () => void
  onOpenCrafting?: () => void
  showYard?: boolean
  variant?: 'full' | 'hero'
  talkOpen?: boolean
  onTalkOpen?: () => void
  onTalkClose?: () => void
  noticeOpen?: boolean
  onNoticeOpen?: () => void
  onNoticeClose?: () => void
}

function formatGreyhavenTime(date: Date): string {
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

function useGreyhavenClock(): string {
  const [now, setNow] = useState(() => formatGreyhavenTime(new Date()))

  useEffect(() => {
    const tick = () => setNow(formatGreyhavenTime(new Date()))
    tick()
    const id = window.setInterval(tick, 1000)
    return () => window.clearInterval(id)
  }, [])

  return now
}

export function LocationPanel({
  onSearchEncounter,
  searchBusy = false,
  searchError = null,
  onOpenExpedition,
  onOpenMarket,
  onOpenChat,
  onOpenWorld,
  onOpenTravel,
  onTravelClose,
  travelOpen = false,
  onOpenArena,
  onOpenSparring,
  onOpenCrafting,
  showYard = false,
  variant = 'full',
  talkOpen: talkOpenProp,
  onTalkOpen,
  onTalkClose,
  noticeOpen = false,
  onNoticeOpen,
  onNoticeClose,
}: Props) {
  const queryClient = useQueryClient()
  const [moveError, setMoveError] = useState<string | null>(null)
  const [movingToId, setMovingToId] = useState<string | null>(null)
  const [inspected, setInspected] = useState<PublicCharacterResponse | null>(null)
  const [internalTalkOpen, setInternalTalkOpen] = useState(false)
  const [talkNpcCode, setTalkNpcCode] = useState<string | undefined>()
  const talkOpen = talkOpenProp ?? internalTalkOpen
  const openTalk = onTalkOpen ?? (() => setInternalTalkOpen(true))
  const closeTalk = onTalkClose ?? (() => setInternalTalkOpen(false))

  function openTalkWith(code?: string) {
    setTalkNpcCode(code)
    openTalk()
  }

  function closeTalkPanel() {
    setTalkNpcCode(undefined)
    closeTalk()
  }

  const locationQuery = useQuery({
    queryKey: ['location'],
    queryFn: fetchCurrentLocation,
    retry: false,
  })

  const destinationsQuery = useQuery({
    queryKey: ['destinations'],
    queryFn: fetchDestinations,
    retry: false,
  })

  const nearbyQuery = useQuery({
    queryKey: ['nearby-characters'],
    queryFn: fetchNearbyCharacters,
    retry: false,
  })

  const npcsQuery = useQuery({
    queryKey: ['npcs'],
    queryFn: fetchNpcs,
    retry: false,
    enabled: variant !== 'hero',
  })

  async function handleMove(destinationLocationId: string) {
    setMoveError(null)
    setMovingToId(destinationLocationId)
    try {
      await moveToLocation(destinationLocationId)
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['location'] }),
        queryClient.invalidateQueries({ queryKey: ['destinations'] }),
        queryClient.invalidateQueries({ queryKey: ['nearby-characters'] }),
        queryClient.invalidateQueries({ queryKey: ['character'] }),
        queryClient.invalidateQueries({ queryKey: ['quests'] }),
        queryClient.invalidateQueries({ queryKey: ['npcs'] }),
      ])
      onTravelClose?.()
    } catch (error) {
      if (error instanceof ApiError) {
        setMoveError(error.message)
      } else {
        setMoveError('Unable to travel right now.')
      }
    } finally {
      setMovingToId(null)
    }
  }

  if (locationQuery.isLoading) {
    return (
      <div className="game-column game-column-center" data-testid="location-panel">
        <h2>Greyhaven</h2>
        <LoadingState>Loading location…</LoadingState>
      </div>
    )
  }

  if (locationQuery.error instanceof ApiError) {
    return (
      <div className="game-column game-column-center" data-testid="location-panel">
        <h2>Greyhaven</h2>
        <ErrorState onRetry={() => void locationQuery.refetch()}>{locationQuery.error.message}</ErrorState>
      </div>
    )
  }

  const location = locationQuery.data
  if (!location) {
    return null
  }

  const destinations = destinationsQuery.data?.destinations ?? []
  const nearby = nearbyQuery.data?.characters ?? []
  const nearbyTruncated = nearbyQuery.data?.truncated ?? false
  const nearbyLimit = nearbyQuery.data?.limit ?? nearby.length
  const nearbyTotal = nearbyQuery.data?.totalCount ?? nearby.length
  const npcs = npcsQuery.data?.npcs ?? []

  async function inspectNearby(id: string) {
    try {
      setInspected(await fetchPublicCharacter(id))
    } catch {
      setInspected(null)
    }
  }

  const hero = variant === 'hero'
  const atYard = location.actions.includes('CHALLENGE_DUEL') || location.actions.includes('START_SPARRING_DRILL')
  const yard = showYard && atYard ? <SparringYardPanel /> : null

  if (hero && noticeOpen) {
    return (
      <>
        <div
          className="game-column location-hero location-hero-board"
          data-testid="location-panel"
          data-workspace="notice-board"
        >
          <LocationBoardScene location={location} />
          <NoticeBoard locationCode={location.code} open onClose={() => onNoticeClose?.()} onOpenTalk={openTalk} />
        </div>
        {yard}
      </>
    )
  }

  if (hero) {
    return (
      <>
        <div className="game-column location-hero" data-testid="location-panel">
          <LocationHero
            location={location}
            destinations={destinations}
            movingToId={movingToId}
            moveError={moveError}
            searchBusy={searchBusy}
            searchError={searchError}
            onSearchEncounter={onSearchEncounter}
            onOpenWorld={onOpenWorld}
            onOpenTravel={onOpenTravel}
            onOpenMarket={onOpenMarket}
            onOpenChat={onOpenChat}
            onOpenExpedition={onOpenExpedition}
            onOpenArena={onOpenArena}
            onOpenSparring={onOpenSparring}
            showYard={showYard}
            onOpenCrafting={onOpenCrafting}
            onOpenTalk={openTalk}
            onOpenNotice={onNoticeOpen}
            noticeOpen={noticeOpen}
            onMove={(id) => void handleMove(id)}
          />
          <LocationQuestAction
            locationCode={location.code}
            onOpenTalk={openTalk}
            onSearchEncounter={onSearchEncounter}
            onOpenWorld={onOpenWorld}
          />
          <NpcDialogue
            open={talkOpen}
            onClose={closeTalkPanel}
            onOpenMarket={onOpenMarket}
            initialNpcCode={talkNpcCode}
          />
        </div>
        {yard}
      </>
    )
  }

  const openTravel = onOpenTravel ?? (() => undefined)
  const closeTravel = onTravelClose ?? (() => undefined)
  const safe = location.safety === 'SAFE'

  return (
    <div className="locations-workspace" data-testid="location-panel" id="world">
      {noticeOpen ? (
        <div className="location-hero location-hero-board" data-workspace="notice-board">
          <LocationBoardScene location={location} />
          <NoticeBoard locationCode={location.code} open onClose={() => onNoticeClose?.()} onOpenTalk={openTalk} />
        </div>
      ) : (
        <div className="locations-split">
          <div className="locations-hero-well">
            <div
              className="location-hero-art"
              aria-hidden="true"
              style={{ backgroundImage: `url(${locationArtUrl(location.code)})` }}
            />
            <div className="locations-hero-overlay">
              <div className="location-hero-identity">
                <LocationCrest />
                <div>
                  <p className="location-hero-kicker">Current location</p>
                  <h2 className="location-hero-region">{location.region}</h2>
                  <p className="location-hero-place" data-testid="current-location">
                    {location.name}
                  </p>
                </div>
              </div>
              <p className="location-description" data-testid="location-description">
                {location.description}
              </p>
              {location.recommendedLevelMin != null && location.recommendedLevelMax != null ? (
                <p className="muted" data-testid="location-band">
                  Recommended levels {location.recommendedLevelMin}–{location.recommendedLevelMax}
                </p>
              ) : null}
              <p className="location-hero-pills">
                <span
                  className={
                    safe ? 'location-hero-pill location-hero-pill-safe' : 'location-hero-pill location-hero-pill-danger'
                  }
                  data-testid="location-safety"
                >
                  <LocationIcon name={safe ? 'spark' : 'danger'} />
                  {safe ? 'Safe Zone' : 'Dangerous'}
                </span>
                <span className="location-hero-pill location-hero-pill-muted" data-testid="location-pvp">
                  <LocationIcon name={safe ? 'nopvp' : 'pve'} />
                  {safe ? 'No PvP' : 'PvE'}
                </span>
                <span className="muted" data-testid="location-code">
                  {location.code}
                </span>
              </p>
              {location.actions.includes('ENTER_DUNGEON') ? <DungeonPanel atEntrance /> : null}
              <nav className="location-place-verbs" aria-label="Place actions">
                <HeroTile
                  testId="hero-travel"
                  icon="compass"
                  title="Travel"
                  subtitle="Change location"
                  onClick={() => openTravel()}
                />
                {location.actions.includes('SEARCH_ENCOUNTER') ? (
                  <HeroTile
                    testId="search-encounter-button"
                    icon="search"
                    title={searchBusy ? 'Searching…' : 'Search'}
                    subtitle="Hunt nearby"
                    disabled={searchBusy || !onSearchEncounter}
                    onClick={() => onSearchEncounter?.()}
                  />
                ) : null}
                {onNoticeOpen && location.actions.includes('NOTICE_BOARD') ? (
                  <HeroTile
                    testId="hero-notice"
                    icon="notice"
                    title="Notice"
                    subtitle="Quest board"
                    selected={noticeOpen}
                    onClick={() => onNoticeOpen()}
                  />
                ) : null}
                {location.actions.includes('ENTER_ARENA') ? (
                  <HeroTile
                    testId="enter-arena-action"
                    icon="arena"
                    title="Arena"
                    subtitle="Challenge defenders"
                    onClick={() => onOpenArena?.()}
                  />
                ) : null}
                {atYard ? (
                  <HeroTile
                    testId="enter-sparring-action"
                    icon="arena"
                    title={showYard ? 'Back' : 'Duels'}
                    subtitle="Live spars & drills"
                    selected={showYard}
                    onClick={() => onOpenSparring?.()}
                  />
                ) : null}
              </nav>
              {searchError ? (
                <p className="form-error" role="alert" data-testid="search-encounter-error">
                  {searchError}
                </p>
              ) : null}
            </div>
            {yard}
          </div>
          <aside className="locations-people">
            <NpcStrip npcs={npcs} onTalk={(code) => openTalkWith(code)} />
            <HereNowList
              locationName={location.name}
              characters={nearby}
              loading={nearbyQuery.isLoading}
              truncated={nearbyTruncated}
              totalCount={nearbyTotal}
              limit={nearbyLimit}
              onInspect={(id) => void inspectNearby(id)}
            />
            {inspected ? (
              <aside data-testid="public-inspect">
                <h3>{inspected.name}</h3>
                <p>
                  Level {inspected.level} · Rating {inspected.arenaRating}
                </p>
                <p>
                  STR {inspected.strength} AGI {inspected.agility} END {inspected.endurance} PER {inspected.perception}
                </p>
                <ul>
                  {inspected.equipment.map((item) => (
                    <li key={item.slot}>
                      {item.slot}: {item.displayName}
                    </li>
                  ))}
                </ul>
                <Button type="button" onClick={() => setInspected(null)}>
                  Close
                </Button>
              </aside>
            ) : null}
          </aside>
        </div>
      )}
      {travelOpen ? (
        <div
          className="locations-travel-sheet"
          data-testid="travel-sheet"
          role="dialog"
          aria-labelledby="destinations-heading"
        >
          <div className="locations-travel-sheet-head">
            <h3 id="destinations-heading">Travel</h3>
            <Button type="button" data-testid="travel-sheet-close" onClick={() => closeTravel()}>
              Close
            </Button>
          </div>
          {destinationsQuery.isLoading ? (
            <LoadingState>Loading destinations…</LoadingState>
          ) : destinations.length === 0 ? (
            <EmptyState>No connected destinations.</EmptyState>
          ) : (
            <ul className="destination-list" data-testid="destination-list">
              {destinations.map((destination) => (
                <li key={destination.id}>
                  <div>
                    <strong data-testid={`destination-name-${destination.code}`}>{destination.name}</strong>
                    <span className="muted">
                      {' '}
                      · {destination.safety === 'SAFE' ? 'Safe' : 'Dangerous'}
                      {destination.recommendedLevelMin != null
                        ? ` · Lv ${destination.recommendedLevelMin}–${destination.recommendedLevelMax}`
                        : ''}
                    </span>
                  </div>
                  <Button
                    type="button"
                    data-testid={`destination-${destination.code}`}
                    disabled={movingToId !== null}
                    onClick={() => void handleMove(destination.id)}
                  >
                    {movingToId === destination.id ? 'Traveling…' : 'Travel'}
                  </Button>
                </li>
              ))}
            </ul>
          )}
          {moveError ? (
            <p className="form-error" role="alert" data-testid="move-error">
              {moveError}
            </p>
          ) : null}
        </div>
      ) : null}
      {noticeOpen ? null : (
        <LocationQuestAction
          locationCode={location.code}
          onOpenTalk={openTalk}
          onSearchEncounter={onSearchEncounter}
          onOpenWorld={onOpenWorld}
        />
      )}
      <NpcDialogue
        open={talkOpen}
        onClose={closeTalkPanel}
        onOpenMarket={onOpenMarket}
        initialNpcCode={talkNpcCode}
      />
    </div>
  )
}
export type LocationHeroProps = {
  location: LocationResponse
  destinations: DestinationResponse[]
  movingToId: string | null
  moveError: string | null
  searchBusy: boolean
  searchError: string | null
  /** When set, skips the live Greyhaven clock so visual fixtures stay stable. */
  clock?: string
  onSearchEncounter?: () => void
  onOpenWorld?: () => void
  onOpenTravel?: () => void
  onOpenMarket?: () => void
  onOpenChat?: () => void
  onOpenExpedition?: () => void
  onOpenArena?: () => void
  onOpenSparring?: () => void
  showYard?: boolean
  onOpenCrafting?: () => void
  onOpenTalk?: () => void
  onOpenNotice?: () => void
  noticeOpen?: boolean
  onMove: (destinationLocationId: string) => void
}

type HeroTileModel = {
  testId: string
  icon: LocationActionIconName
  title: string
  subtitle: string
  onClick?: () => void
  disabled?: boolean
  comingLater?: boolean
  selected?: boolean
}

function heroActionTiles({
  location,
  destinations,
  movingToId,
  searchBusy,
  onSearchEncounter,
  onOpenWorld,
  onOpenTravel,
  onOpenMarket,
  onOpenChat,
  onOpenExpedition,
  onOpenArena,
  onOpenSparring,
  showYard = false,
  onOpenCrafting,
  onOpenTalk,
  onOpenNotice,
  noticeOpen = false,
  onMove,
}: Pick<
  LocationHeroProps,
  | 'location'
  | 'destinations'
  | 'movingToId'
  | 'searchBusy'
  | 'onSearchEncounter'
  | 'onOpenWorld'
  | 'onOpenTravel'
  | 'onOpenMarket'
  | 'onOpenChat'
  | 'onOpenExpedition'
  | 'onOpenArena'
  | 'onOpenSparring'
  | 'showYard'
  | 'onOpenCrafting'
  | 'onOpenTalk'
  | 'onOpenNotice'
  | 'noticeOpen'
  | 'onMove'
>): HeroTileModel[] {
  const actions = new Set(location.actions)
  const tavernDestination = destinations.find((destination) => destination.code === 'TAVERN')
  const atTavern = location.code === 'TAVERN'
  const tiles: HeroTileModel[] = [
    {
      testId: 'hero-travel',
      icon: 'compass',
      title: 'Travel',
      subtitle: 'Change location',
      onClick: () => (onOpenTravel ?? onOpenWorld)?.(),
    },
  ]

  if (actions.has('CHALLENGE_DUEL') || actions.has('START_SPARRING_DRILL')) {
    tiles.push({
      testId: 'enter-sparring-action',
      icon: 'arena',
      title: 'Duels',
      subtitle: 'Live spars & drills',
      selected: showYard,
      onClick: () => onOpenSparring?.(),
    })
  }

  if (actions.has('SEARCH_ENCOUNTER')) {
    tiles.push({
      testId: 'search-encounter-button',
      icon: 'search',
      title: searchBusy ? 'Searching…' : 'Search',
      subtitle: 'Hunt nearby',
      disabled: searchBusy || !onSearchEncounter,
      onClick: () => onSearchEncounter?.(),
    })
  }

  if (actions.has('ENTER_ARENA')) {
    tiles.push({
      testId: 'enter-arena-action',
      icon: 'arena',
      title: 'Arena',
      subtitle: 'Challenge defenders',
      onClick: () => onOpenArena?.(),
    })
  }

  if (actions.has('START_EXPEDITION') || actions.has('INSPECT_EXPEDITIONS')) {
    tiles.push({
      testId: 'start-expedition-action',
      icon: 'expedition',
      title: 'Expeditions',
      subtitle: 'Send a party',
      onClick: () => onOpenExpedition?.(),
    })
  }

  if (actions.has('CRAFT') || actions.has('CLAIM_CRAFT') || actions.has('SALVAGE')) {
    tiles.push({
      testId: 'open-crafting-action',
      icon: 'craft',
      title: 'Crafting',
      subtitle: 'Jobs & salvage',
      onClick: () => onOpenCrafting?.(),
    })
  }

  if (actions.has('BROWSE_MARKET') || actions.has('CREATE_LISTING') || actions.has('BUY_ITEM')) {
    tiles.push({
      testId: 'open-market-BROWSE_MARKET',
      icon: 'market',
      title: 'Local Market',
      subtitle: 'Buy & sell',
      onClick: () => onOpenMarket?.(),
    })
  }

  if (actions.has('VIEW_CHAT')) {
    tiles.push({
      testId: 'open-chat-action',
      icon: 'chat',
      title: 'Chat',
      subtitle: 'Talk here',
      onClick: () => onOpenChat?.(),
    })
  }

  if (atTavern) {
    tiles.push({
      testId: 'hero-tavern',
      icon: 'tavern',
      title: 'Tavern',
      subtitle: 'Find players',
      onClick: () => (onOpenChat ?? onOpenExpedition)?.(),
    })
  } else if (tavernDestination) {
    tiles.push({
      testId: 'hero-tavern',
      icon: 'tavern',
      title: 'Tavern',
      subtitle: movingToId === tavernDestination.id ? 'Traveling…' : 'Find players',
      disabled: movingToId !== null,
      onClick: () => onMove(tavernDestination.id),
    })
  }

  if (location.safety === 'SAFE') {
    if (!tiles.some((tile) => tile.testId === 'hero-tavern')) {
      tiles.push({
        testId: 'hero-tavern',
        icon: 'tavern',
        title: 'Tavern',
        subtitle: 'Find players',
        comingLater: true,
      })
    }
    if (!tiles.some((tile) => tile.testId === 'open-market-BROWSE_MARKET')) {
      tiles.push({
        testId: 'open-market-BROWSE_MARKET',
        icon: 'market',
        title: 'Local Market',
        subtitle: 'Buy & sell',
        onClick: () => onOpenMarket?.(),
      })
    }
    if (actions.has('TALK_NPCS')) {
      tiles.push({
        testId: 'talk-npcs-action',
        icon: 'notice',
        title: 'People',
        subtitle: 'Talk here',
        onClick: () => onOpenTalk?.(),
      })
    }
    if (actions.has('NOTICE_BOARD')) {
      tiles.push({
        testId: 'hero-notice',
        icon: 'notice',
        title: 'Notice Board',
        subtitle: 'Quests & tasks',
        selected: noticeOpen,
        disabled: !onOpenNotice,
        onClick: () => onOpenNotice?.(),
      })
    } else if (!actions.has('TALK_NPCS')) {
      tiles.push({
        testId: 'hero-notice',
        icon: 'notice',
        title: 'Notice Board',
        subtitle: 'Quests & tasks',
        comingLater: true,
      })
    }
    tiles.push({
      testId: 'hero-guild',
      icon: 'guild',
      title: 'Guild Hall',
      subtitle: 'Guild activities',
      comingLater: true,
    })
  }

  return tiles.slice(0, 6)
}

function LocationBoardScene({ location }: { location: LocationResponse }) {
  return (
    <div className="location-board-scene" data-testid="notice-board-scene">
      <span className="location-board-corner location-board-corner-bl" aria-hidden="true" />
      <span className="location-board-corner location-board-corner-br" aria-hidden="true" />
      <div
        className="location-hero-art location-board-art"
        aria-hidden="true"
        style={{ backgroundImage: `url(${noticeBoardArtUrl()})` }}
      />
      <div className="location-board-identity">
        <p className="location-hero-kicker">Current location</p>
        <h2 className="location-hero-region">{location.region}</h2>
        <p className="location-board-district" data-testid="current-location">
          {location.name}
        </p>
        <p className="location-hero-pills">
          <span
            className={
              location.safety === 'SAFE'
                ? 'location-hero-pill location-hero-pill-safe'
                : 'location-hero-pill location-hero-pill-danger'
            }
          >
            {location.safety === 'SAFE' ? 'Safe Zone' : 'Dangerous'}
          </span>
        </p>
      </div>
    </div>
  )
}

export function LocationHero({
  location,
  destinations,
  movingToId,
  moveError,
  searchBusy,
  searchError,
  clock: clockOverride,
  onSearchEncounter,
  onOpenWorld,
  onOpenTravel,
  onOpenMarket,
  onOpenChat,
  onOpenExpedition,
  onOpenArena,
  onOpenSparring,
  showYard = false,
  onOpenCrafting,
  onOpenTalk,
  onOpenNotice,
  noticeOpen = false,
  onMove,
}: LocationHeroProps) {
  const liveClock = useGreyhavenClock()
  const clock = clockOverride ?? liveClock
  const weather = locationWeather(location.code)
  const safe = location.safety === 'SAFE'
  const tiles = heroActionTiles({
    location,
    destinations,
    movingToId,
    searchBusy,
    onSearchEncounter,
    onOpenWorld,
    onOpenTravel,
    onOpenMarket,
    onOpenChat,
    onOpenExpedition,
    onOpenArena,
    onOpenSparring,
    showYard,
    onOpenCrafting,
    onOpenTalk,
    onOpenNotice,
    noticeOpen,
    onMove,
  })

  return (
    <>
      <div
        className="location-hero-art"
        aria-hidden="true"
        style={{ backgroundImage: `url(${locationArtUrl(location.code)})` }}
      />
      <div className="location-hero-body">
        <div className="location-hero-top">
          <div className="location-hero-identity">
            <LocationCrest />
            <div>
              <p className="location-hero-kicker">Current location</p>
              <h2 className="location-hero-region">{location.region}</h2>
              <p className="location-hero-place" data-testid="current-location">
                {location.name}
              </p>
            </div>
          </div>
          <div className="location-hero-env">
            <div className="location-hero-env-card">
              <div className="location-hero-clock" data-testid="location-clock">
                <strong>{clock}</strong>
                <span>Greyhaven time</span>
              </div>
              <p className="location-hero-weather" data-testid="location-weather">
                {locationActionArtUrl(weather.icon) ? (
                  <img className="location-hero-env-art" src={locationActionArtUrl(weather.icon)} alt="" />
                ) : (
                  <LocationIcon name={weather.icon} />
                )}
                <span className="location-hero-weather-label">{weather.label}</span>
                <span className="location-hero-temp">{weather.temperature}</span>
              </p>
              <Button
                type="button"
                variant="secondary"
                className="location-hero-map"
                data-testid="hero-world-map"
                onClick={() => onOpenWorld?.()}
              >
                {locationActionArtUrl('globe') ? (
                  <img className="location-hero-env-art" src={locationActionArtUrl('globe')} alt="" />
                ) : (
                  <LocationIcon name="globe" />
                )}
                World Map
              </Button>
            </div>
          </div>
        </div>

        <p className="location-description" data-testid="location-description">
          {location.description}
        </p>
        {location.recommendedLevelMin != null && location.recommendedLevelMax != null ? (
          <p className="muted" data-testid="location-band">
            Recommended levels {location.recommendedLevelMin}–{location.recommendedLevelMax}
          </p>
        ) : null}
        {location.actions.includes('ENTER_DUNGEON') ? <DungeonPanel atEntrance /> : null}
        <p className="location-hero-pills">
          <span
            className={safe ? 'location-hero-pill location-hero-pill-safe' : 'location-hero-pill location-hero-pill-danger'}
            data-testid="location-safety"
          >
            <LocationIcon name={safe ? 'spark' : 'danger'} />
            {safe ? 'Safe Zone' : 'Dangerous'}
          </span>
          <span className="location-hero-pill location-hero-pill-muted" data-testid="location-pvp">
            <LocationIcon name={safe ? 'nopvp' : 'pve'} />
            {safe ? 'No PvP' : 'PvE'}
          </span>
        </p>

        <nav className="location-hero-actions" aria-label="Location actions">
          {tiles.map((tile) => (
            <HeroTile key={tile.testId} {...tile} />
          ))}
        </nav>
        {moveError ? (
          <p className="form-error" role="alert" data-testid="move-error">
            {moveError}
          </p>
        ) : null}
        {searchError ? (
          <p className="form-error" role="alert" data-testid="search-encounter-error">
            {searchError}
          </p>
        ) : null}
      </div>
    </>
  )
}

type TileProps = {
  testId: string
  icon: LocationActionIconName
  title: string
  subtitle: string
  onClick?: () => void
  disabled?: boolean
  comingLater?: boolean
  selected?: boolean
}

function HeroTile({ testId, icon, title, subtitle, onClick, disabled, comingLater, selected }: TileProps) {
  const content: ReactNode = (
    <>
      <span className="location-hero-tile-icon">
        {locationActionArtUrl(icon) ? (
          <img className="location-hero-tile-art" src={locationActionArtUrl(icon)} alt="" />
        ) : (
          <LocationIcon name={icon} />
        )}
      </span>
      <span className="location-hero-tile-copy">
        <strong>{title}</strong>
        <span>{subtitle}</span>
      </span>
    </>
  )

  if (comingLater) {
    return (
      <ComingLaterButton className="location-hero-tile" data-testid={testId}>
        {content}
      </ComingLaterButton>
    )
  }

  return (
    <button
      type="button"
      className={selected ? 'location-hero-tile is-selected' : 'location-hero-tile'}
      data-testid={testId}
      aria-current={selected ? 'true' : undefined}
      disabled={disabled}
      onClick={onClick}
    >
      {content}
    </button>
  )
}
