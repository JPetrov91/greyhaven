import { ApiError, apiRequest } from './client'
import type { MeResponse } from './types'

/** Returns the current user, or null when there is no session. */
export async function fetchMe(): Promise<MeResponse | null> {
  try {
    return await apiRequest<MeResponse>('/api/v1/me')
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) {
      return null
    }
    throw error
  }
}

export function registerAccount(email: string, password: string): Promise<MeResponse> {
  return apiRequest<MeResponse>('/api/v1/auth/register', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
}

export function loginAccount(email: string, password: string): Promise<MeResponse> {
  return apiRequest<MeResponse>('/api/v1/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
}

export function logoutAccount(): Promise<void> {
  return apiRequest<void>('/api/v1/auth/logout', {
    method: 'POST',
  })
}
