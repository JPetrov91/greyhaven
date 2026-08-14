// @vitest-environment jsdom

import { afterEach, describe, expect, it } from 'vitest'
import { NAV_COLLAPSE_STORAGE_KEY, persistNavCollapsed, readStoredNavCollapsed } from './navCollapse'

afterEach(() => {
  localStorage.removeItem(NAV_COLLAPSE_STORAGE_KEY)
})

describe('navCollapse', () => {
  it('defaults to expanded and persists the collapsed rail', () => {
    expect(readStoredNavCollapsed()).toBe(false)
    persistNavCollapsed(true)
    expect(localStorage.getItem(NAV_COLLAPSE_STORAGE_KEY)).toBe('true')
    expect(readStoredNavCollapsed()).toBe(true)
    persistNavCollapsed(false)
    expect(readStoredNavCollapsed()).toBe(false)
  })
})
