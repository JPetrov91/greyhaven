const KNOWN = ['CITY_SQUARE', 'TAVERN', 'MARKET', 'OLD_TOWN', 'FOREST', 'NORTH_ROAD'] as const

export type LocationArtCode = (typeof KNOWN)[number]

const ART_ALIAS: Record<string, string> = {
  ARENA: 'city_square',
  CRAFTSMEN_WARD: 'market',
  HARBOUR: 'north_road',
  SEWERS: 'old_town',
  OLD_MINE: 'forest',
  BANDIT_CAMP: 'north_road',
  ANCIENT_RUINS: 'forest',
}

export function locationArtUrl(code: string): string {
  if (KNOWN.includes(code as LocationArtCode)) {
    return `/locations/${code.toLowerCase()}.webp`
  }
  const alias = ART_ALIAS[code]
  if (alias) {
    return `/locations/${alias}.webp`
  }
  return '/locations/city_square.webp'
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
          <circle cx="12" cy="12" r="8" fill="none" stroke="currentColor" strokeWidth="1.6" />
          <path d="m12 6.2 2.4 8.2-2.4-1.4-2.4 1.4Z" fill="currentColor" />
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
          <path d="M12 4.5v13.5" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
          <path d="M8 19.2h8" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
          <path d="M12 7.2 6.5 9.4 8.2 14H12" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round" />
          <path d="M12 7.2 17.5 9.4 15.8 14H12" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round" />
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
          <path d="M7 8h8.5v7.2A3.8 3.8 0 0 1 11.7 19H11A4 4 0 0 1 7 15Z" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round" />
          <path d="M15.5 9.2h2.2A2.4 2.4 0 0 1 20 11.6 2.4 2.4 0 0 1 17.7 14H15.5" fill="none" stroke="currentColor" strokeWidth="1.6" />
          <path d="M9 5.8c.6 1.2.6 2.2 0 3.2M12 5.4c.6 1.2.6 2.4 0 3.6" fill="none" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" />
        </>
      ) : null}
      {name === 'notice' ? (
        <>
          <path d="M7 5.5h10v13H7Z" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round" />
          <path d="M9.2 9h5.6M9.2 12h5.6M9.2 15h3.4" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
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
