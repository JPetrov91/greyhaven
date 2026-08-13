import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import {
  fetchCurrentLocation,
  fetchDestinations,
  fetchNearbyCharacters,
  moveToLocation,
} from '../api/world'
import type { LocationAction } from '../api/types'

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
}

/** Actions already represented by dedicated UI sections on this screen. */
const IMPLIED_ACTIONS = new Set<LocationAction>(['INSPECT', 'MOVE', 'VIEW_NEARBY'])

type Props = {
  onSearchEncounter?: () => void
  searchBusy?: boolean
  searchError?: string | null
  onOpenExpedition?: () => void
  onOpenMarket?: () => void
  onOpenChat?: () => void
}

export function LocationPanel({
  onSearchEncounter,
  searchBusy = false,
  searchError = null,
  onOpenExpedition,
  onOpenMarket,
  onOpenChat,
}: Props) {
  const queryClient = useQueryClient()
  const [moveError, setMoveError] = useState<string | null>(null)
  const [movingToId, setMovingToId] = useState<string | null>(null)

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
        <p className="muted">Loading location…</p>
      </div>
    )
  }

  if (locationQuery.error instanceof ApiError) {
    return (
      <div className="game-column game-column-center" data-testid="location-panel">
        <h2>Greyhaven</h2>
        <p className="form-error" role="alert">
          {locationQuery.error.message}
        </p>
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

  return (
    <div className="game-column game-column-center" data-testid="location-panel">
      <p className="location-region muted">{location.region}</p>
      <h2 data-testid="current-location">{location.name}</h2>
      <p className="location-meta">
        <span data-testid="location-safety" className={location.safety === 'SAFE' ? 'safety-safe' : 'safety-danger'}>
          {location.safety === 'SAFE' ? 'Safe' : 'Dangerous'}
        </span>
        <span className="muted" data-testid="location-code">
          {location.code}
        </span>
      </p>
      <p className="location-description" data-testid="location-description">
        {location.description}
      </p>

      <section className="location-section" aria-labelledby="destinations-heading">
        <h3 id="destinations-heading">Travel</h3>
        {destinationsQuery.isLoading ? (
          <p className="muted">Loading destinations…</p>
        ) : destinations.length === 0 ? (
          <p className="muted">No connected destinations.</p>
        ) : (
          <ul className="destination-list" data-testid="destination-list">
            {destinations.map((destination) => (
              <li key={destination.id}>
                <div>
                  <strong data-testid={`destination-name-${destination.code}`}>{destination.name}</strong>
                  <span className="muted">
                    {' '}
                    · {destination.safety === 'SAFE' ? 'Safe' : 'Dangerous'}
                  </span>
                </div>
                <button
                  type="button"
                  className="travel-button"
                  data-testid={`destination-${destination.code}`}
                  disabled={movingToId !== null}
                  onClick={() => void handleMove(destination.id)}
                >
                  {movingToId === destination.id ? 'Traveling…' : 'Travel'}
                </button>
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

      <section className="location-section" aria-labelledby="actions-heading">
        <h3 id="actions-heading">Available actions</h3>
        <ul className="action-list" data-testid="location-actions">
          {location.actions
            .filter((action) => IMPLIED_ACTIONS.has(action))
            .map((action) => (
              <li key={action} data-testid={`action-${action}`}>
                <span>{ACTION_LABELS[action]}</span>
                <span className="action-status available">Available</span>
              </li>
            ))}
          {listedActions.map((action) => (
            <li key={action} data-testid={`action-${action}`}>
              <span>{ACTION_LABELS[action]}</span>
              {action === 'SEARCH_ENCOUNTER' ? (
                <button
                  type="button"
                  className="travel-button"
                  data-testid="search-encounter-button"
                  disabled={searchBusy || !onSearchEncounter}
                  onClick={() => onSearchEncounter?.()}
                >
                  {searchBusy ? 'Searching…' : 'Search'}
                </button>
              ) : action === 'START_EXPEDITION' || action === 'INSPECT_EXPEDITIONS' ? (
                <button
                  type="button"
                  className="travel-button"
                  data-testid={
                    action === 'START_EXPEDITION' ? 'start-expedition-action' : 'inspect-expedition-action'
                  }
                  disabled={!onOpenExpedition}
                  onClick={() => onOpenExpedition?.()}
                >
                  Open
                </button>
              ) : action === 'BROWSE_MARKET' ||
                action === 'CREATE_LISTING' ||
                action === 'BUY_ITEM' ||
                action === 'CANCEL_LISTING' ? (
                <button
                  type="button"
                  className="travel-button"
                  data-testid={`open-market-${action}`}
                  disabled={!onOpenMarket}
                  onClick={() => onOpenMarket?.()}
                >
                  Open
                </button>
              ) : action === 'VIEW_CHAT' ? (
                <button
                  type="button"
                  className="travel-button"
                  data-testid="open-chat-action"
                  disabled={!onOpenChat}
                  onClick={() => onOpenChat?.()}
                >
                  Show
                </button>
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

      <section className="location-section" aria-labelledby="nearby-heading">
        <h3 id="nearby-heading">Nearby characters</h3>
        {nearbyQuery.isLoading ? (
          <p className="muted">Looking around…</p>
        ) : nearby.length === 0 ? (
          <p className="muted" data-testid="nearby-empty">
            No other characters are here.
          </p>
        ) : (
          <>
            <ul className="nearby-list" data-testid="nearby-characters">
              {nearby.map((character) => (
                <li key={character.id} data-testid={`nearby-${character.name}`}>
                  <strong>{character.name}</strong>
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
    </div>
  )
}
