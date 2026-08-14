export const NAV_COLLAPSE_STORAGE_KEY = 'greyhaven.navCollapsed'

export function readStoredNavCollapsed(): boolean {
  try {
    return localStorage.getItem(NAV_COLLAPSE_STORAGE_KEY) === 'true'
  } catch {
    return false
  }
}

export function persistNavCollapsed(collapsed: boolean): void {
  try {
    if (collapsed) {
      localStorage.setItem(NAV_COLLAPSE_STORAGE_KEY, 'true')
    } else {
      localStorage.removeItem(NAV_COLLAPSE_STORAGE_KEY)
    }
  } catch {
    // Ignore quota / private-mode failures; the in-memory flag still applies.
  }
}
