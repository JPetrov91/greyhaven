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
