import { apiRequest } from './client'
import type { ExpeditionResponse, ExpeditionStrategy } from './types'

export async function fetchCurrentExpedition(): Promise<ExpeditionResponse | null> {
  const body = await apiRequest<ExpeditionResponse | undefined>('/api/v1/expeditions/current')
  return body ?? null
}

export function startExpedition(strategy: ExpeditionStrategy): Promise<ExpeditionResponse> {
  return apiRequest<ExpeditionResponse>('/api/v1/expeditions', {
    method: 'POST',
    body: JSON.stringify({ strategy }),
  })
}

export function claimExpedition(expeditionId: string): Promise<ExpeditionResponse> {
  return apiRequest<ExpeditionResponse>(`/api/v1/expeditions/${expeditionId}/claim`, {
    method: 'POST',
  })
}
