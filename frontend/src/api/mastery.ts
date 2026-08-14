import { apiRequest } from './client'
import type { MasteriesResponse, TechniquesResponse } from './types'

export function fetchMasteries(): Promise<MasteriesResponse> {
  return apiRequest<MasteriesResponse>('/api/v1/character/masteries')
}

export function fetchTechniques(): Promise<TechniquesResponse> {
  return apiRequest<TechniquesResponse>('/api/v1/character/techniques')
}

export function updateTechniqueLoadout(slots: Array<string | null>): Promise<TechniquesResponse> {
  return apiRequest<TechniquesResponse>('/api/v1/character/technique-loadout', {
    method: 'PUT',
    body: JSON.stringify({ slots }),
  })
}
