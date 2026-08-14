export type GameNavItem =
  | 'home'
  | 'world'
  | 'character'
  | 'inventory'
  | 'equipment'
  | 'mastery'
  | 'market'
  | 'expeditions'

export type GameLocation = { pathname: string; search: string; hash: string }

export function gameViewFromLocation(location: GameLocation): GameNavItem {
  if (!location.pathname.startsWith('/game')) {
    return 'home'
  }
  if (new URLSearchParams(location.search).get('panel') === 'market') {
    return 'market'
  }
  const hash = location.hash.replace(/^#/, '')
  if (hash === 'character') {
    return 'character'
  }
  if (hash === 'inventory' || hash === 'equipment') {
    return hash === 'equipment' ? 'equipment' : 'inventory'
  }
  if (hash === 'mastery') {
    return 'mastery'
  }
  if (hash === 'world') {
    return 'world'
  }
  if (hash === 'expeditions') {
    return 'expeditions'
  }
  return 'home'
}

export function gameLink(item: GameNavItem): { pathname: '/game'; search: string; hash: string } {
  if (item === 'market') {
    return { pathname: '/game', search: '?panel=market', hash: '' }
  }
  if (item === 'home') {
    return { pathname: '/game', search: '', hash: '' }
  }
  const hash =
    item === 'equipment' ? 'inventory' : item === 'world' ? 'world' : item === 'expeditions' ? 'expeditions' : item
  return { pathname: '/game', search: '', hash }
}

export function isGameNavActive(item: GameNavItem, location: GameLocation): boolean {
  if (!location.pathname.startsWith('/game')) {
    return false
  }
  const view = gameViewFromLocation(location)
  if (item === 'inventory' || item === 'equipment') {
    return view === 'inventory' || view === 'equipment'
  }
  return view === item
}
