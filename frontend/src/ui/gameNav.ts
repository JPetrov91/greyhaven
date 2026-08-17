export type GameNavItem =
  | 'home'
  | 'world'
  | 'quests'
  | 'character'
  | 'inventory'
  | 'equipment'
  | 'mastery'
  | 'market'
  | 'expeditions'
  | 'crafting'
  | 'pvp'
  | 'sparring'

export type GameLocation = { pathname: string; search: string; hash: string }

export function gameViewFromLocation(location: GameLocation): GameNavItem {
  if (!location.pathname.startsWith('/game')) {
    return 'home'
  }
  if (new URLSearchParams(location.search).get('panel') === 'market') {
    return 'market'
  }
  const hash = location.hash.replace(/^#/, '') as GameNavItem
  if (
    hash === 'character' ||
    hash === 'inventory' ||
    hash === 'equipment' ||
    hash === 'mastery' ||
    hash === 'world' ||
    hash === 'quests' ||
    hash === 'expeditions' ||
    hash === 'crafting' ||
    hash === 'pvp'
  ) {
    return hash
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
  return { pathname: '/game', search: '', hash: item }
}

export function gameTravelLink(): { pathname: '/game'; search: string; hash: string } {
  return { pathname: '/game', search: '?travel=1', hash: 'world' }
}

export function isTravelSheetOpen(location: GameLocation): boolean {
  return gameViewFromLocation(location) === 'world' && new URLSearchParams(location.search).get('travel') === '1'
}

export function isGameNavActive(item: GameNavItem, location: GameLocation): boolean {
  if (!location.pathname.startsWith('/game')) {
    return false
  }
  return gameViewFromLocation(location) === item
}
