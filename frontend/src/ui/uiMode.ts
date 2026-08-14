import { useEffect, useState } from 'react'

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

export function readAppliedUiMode(): UiMode {
  return document.documentElement.dataset.uiMode === 'compact' ? 'compact' : 'normal'
}

export function useUiMode(): UiMode {
  const [mode, setMode] = useState<UiMode>(() =>
    typeof document === 'undefined' ? 'normal' : readAppliedUiMode(),
  )

  useEffect(() => {
    const root = document.documentElement
    const sync = () => setMode(root.dataset.uiMode === 'compact' ? 'compact' : 'normal')
    sync()
    const observer = new MutationObserver(sync)
    observer.observe(root, { attributes: true, attributeFilter: ['data-ui-mode'] })
    return () => observer.disconnect()
  }, [])

  return mode
}
