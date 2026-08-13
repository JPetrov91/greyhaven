import { notifySessionExpired } from '../auth/sessionExpiry'
import type { ApiErrorBody } from './types'

export class ApiError extends Error {
  readonly code: string
  readonly status: number

  constructor(status: number, body: ApiErrorBody) {
    super(body.message)
    this.name = 'ApiError'
    this.status = status
    this.code = body.code
  }
}

function readCookie(name: string): string | null {
  const prefix = `${name}=`
  const parts = document.cookie.split(';')
  for (const part of parts) {
    const trimmed = part.trim()
    if (trimmed.startsWith(prefix)) {
      return decodeURIComponent(trimmed.slice(prefix.length))
    }
  }
  return null
}

async function ensureCsrfCookie(): Promise<void> {
  // Always refresh: Spring replaces the CSRF token on authentication, and a
  // cached cookie from before login/register will 403 subsequent POSTs.
  await fetch('/api/v1/bootstrap', {
    method: 'GET',
    credentials: 'include',
  })
}

export async function apiRequest<T>(
  path: string,
  init: RequestInit = {},
): Promise<T> {
  const method = (init.method ?? 'GET').toUpperCase()
  const headers = new Headers(init.headers)

  if (init.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  if (method !== 'GET' && method !== 'HEAD' && method !== 'OPTIONS' && method !== 'TRACE') {
    await ensureCsrfCookie()
    const csrfToken = readCookie('XSRF-TOKEN')
    if (csrfToken) {
      headers.set('X-XSRF-TOKEN', csrfToken)
    }
  }

  const response = await fetch(path, {
    ...init,
    method,
    headers,
    credentials: 'include',
  })

  if (response.status === 204) {
    return undefined as T
  }

  const contentType = response.headers.get('Content-Type') ?? ''
  const isJson = contentType.includes('application/json')
  const payload = isJson ? await response.json() : undefined

  if (!response.ok) {
    if (payload && typeof payload === 'object' && 'code' in payload && 'message' in payload) {
      const body = payload as ApiErrorBody
      notifySessionExpired(response.status, body.code)
      throw new ApiError(response.status, body)
    }
    throw new ApiError(response.status, {
      code: 'REQUEST_FAILED',
      message: `Request failed with status ${response.status}`,
      timestamp: new Date().toISOString(),
    })
  }

  return payload as T
}
