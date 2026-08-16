import type { ReactNode } from 'react'
import type { DestinationResponse, LocationResponse } from '../../api/types'
import { Button } from '../../ui/Button'
import { StatusBadge } from '../../ui/StatusBadge'
import {
  LocationCrest,
  LocationIcon,
  locationActionArtUrl,
  locationArtUrl,
  locationWeather,
  type LocationActionIconName,
} from '../../ui/locationMedia'
import { MAIN_SHELL_VISUAL_CLOCK, mainShellDestinations, mainShellLocation } from '../mainShellVisualFixture'

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
}: {
  location: LocationResponse
  destinations: DestinationResponse[]
}): HeroTileModel[] {
  const actions = new Set(location.actions)
  const tavernDestination = destinations.find((destination) => destination.code === 'TAVERN')
  const atTavern = location.code === 'TAVERN'
  const tiles: HeroTileModel[] = [
    {
      testId: 'hero-travel',
      icon: 'compass',
      title: 'Travel',
      subtitle: 'Change location',
    },
  ]

  if (actions.has('SEARCH_ENCOUNTER')) {
    tiles.push({
      testId: 'search-encounter-button',
      icon: 'search',
      title: 'Search',
      subtitle: 'Hunt nearby',
    })
  }

  if (actions.has('ENTER_ARENA')) {
    tiles.push({
      testId: 'enter-arena-action',
      icon: 'arena',
      title: 'Arena',
      subtitle: 'Challenge defenders',
    })
  }

  if (actions.has('START_EXPEDITION') || actions.has('INSPECT_EXPEDITIONS')) {
    tiles.push({
      testId: 'start-expedition-action',
      icon: 'expedition',
      title: 'Expeditions',
      subtitle: 'Send a party',
    })
  }

  if (actions.has('CRAFT') || actions.has('CLAIM_CRAFT') || actions.has('SALVAGE')) {
    tiles.push({
      testId: 'open-crafting-action',
      icon: 'craft',
      title: 'Crafting',
      subtitle: 'Jobs & salvage',
    })
  }

  if (actions.has('BROWSE_MARKET') || actions.has('CREATE_LISTING') || actions.has('BUY_ITEM')) {
    tiles.push({
      testId: 'open-market-BROWSE_MARKET',
      icon: 'market',
      title: 'Local Market',
      subtitle: 'Buy & sell',
    })
  }

  if (actions.has('VIEW_CHAT')) {
    tiles.push({
      testId: 'open-chat-action',
      icon: 'chat',
      title: 'Chat',
      subtitle: 'Talk here',
    })
  }

  if (atTavern) {
    tiles.push({
      testId: 'hero-tavern',
      icon: 'tavern',
      title: 'Tavern',
      subtitle: 'Find players',
    })
  } else if (tavernDestination) {
    tiles.push({
      testId: 'hero-tavern',
      icon: 'tavern',
      title: 'Tavern',
      subtitle: 'Find players',
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

function HeroTile({ testId, icon, title, subtitle, disabled, comingLater }: HeroTileModel) {
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
        <strong className="type-item">{title}</strong>
        <span className="type-meta">{subtitle}</span>
      </span>
    </>
  )

  return (
    <Button
      type="button"
      variant="secondary"
      className="location-hero-tile"
      data-testid={testId}
      disabled={disabled || comingLater}
    >
      {content}
    </Button>
  )
}

/** Visual copy of production LocationHero. Fixture data only. New UI Engine finish. */
export function MainShellLocationPanel() {
  const location = mainShellLocation
  const destinations = mainShellDestinations
  const weather = locationWeather(location.code)
  const safe = location.safety === 'SAFE'
  const tiles = heroActionTiles({ location, destinations })

  return (
    <div className="location-hero surface-base surface-frame" data-testid="location-panel">
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
              <p className="location-hero-kicker type-micro">Current location</p>
              <h2 className="location-hero-region type-page-heading">{location.region}</h2>
              <p className="location-hero-place type-item" data-testid="current-location">
                {location.name}
              </p>
            </div>
          </div>
          <div className="location-hero-env">
            <div className="location-hero-env-card surface-raised">
              <div className="location-hero-clock" data-testid="location-clock">
                <strong className="type-numeric type-numeric-gold">{MAIN_SHELL_VISUAL_CLOCK}</strong>
                <span className="type-micro">Greyhaven time</span>
              </div>
              <p className="location-hero-weather" data-testid="location-weather">
                {locationActionArtUrl(weather.icon) ? (
                  <img className="location-hero-env-art" src={locationActionArtUrl(weather.icon)} alt="" />
                ) : (
                  <LocationIcon name={weather.icon} />
                )}
                <span className="location-hero-weather-label type-compact">{weather.label}</span>
                <span className="location-hero-temp type-numeric">{weather.temperature}</span>
              </p>
              <Button type="button" variant="secondary" className="location-hero-map" data-testid="hero-world-map">
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

        <p className="location-description type-body" data-testid="location-description">
          {location.description}
        </p>
        {location.recommendedLevelMin != null && location.recommendedLevelMax != null ? (
          <p className="type-meta" data-testid="location-band">
            Recommended levels {location.recommendedLevelMin}–{location.recommendedLevelMax}
          </p>
        ) : null}
        <p className="location-hero-pills">
          <StatusBadge tone={safe ? 'safe' : 'danger'} data-testid="location-safety">
            <LocationIcon name={safe ? 'spark' : 'danger'} />
            {safe ? 'Safe Zone' : 'Dangerous'}
          </StatusBadge>
          <StatusBadge tone="neutral" data-testid="location-pvp">
            <LocationIcon name={safe ? 'nopvp' : 'pve'} />
            {safe ? 'No PvP' : 'PvE'}
          </StatusBadge>
        </p>

        <nav className="location-hero-actions" aria-label="Location actions">
          {tiles.map((tile) => (
            <HeroTile key={tile.testId} {...tile} />
          ))}
        </nav>
      </div>
    </div>
  )
}
