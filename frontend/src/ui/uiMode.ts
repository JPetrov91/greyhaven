export const UI_MODE_STORAGE_KEY = 'greyhaven.uiMode'

export type UiMode = 'normal' | 'compact'

export function readStoredUiMode(): UiMode {
  try {
    return localStorage.getItem(UI_MODE_STORAGE_KEY) === 'compact' ? 'compact' : 'normal'
  } catch {
    return 'normal'
  }
}

export function persistUiMode(mode: UiMode): void {
  try {
    localStorage.setItem(UI_MODE_STORAGE_KEY, mode)
  } catch {
    // Ignore quota / private-mode failures; the in-memory mode still applies.
  }
}

export function applyUiMode(mode: UiMode): void {
  document.documentElement.dataset.uiMode = mode
}
