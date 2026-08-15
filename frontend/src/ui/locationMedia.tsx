const KNOWN = [
  'CITY_SQUARE',
  'TAVERN',
  'MARKET',
  'OLD_TOWN',
  'FOREST',
  'NORTH_ROAD',
  'ARENA',
  'CRAFTSMEN_WARD',
  'HARBOUR',
  'SEWERS',
  'OLD_MINE',
  'BANDIT_CAMP',
  'ANCIENT_RUINS',
] as const

export type LocationArtCode = (typeof KNOWN)[number]

export function locationArtUrl(code: string): string {
  if (KNOWN.includes(code as LocationArtCode)) {
    return `/locations/${code.toLowerCase()}.webp`
  }
  return '/locations/city_square.webp'
}

const ACTION_ART: Partial<Record<LocationActionIconName, string>> = {
  compass: '/icons/actions/travel.webp',
  arena: '/icons/actions/arena.webp',
  tavern: '/icons/actions/tavern.webp',
  market: '/icons/actions/market.webp',
  notice: '/icons/actions/notice.webp',
  guild: '/icons/actions/guild.webp',
  search: '/icons/actions/search.webp',
  expedition: '/icons/actions/expedition.webp',
  craft: '/icons/actions/craft.webp',
  chat: '/icons/actions/chat.webp',
  globe: '/icons/env/world-map.webp',
  'weather-cloud': '/icons/env/weather-cloud.webp',
  'weather-fog': '/icons/env/weather-fog.webp',
  'weather-wind': '/icons/env/weather-wind.webp',
  'weather-hearth': '/icons/env/weather-hearth.webp',
}

export function locationActionArtUrl(name: LocationActionIconName): string | undefined {
  return ACTION_ART[name]
}

export type LocationWeather = {
  label: string
  temperature: string
  icon: LocationActionIconName
}

const WEATHER_BY_CODE: Record<LocationArtCode, LocationWeather> = {
  CITY_SQUARE: { label: 'Cloudy', temperature: '13°C', icon: 'weather-cloud' },
  TAVERN: { label: 'Hearth-warm', temperature: '21°C', icon: 'weather-hearth' },
  MARKET: { label: 'Overcast', temperature: '12°C', icon: 'weather-cloud' },
  OLD_TOWN: { label: 'Fog', temperature: '9°C', icon: 'weather-fog' },
  FOREST: { label: 'Damp', temperature: '8°C', icon: 'weather-fog' },
  NORTH_ROAD: { label: 'Windy', temperature: '6°C', icon: 'weather-wind' },
  ARENA: { label: 'Dusty', temperature: '14°C', icon: 'weather-wind' },
  CRAFTSMEN_WARD: { label: 'Hearth-warm', temperature: '24°C', icon: 'weather-hearth' },
  HARBOUR: { label: 'Windy', temperature: '7°C', icon: 'weather-wind' },
  SEWERS: { label: 'Damp', temperature: '11°C', icon: 'weather-fog' },
  OLD_MINE: { label: 'Damp', temperature: '6°C', icon: 'weather-fog' },
  BANDIT_CAMP: { label: 'Windy', temperature: '8°C', icon: 'weather-wind' },
  ANCIENT_RUINS: { label: 'Fog', temperature: '5°C', icon: 'weather-fog' },
}

export function locationWeather(code: string): LocationWeather {
  if (KNOWN.includes(code as LocationArtCode)) {
    return WEATHER_BY_CODE[code as LocationArtCode]
  }
  return WEATHER_BY_CODE.CITY_SQUARE
}

export type LocationActionIconName =
  | 'map'
  | 'globe'
  | 'compass'
  | 'search'
  | 'expedition'
  | 'market'
  | 'craft'
  | 'arena'
  | 'chat'
  | 'safe'
  | 'danger'
  | 'spark'
  | 'nopvp'
  | 'pve'
  | 'tavern'
  | 'notice'
  | 'guild'
  | 'weather-cloud'
  | 'weather-fog'
  | 'weather-wind'
  | 'weather-hearth'

type IconProps = {
  name: LocationActionIconName
  className?: string
}

const ICON_TITLES: Record<LocationActionIconName, string> = {
  map: 'Map',
  globe: 'World map',
  compass: 'Travel',
  search: 'Search',
  expedition: 'Expedition',
  market: 'Market',
  craft: 'Crafting',
  arena: 'Arena',
  chat: 'Chat',
  safe: 'Safe',
  danger: 'Dangerous',
  spark: 'Safe zone',
  nopvp: 'No PvP',
  pve: 'PvE',
  tavern: 'Tavern',
  notice: 'Notice board',
  guild: 'Guild hall',
  'weather-cloud': 'Cloudy',
  'weather-fog': 'Fog',
  'weather-wind': 'Wind',
  'weather-hearth': 'Hearth',
}

export function LocationIcon({ name, className = 'location-icon' }: IconProps) {
  return (
    <svg className={className} viewBox="0 0 24 24" aria-hidden="true" focusable="false">
      <title>{ICON_TITLES[name]}</title>
      {name === 'map' ? (
        <>
          <path d="M4.5 6.2 9 4.5l6 2.2 4.5-1.7V18l-4.5 1.7-6-2.2-4.5 1.7Z" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round" />
          <path d="M9 4.5v13.5M15 6.7V20" fill="none" stroke="currentColor" strokeWidth="1.6" />
        </>
      ) : null}
      {name === 'globe' ? (
        <>
          <circle cx="12" cy="12" r="8" fill="none" stroke="currentColor" strokeWidth="1.6" />
          <path d="M4 12h16M12 4c2.4 2.6 3.6 5.4 3.6 8s-1.2 5.4-3.6 8c-2.4-2.6-3.6-5.4-3.6-8S9.6 6.6 12 4Z" fill="none" stroke="currentColor" strokeWidth="1.5" />
        </>
      ) : null}
      {name === 'compass' ? (
        <>
          <circle cx="12" cy="12" r="7.4" fill="none" stroke="currentColor" strokeWidth="1.55" />
          <path d="M12 6.4 14.1 14l-2.1-1.15L9.9 14Z" fill="currentColor" />
        </>
      ) : null}
      {name === 'search' ? (
        <>
          <circle cx="10.5" cy="10.5" r="5.2" fill="none" stroke="currentColor" strokeWidth="1.6" />
          <path d="m14.4 14.4 5 5" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
          <path d="M10.5 8.2v4.6M8.2 10.5h4.6" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
        </>
      ) : null}
      {name === 'expedition' ? (
        <path d="M12 3.5 14.2 9H20l-4.4 3.4L17.6 18 12 14.8 6.4 18l2-5.6L4 9h5.8Z" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round" />
      ) : null}
      {name === 'market' ? (
        <>
          <path d="M12 4.6v13.6" fill="none" stroke="currentColor" strokeWidth="1.55" strokeLinecap="round" />
          <path d="M7.2 19.4h9.6" fill="none" stroke="currentColor" strokeWidth="1.55" strokeLinecap="round" />
          <path d="M5.2 8.2h13.6" fill="none" stroke="currentColor" strokeWidth="1.55" strokeLinecap="round" />
          <path d="M5.2 8.2c0 2.5 1.4 4.1 3.3 4.1S11.8 10.7 11.8 8.2" fill="none" stroke="currentColor" strokeWidth="1.5" />
          <path d="M12.2 8.2c0 2.5 1.4 4.1 3.3 4.1s3.3-1.6 3.3-4.1" fill="none" stroke="currentColor" strokeWidth="1.5" />
        </>
      ) : null}
      {name === 'craft' ? (
        <>
          <path d="M14.6 4.8 19 9.2 10.4 17.8 6 18.8l1-4.4Z" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round" />
          <path d="M13.2 6.2 17.6 10.6" fill="none" stroke="currentColor" strokeWidth="1.5" />
          <path d="M5.2 19.2h7.4" fill="none" stroke="currentColor" strokeWidth="1.55" strokeLinecap="round" />
        </>
      ) : null}
      {name === 'arena' ? (
        <>
          <path d="M8.2 17.6 15.8 6.4" fill="none" stroke="currentColor" strokeWidth="1.55" strokeLinecap="round" />
          <path d="M15.8 17.6 8.2 6.4" fill="none" stroke="currentColor" strokeWidth="1.55" strokeLinecap="round" />
          <path d="M14.4 5.2 17.4 8.2" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
          <path d="M6.6 8.2 9.6 5.2" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
          <path d="M7.4 18.6h2.6M14 18.6h2.6" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
        </>
      ) : null}
      {name === 'chat' ? (
        <>
          <path d="M5 6.5h14v9.2H9.5L5 19.5Z" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round" />
          <path d="M8.5 10h7M8.5 13h4.5" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
        </>
      ) : null}
      {name === 'safe' ? (
        <path d="M12 3.8 19 7v5.2c0 4.2-2.9 6.8-7 8-4.1-1.2-7-3.8-7-8V7Z" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round" />
      ) : null}
      {name === 'danger' ? (
        <>
          <path d="M12 4 21 19.5H3Z" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round" />
          <path d="M12 10v4.2" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" />
          <circle cx="12" cy="16.6" r="0.85" fill="currentColor" />
        </>
      ) : null}
      {name === 'spark' ? (
        <path d="M12 3.2 13.4 9H19l-4.4 3.2L16.2 18 12 14.8 7.8 18l1.6-5.8L5 9h5.6Z" fill="currentColor" />
      ) : null}
      {name === 'nopvp' ? (
        <>
          <path d="M6.2 7.2 10 12.2 7 18.2" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
          <path d="M17.8 7.2 14 12.2 17 18.2" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
          <path d="M5.5 6.5 18.5 17.5" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" />
        </>
      ) : null}
      {name === 'pve' ? (
        <>
          <path d="M7 17.5 12 5.5 17 17.5" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round" />
          <path d="M8.8 13.2h6.4" fill="none" stroke="currentColor" strokeWidth="1.6" />
        </>
      ) : null}
      {name === 'tavern' ? (
        <>
          <path d="M6.6 9h9.2v7.4A3.4 3.4 0 0 1 12.4 19.8H9.8A3.2 3.2 0 0 1 6.6 16.6Z" fill="none" stroke="currentColor" strokeWidth="1.55" strokeLinejoin="round" />
          <path d="M15.8 10.4h2.4A2.2 2.2 0 0 1 20.4 12.6 2.2 2.2 0 0 1 18.2 14.8H15.8" fill="none" stroke="currentColor" strokeWidth="1.55" />
          <path d="M9.2 5.2c.45 1 .45 1.8 0 2.7M12.4 4.9c.5 1.1.5 2 0 3.1" fill="none" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" />
        </>
      ) : null}
      {name === 'notice' ? (
        <>
          <path d="M7.2 4.8h9.6v14.4H7.2Z" fill="none" stroke="currentColor" strokeWidth="1.55" strokeLinejoin="round" />
          <path d="M9.2 8.4h5.6M9.2 11.6h5.6M9.2 14.8h3.6" fill="none" stroke="currentColor" strokeWidth="1.45" strokeLinecap="round" />
        </>
      ) : null}
      {name === 'guild' ? (
        <>
          <path d="M6.5 5.5h11v6.2c0 4.2-2.6 6.6-5.5 7.8-2.9-1.2-5.5-3.6-5.5-7.8Z" fill="#8b2c2c" stroke="#c9a227" strokeWidth="1.2" />
          <path d="M12 8.2 12.7 10h1.8l-1.45 1.05.55 1.75L12 11.85 10.4 12.8l.55-1.75L9.5 10h1.8Z" fill="#f4efe4" />
        </>
      ) : null}
      {name === 'weather-cloud' ? (
        <path d="M8.2 16.5h8.4A3.4 3.4 0 0 0 18.2 10a4.4 4.4 0 0 0-8.3-1.4A3.3 3.3 0 0 0 8.2 16.5Z" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round" />
      ) : null}
      {name === 'weather-fog' ? (
        <>
          <path d="M6 10.5h12M5 13.5h14M7 16.5h10" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
        </>
      ) : null}
      {name === 'weather-wind' ? (
        <>
          <path d="M4 10h11a2.2 2.2 0 1 0-2.2-2.2" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
          <path d="M4 14h13.5a2.2 2.2 0 1 1-2.2 2.2" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
        </>
      ) : null}
      {name === 'weather-hearth' ? (
        <>
          <path d="M12 6.2c2.6 2.8 4.2 5 4.2 7.2A4.2 4.2 0 0 1 12 17.6 4.2 4.2 0 0 1 7.8 13.4c0-2.2 1.6-4.4 4.2-7.2Z" fill="none" stroke="currentColor" strokeWidth="1.6" />
          <path d="M9.5 19.2h5" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
        </>
      ) : null}
    </svg>
  )
}

export function LocationCrest({ className = 'location-crest' }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 48 56" aria-hidden="true" focusable="false">
      <title>Greyhaven crest</title>
      <path
        d="M8 6h32v22.5c0 12-7.2 18.8-16 22.5C15.2 47.3 8 40.5 8 28.5Z"
        fill="#8b2c2c"
        stroke="#c9a227"
        strokeWidth="1.6"
      />
      <path
        d="M24 14 26.2 19.6h5.8l-4.7 3.4 1.8 5.6L24 25.4 19 28.6l1.8-5.6-4.7-3.4h5.8Z"
        fill="#f4efe4"
      />
    </svg>
  )
}
