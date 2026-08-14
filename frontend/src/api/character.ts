import { apiRequest } from './client'
import type { CharacterResponse } from './types'

export function fetchCharacter(): Promise<CharacterResponse> {
  return apiRequest<CharacterResponse>('/api/v1/character')
}

export function createCharacter(name: string): Promise<CharacterResponse> {
  return apiRequest<CharacterResponse>('/api/v1/characters', {
    method: 'POST',
    body: JSON.stringify({ name }),
  })
}

export function allocateAttributes(deltas: {
  strength: number
  agility: number
  endurance: number
  perception: number
}): Promise<CharacterResponse> {
  return apiRequest<CharacterResponse>('/api/v1/character/attributes', {
    method: 'POST',
    body: JSON.stringify(deltas),
  })
}

export function respecCharacter(): Promise<CharacterResponse> {
  return apiRequest<CharacterResponse>('/api/v1/character/respec', {
    method: 'POST',
  })
}
