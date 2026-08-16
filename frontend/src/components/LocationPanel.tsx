import { useEffect, useState, type ReactNode } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import {
  fetchCurrentLocation,
  fetchDestinations,
  fetchNearbyCharacters,
  moveToLocation,
} from '../api/world'
import { fetchPublicCharacter, type PublicCharacterResponse } from '../api/pvp'
import type { DestinationResponse, LocationAction, LocationResponse } from '../api/types'
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
} from '../ui/locationMedia'
import type { LocationActionIconName } from '../ui/locationMedia'
import { DungeonPanel } from './DungeonPanel'
import { NpcDialogue } from './NpcDialogue'

const ACTION_LABELS: Record<LocationAction, string> = {
  INSPECT: 'Inspect location',
  MOVE: 'Travel',
  VIEW_NEARBY: 'View nearby characters',
  VIEW_CHAT: 'Global chat',
  START_EXPEDITION: 'Start expedition',
  INSPECT_EXPEDITIONS: 'Inspect expeditions',
  BROWSE_MARKET: 'Browse market listings',
  CREATE_LISTING: 'Create listing',
  BUY_ITEM: 'Buy item',
  CANCEL_LISTING: 'Cancel own listing',
  SEARCH_ENCOUNTER: 'Search for encounter',
  ENTER_DUNGEON: 'Enter dungeon',
  ENTER_ARENA: 'Enter Arena',
  CHALLENGE_DUEL: 'Challenge to a duel',
  CRAFT: 'Craft',
  CLAIM_CRAFT: 'Claim finished craft',
  SALVAGE: 'Salvage equipment',
  CREATE_BUY_ORDER: 'Create buy order',
  FULFILL_BUY_ORDER: 'Fulfill buy order',
  TALK_NPCS: 'Talk to people',
}

/** Actions already represented by dedicated UI sections on this screen. */
const IMPLIED_ACTIONS = new Set<LocationAction>([
  'INSPECT',
  'MOVE',
  'VIEW_NEARBY',
  'ENTER_DUNGEON',
  'ENTER_ARENA',
  'CHALLENGE_DUEL',
  'CREATE_BUY_ORDER',
  'FULFILL_BUY_ORDER',
])

type Props = {
  onSearchEncounter?: () => void
  searchBusy?: boolean
  searchError?: string | null
  onOpenExpedition?: () => void
  onOpenMarket?: () => void
  onOpenChat?: () => void
  onOpenWorld?: () => void
  onOpenArena?: () => void
  onOpenCrafting?: () => void
  variant?: 'full' | 'hero'
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
  onOpenArena,
  onOpenCrafting,
  variant = 'full',
}: Props) {
  const queryClient = useQueryClient()
  const [moveError, setMoveError] = useState<string | null>(null)
  const [movingToId, setMovingToId] = useState<string | null>(null)
  const [inspected, setInspected] = useState<PublicCharacterResponse | null>(null)
  const [talkOpen, setTalkOpen] = useState(false)

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

  const listedActions = location.actions.filter((action) => !IMPLIED_ACTIONS.has(action))
  const destinations = destinationsQuery.data?.destinations ?? []
  const nearby = nearbyQuery.data?.characters ?? []
  const nearbyTruncated = nearbyQuery.data?.truncated ?? false

  async function inspectNearby(id: string) {
    try {
      setInspected(await fetchPublicCharacter(id))
    } catch {
      setInspected(null)
    }
  }

  const hero = variant === 'hero'
  const wrapperClass = hero ? 'game-column location-hero' : 'game-column game-column-center'

  return (
    <div className={wrapperClass} data-testid="location-panel" id={hero ? undefined : 'world'}>
      {hero ? (
        <LocationHero
          location={location}
          destinations={destinations}
          movingToId={movingToId}
          moveError={moveError}
          searchBusy={searchBusy}
          searchError={searchError}
          onSearchEncounter={onSearchEncounter}
          onOpenWorld={onOpenWorld}
          onOpenMarket={onOpenMarket}
          onOpenChat={onOpenChat}
          onOpenExpedition={onOpenExpedition}
          onOpenArena={onOpenArena}
          onOpenCrafting={onOpenCrafting}
          onMove={(id) => void handleMove(id)}
        />
      ) : (
        <>
          <div className="location-hero location-page-banner">
            <div
              className="location-hero-art"
              aria-hidden="true"
              style={{ backgroundImage: `url(${locationArtUrl(location.code)})` }}
            />
            <div className="location-hero-body">
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
                    location.safety === 'SAFE'
                      ? 'location-hero-pill location-hero-pill-safe'
                      : 'location-hero-pill location-hero-pill-danger'
                  }
                  data-testid="location-safety"
                >
                  <LocationIcon name={location.safety === 'SAFE' ? 'spark' : 'danger'} />
                  {location.safety === 'SAFE' ? 'Safe Zone' : 'Dangerous'}
                </span>
                <span className="location-hero-pill location-hero-pill-muted" data-testid="location-pvp">
                  <LocationIcon name={location.safety === 'SAFE' ? 'nopvp' : 'pve'} />
                  {location.safety === 'SAFE' ? 'No PvP' : 'PvE'}
                </span>
                <span className="muted" data-testid="location-code">
                  {location.code}
                </span>
              </p>
            </div>
          </div>
          {location.actions.includes('ENTER_DUNGEON') ? <DungeonPanel atEntrance /> : null}
          {location.actions.includes('ENTER_ARENA') ? (
            <Button type="button" data-testid="enter-arena-action" onClick={() => onOpenArena?.()}>
              Open Arena
            </Button>
          ) : null}

          <section className="location-section" aria-labelledby="destinations-heading">
            <h3 id="destinations-heading">Travel</h3>
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
          </section>

          {listedActions.length > 0 ? (
            <section className="location-section" aria-labelledby="actions-heading">
              <h3 id="actions-heading">Available actions</h3>
              <ul className="action-list" data-testid="location-actions">
                {listedActions.map((action) => (
                  <li key={action} data-testid={`action-${action}`}>
                    <span>{ACTION_LABELS[action]}</span>
                    {action === 'SEARCH_ENCOUNTER' ? (
                      <Button
                        type="button"
                        data-testid="search-encounter-button"
                        disabled={searchBusy || !onSearchEncounter}
                        onClick={() => onSearchEncounter?.()}
                      >
                        {searchBusy ? 'Searching…' : 'Search'}
                      </Button>
                    ) : action === 'START_EXPEDITION' || action === 'INSPECT_EXPEDITIONS' ? (
                      <Button
                        type="button"
                        data-testid={
                          action === 'START_EXPEDITION' ? 'start-expedition-action' : 'inspect-expedition-action'
                        }
                        disabled={!onOpenExpedition}
                        onClick={() => onOpenExpedition?.()}
                      >
                        Open
                      </Button>
                    ) : action === 'BROWSE_MARKET' ||
                      action === 'CREATE_LISTING' ||
                      action === 'BUY_ITEM' ||
                      action === 'CANCEL_LISTING' ||
                      action === 'CREATE_BUY_ORDER' ||
                      action === 'FULFILL_BUY_ORDER' ? (
                      <Button
                        type="button"
                        data-testid={`open-market-${action}`}
                        disabled={!onOpenMarket}
                        onClick={() => onOpenMarket?.()}
                      >
                        Open
                      </Button>
                    ) : action === 'CRAFT' || action === 'CLAIM_CRAFT' || action === 'SALVAGE' ? (
                      <Button
                        type="button"
                        data-testid={action === 'CRAFT' ? 'open-crafting-action' : `open-crafting-${action}`}
                        disabled={!onOpenCrafting}
                        onClick={() => onOpenCrafting?.()}
                      >
                        Open
                      </Button>
                    ) : action === 'TALK_NPCS' ? (
                      <Button type="button" data-testid="talk-npcs-action" onClick={() => setTalkOpen(true)}>
                        Talk
                      </Button>
                    ) : action === 'VIEW_CHAT' ? (
                      <Button
                        type="button"
                        data-testid="open-chat-action"
                        disabled={!onOpenChat}
                        onClick={() => onOpenChat?.()}
                      >
                        Show
                      </Button>
                    ) : (
                      <span className="action-status available">Available</span>
                    )}
                  </li>
                ))}
              </ul>
              {searchError ? (
                <p className="form-error" role="alert" data-testid="search-encounter-error">
                  {searchError}
                </p>
              ) : null}
            </section>
          ) : null}

          <section className="location-section" aria-labelledby="nearby-heading">
            <h3 id="nearby-heading">Nearby characters</h3>
            {nearbyQuery.isLoading ? (
              <LoadingState>Looking around…</LoadingState>
            ) : nearby.length === 0 ? (
              <EmptyState testId="nearby-empty">No other characters are here.</EmptyState>
            ) : (
              <>
                <ul className="nearby-list" data-testid="nearby-characters">
                  {nearby.map((character) => (
                    <li key={character.id} data-testid={`nearby-${character.name}`}>
                      <button type="button" onClick={() => void inspectNearby(character.id)}>
                        <strong>{character.name}</strong>
                      </button>
                      <span className="muted">Level {character.level}</span>
                    </li>
                  ))}
                </ul>
                {nearbyTruncated ? (
                  <p className="muted" data-testid="nearby-truncated">
                    Showing the first {nearby.length} characters here.
                  </p>
                ) : null}
              </>
            )}
          </section>
          {inspected ? (
            <aside data-testid="public-inspect">
              <h3>{inspected.name}</h3>
              <p>
                Level {inspected.level} · Rating {inspected.arenaRating}
              </p>
              <p>
                STR {inspected.strength} AGI {inspected.agility} END {inspected.endurance} PER{' '}
                {inspected.perception}
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
          <NpcDialogue open={talkOpen} onClose={() => setTalkOpen(false)} onOpenMarket={onOpenMarket} />
        </>
      )}
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
  onOpenMarket?: () => void
  onOpenChat?: () => void
  onOpenExpedition?: () => void
  onOpenArena?: () => void
  onOpenCrafting?: () => void
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
}

function heroActionTiles({
  location,
  destinations,
  movingToId,
  searchBusy,
  onSearchEncounter,
  onOpenWorld,
  onOpenMarket,
  onOpenChat,
  onOpenExpedition,
  onOpenArena,
  onOpenCrafting,
  onMove,
}: Pick<
  LocationHeroProps,
  | 'location'
  | 'destinations'
  | 'movingToId'
  | 'searchBusy'
  | 'onSearchEncounter'
  | 'onOpenWorld'
  | 'onOpenMarket'
  | 'onOpenChat'
  | 'onOpenExpedition'
  | 'onOpenArena'
  | 'onOpenCrafting'
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
      onClick: () => onOpenWorld?.(),
    },
  ]

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
    tiles.push({
      testId: 'hero-notice',
      icon: 'notice',
      title: 'Notice Board',
      subtitle: 'Quests & tasks',
      comingLater: true,
    })
    tiles.push({
      testId: 'hero-guild',
      icon: 'guild',
      title: 'Guild Hall',
      subtitle: 'Guild activities',
      comingLater: true,
    })
  }

  return tiles.slice(0, 5)
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
  onOpenMarket,
  onOpenChat,
  onOpenExpedition,
  onOpenArena,
  onOpenCrafting,
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
    onOpenMarket,
    onOpenChat,
    onOpenExpedition,
    onOpenArena,
    onOpenCrafting,
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
}

function HeroTile({ testId, icon, title, subtitle, onClick, disabled, comingLater }: TileProps) {
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
    <button type="button" className="location-hero-tile" data-testid={testId} disabled={disabled} onClick={onClick}>
      {content}
    </button>
  )
}
