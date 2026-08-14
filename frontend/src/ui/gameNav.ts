export type GameNavItem = 'character' | 'world' | 'inventory' | 'market'

export function isGameNavActive(
  item: GameNavItem,
  location: { pathname: string; search: string; hash: string },
): boolean {
  if (!location.pathname.startsWith('/game')) {
    return false
  }
  const market = new URLSearchParams(location.search).get('panel') === 'market'
  const hash = location.hash.replace(/^#/, '')
  if (item === 'market') {
    return market
  }
  if (market) {
    return false
  }
  if (item === 'inventory') {
    return hash === 'inventory'
  }
  if (item === 'character') {
    return hash === 'character'
  }
  return hash !== 'inventory' && hash !== 'character'
}
