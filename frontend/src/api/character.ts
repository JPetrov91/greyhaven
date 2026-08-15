import { apiRequest } from './client'
import type { CharacterResponse, CharacterRosterResponse } from './types'

export function fetchCharacter(): Promise<CharacterResponse> {
  return apiRequest<CharacterResponse>('/api/v1/character')
}

export function fetchCharacterRoster(): Promise<CharacterRosterResponse> {
  return apiRequest<CharacterRosterResponse>('/api/v1/characters')
}

export function createCharacter(input: {
  name: string
  gender: 'MALE' | 'FEMALE'
  avatarCode: string
  slotIndex?: number
}): Promise<CharacterResponse> {
  return apiRequest<CharacterResponse>('/api/v1/characters', {
    method: 'POST',
    body: JSON.stringify(input),
  })
}

export function selectCharacter(characterId: string): Promise<CharacterResponse> {
  return apiRequest<CharacterResponse>(`/api/v1/characters/${characterId}/select`, {
    method: 'POST',
  })
}

export function checkCharacterNameAvailable(name: string): Promise<{ available: boolean }> {
  return apiRequest<{ available: boolean }>(
    `/api/v1/characters/name-available?name=${encodeURIComponent(name)}`,
  )
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
