import { describe, expect, it, vi } from 'vitest'
import { fetchServerHealth } from './health'

describe('fetchServerHealth', () => {
  it('returns true when actuator reports UP', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: true,
        json: async () => ({ status: 'UP' }),
      }),
    )
    await expect(fetchServerHealth()).resolves.toBe(true)
  })

  it('returns false when the health endpoint is unreachable', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockRejectedValue(new Error('offline')),
    )
    await expect(fetchServerHealth()).resolves.toBe(false)
  })
})
