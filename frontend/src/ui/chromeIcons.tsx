export type ChromeIconName =
  | 'trophy'
  | 'mail'
  | 'pack'
  | 'friends'
  | 'store'
  | 'settings'
  | 'menu'
  | 'home'
  | 'locations'
  | 'character'
  | 'equipment'
  | 'market'
  | 'expeditions'
  | 'mastery'
  | 'crafting'
  | 'pvp'
  | 'guild'
  | 'rankings'
  | 'travel'
  | 'tavern'
  | 'daily'
  | 'collapse'
  | 'online'

const STROKE = {
  fill: 'none' as const,
  stroke: 'currentColor',
  strokeWidth: 1.6,
  strokeLinejoin: 'round' as const,
}

export function ChromeIcon({ name, className = 'chrome-icon' }: { name: ChromeIconName; className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" aria-hidden="true" focusable="false">
      {glyph(name)}
    </svg>
  )
}

function glyph(name: ChromeIconName) {
  switch (name) {
    case 'trophy':
      return (
        <path
          d="M7 5h10v3a5 5 0 0 1-4 4.9V15h3v2H8v-2h3v-2.1A5 5 0 0 1 7 8V5Zm-2 1h2v2a3 3 0 0 1-2-2.6V6Zm12 0h2v.4A3 3 0 0 1 17 8V6Z"
          {...STROKE}
        />
      )
    case 'mail':
      return <path d="M4 7h16v10H4Zm0 0 8 6 8-6" {...STROKE} />
    case 'pack':
      return <path d="M8 8V6.5A4 4 0 0 1 16 6.5V8h2.5v12H5.5V8Zm2 0h4V6.8a2 2 0 0 0-4 0Z" {...STROKE} />
    case 'friends':
      return (
        <>
          <circle cx="9" cy="8" r="2.4" {...STROKE} />
          <path d="M4.5 18c.6-3 2.4-4.5 4.5-4.5S13 15 13.6 18" {...STROKE} strokeLinecap="round" />
          <circle cx="16.2" cy="8.4" r="2" {...STROKE} />
          <path d="M15 13.6c1.7.2 3.2 1.4 3.8 4.4" {...STROKE} strokeLinecap="round" />
        </>
      )
    case 'store':
      return (
        <>
          <path d="M5 10h14v9H5Z" {...STROKE} />
          <path d="M4.5 7.5 7 4.8h10L19.5 7.5H4.5Z" {...STROKE} />
          <path d="M9 13h6" {...STROKE} strokeLinecap="round" />
        </>
      )
    case 'settings':
      return (
        <>
          <circle cx="12" cy="12" r="2.4" {...STROKE} />
          <path
            d="M12 4.6 13.2 7l2.6.2 1.3 2.3 2.2 1.4-.8 2.5.8 2.5-2.2 1.4-1.3 2.3-2.6.2L12 19.4 10.8 17l-2.6-.2-1.3-2.3-2.2-1.4.8-2.5-.8-2.5 2.2-1.4 1.3-2.3 2.6-.2Z"
            {...STROKE}
          />
        </>
      )
    case 'menu':
      return (
        <>
          <path d="M5 7h14" {...STROKE} strokeLinecap="round" />
          <path d="M5 12h14" {...STROKE} strokeLinecap="round" />
          <path d="M5 17h14" {...STROKE} strokeLinecap="round" />
        </>
      )
    case 'home':
      return <path d="M4.5 11 12 4.5 19.5 11v8.5H14v-5H10v5H4.5Z" {...STROKE} />
    case 'locations':
      return (
        <>
          <path d="M12 20s6.5-6.2 6.5-10.2A6.5 6.5 0 0 0 5.5 9.8C5.5 13.8 12 20 12 20Z" {...STROKE} />
          <circle cx="12" cy="9.8" r="2.1" {...STROKE} />
        </>
      )
    case 'character':
      return (
        <>
          <circle cx="12" cy="8" r="2.6" {...STROKE} />
          <path d="M6.2 19c.8-3.6 3-5.4 5.8-5.4s5 1.8 5.8 5.4" {...STROKE} strokeLinecap="round" />
        </>
      )
    case 'equipment':
      return (
        <>
          <path d="M7 6.5 12 4.8 17 6.5v5.2c0 4.4-2.2 6.8-5 7.8-2.8-1-5-3.4-5-7.8Z" {...STROKE} />
          <path d="M12 8.2v7.4" {...STROKE} strokeLinecap="round" />
        </>
      )
    case 'market':
      return (
        <>
          <path d="M4.5 10.2 7 6.8h10l2.5 3.4v8.8H4.5Z" {...STROKE} />
          <path d="M9 14.2h6" {...STROKE} strokeLinecap="round" />
          <path d="M8 6.8v3.4M16 6.8v3.4" {...STROKE} />
        </>
      )
    case 'expeditions':
      return (
        <>
          <circle cx="12" cy="12" r="7.2" {...STROKE} />
          <path d="M12 6.2v2.2M12 15.6v2.2M6.2 12h2.2M15.6 12h2.2" {...STROKE} strokeLinecap="round" />
          <path d="M12 12 15.2 9.2" {...STROKE} strokeLinecap="round" />
        </>
      )
    case 'mastery':
      return (
        <>
          <path d="M6.5 16.5 12 6.2l5.5 10.3" {...STROKE} />
          <path d="M8.4 13.2h7.2" {...STROKE} strokeLinecap="round" />
        </>
      )
    case 'crafting':
      return (
        <>
          <path d="M5 16.8h14v2.4H5Z" {...STROKE} />
          <path d="M8 16.8V9.2h8v7.6" {...STROKE} />
          <path d="M7 9.2h10L15.4 6H8.6Z" {...STROKE} />
        </>
      )
    case 'pvp':
      return (
        <>
          <path d="M7.2 4.8 10 7.6v9.6L7.2 20" {...STROKE} />
          <path d="M16.8 4.8 14 7.6v9.6L16.8 20" {...STROKE} />
          <path d="M5.6 9.2h4.2M14.2 9.2h4.2" {...STROKE} strokeLinecap="round" />
        </>
      )
    case 'guild':
      return (
        <>
          <path d="M6.5 19.2V6.6L12 4.8l5.5 1.8v12.6L12 16.8Z" {...STROKE} />
          <path d="M12 7.4v9" {...STROKE} strokeLinecap="round" />
        </>
      )
    case 'rankings':
      return (
        <>
          <path d="M4.8 16.8h4.2V11H4.8Zm5.1 0h4.2V6.6H9.9Zm5.1 0h4.2v-8h-4.2Z" {...STROKE} />
        </>
      )
    case 'travel':
      return <path d="M7.2 19.4 17.6 12 7.2 4.6v5.4H4.6v3.8h2.6Z" {...STROKE} />
    case 'tavern':
      return (
        <>
          <path d="M7 6.4h8.4v8.2A4.2 4.2 0 0 1 11.2 18.8 4.2 4.2 0 0 1 7 14.6Z" {...STROKE} />
          <path d="M15.4 8.2h2.4a3 3 0 0 1 0 6h-2.2" {...STROKE} />
        </>
      )
    case 'daily':
      return (
        <>
          <circle cx="12" cy="12" r="3.2" {...STROKE} />
          <path
            d="M12 5.2v1.6M12 17.2v1.6M5.2 12h1.6M17.2 12h1.6M7.2 7.2l1.1 1.1M15.7 15.7l1.1 1.1M7.2 16.8l1.1-1.1M15.7 8.3l1.1-1.1"
            {...STROKE}
            strokeLinecap="round"
          />
        </>
      )
    case 'collapse':
      return (
        <>
          <path d="M14.6 6.4 8.8 12l5.8 5.6" {...STROKE} strokeLinecap="round" />
          <path d="M18.4 6.4 12.6 12l5.8 5.6" {...STROKE} strokeLinecap="round" />
        </>
      )
    case 'online':
      return <circle cx="12" cy="12" r="4.2" fill="currentColor" />
    default:
      return null
  }
}
