import { afterEach, describe, expect, it, vi } from 'vitest'
import {
  isSessionExpiredUnauthorized,
  notifySessionExpired,
  setUnauthorizedHandler,
} from './sessionExpiry'

describe('sessionExpiry', () => {
  afterEach(() => {
    setUnauthorizedHandler(null)
  })

  it('treats UNAUTHENTICATED 401 as a lost session', () => {
    expect(isSessionExpiredUnauthorized(401, 'UNAUTHENTICATED')).toBe(true)
  })

  it('does not treat invalid login credentials as a lost session', () => {
    expect(isSessionExpiredUnauthorized(401, 'INVALID_CREDENTIALS')).toBe(false)
  })

  it('notifies the handler only for UNAUTHENTICATED', () => {
    const handler = vi.fn()
    setUnauthorizedHandler(handler)

    notifySessionExpired(401, 'INVALID_CREDENTIALS')
    expect(handler).not.toHaveBeenCalled()

    notifySessionExpired(403, 'ACCESS_DENIED')
    expect(handler).not.toHaveBeenCalled()

    notifySessionExpired(401, 'UNAUTHENTICATED')
    expect(handler).toHaveBeenCalledTimes(1)
  })
})
