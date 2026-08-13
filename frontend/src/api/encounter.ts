import { apiRequest } from './client'
import type { CombatResponse, EncounterResponse, EncounterSearchResponse } from './types'

export async function fetchCurrentEncounter(): Promise<EncounterSearchResponse | null> {
  const body = await apiRequest<EncounterSearchResponse | undefined>('/api/v1/encounters/current')
  return body ?? null
}

export function searchEncounter(): Promise<EncounterSearchResponse> {
  return apiRequest<EncounterSearchResponse>('/api/v1/encounters/search', {
    method: 'POST',
  })
}

export function fightEncounter(encounterId: string): Promise<CombatResponse> {
  return apiRequest<CombatResponse>(`/api/v1/encounters/${encounterId}/fight`, {
    method: 'POST',
  })
}

export function ignoreEncounter(encounterId: string): Promise<EncounterResponse> {
  return apiRequest<EncounterResponse>(`/api/v1/encounters/${encounterId}/ignore`, {
    method: 'POST',
  })
}
