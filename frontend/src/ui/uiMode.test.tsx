// @vitest-environment jsdom

import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it } from 'vitest'
import { applyUiMode, persistUiMode, readStoredUiMode, UI_MODE_STORAGE_KEY, useUiMode } from './uiMode'

afterEach(() => {
  cleanup()
  localStorage.removeItem(UI_MODE_STORAGE_KEY)
  delete document.documentElement.dataset.uiMode
})

function ModeLabel() {
  const mode = useUiMode()
  return <span data-testid="mode">{mode}</span>
}

describe('uiMode', () => {
  it('defaults to normal and persists compact office mode locally', () => {
    expect(readStoredUiMode()).toBe('normal')
    persistUiMode('compact')
    expect(localStorage.getItem(UI_MODE_STORAGE_KEY)).toBe('compact')
    expect(readStoredUiMode()).toBe('compact')
    applyUiMode('compact')
    expect(document.documentElement.dataset.uiMode).toBe('compact')
  })

  it('exposes the applied office mode to components', () => {
    applyUiMode('compact')
    render(<ModeLabel />)
    expect(screen.getByTestId('mode').textContent).toBe('compact')
  })
})
