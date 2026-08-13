// @vitest-environment jsdom

import { afterEach, describe, expect, it } from 'vitest'
import { applyUiMode, persistUiMode, readStoredUiMode, UI_MODE_STORAGE_KEY } from './uiMode'

afterEach(() => {
  localStorage.removeItem(UI_MODE_STORAGE_KEY)
  delete document.documentElement.dataset.uiMode
})

describe('uiMode', () => {
  it('defaults to normal and persists compact office mode locally', () => {
    expect(readStoredUiMode()).toBe('normal')
    persistUiMode('compact')
    expect(localStorage.getItem(UI_MODE_STORAGE_KEY)).toBe('compact')
    expect(readStoredUiMode()).toBe('compact')
    applyUiMode('compact')
    expect(document.documentElement.dataset.uiMode).toBe('compact')
  })
})
