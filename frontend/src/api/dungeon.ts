import { apiRequest } from './client'
import type { DungeonRunResponse } from './types'

export async function fetchCurrentDungeon(): Promise<DungeonRunResponse | null> {
  const body = await apiRequest<DungeonRunResponse | undefined>('/api/v1/dungeons/current')
  return body ?? null
}

export function enterDungeon(): Promise<DungeonRunResponse> {
  return apiRequest<DungeonRunResponse>('/api/v1/dungeons/enter', { method: 'POST' })
}

export function leaveDungeon(): Promise<DungeonRunResponse> {
  return apiRequest<DungeonRunResponse>('/api/v1/dungeons/leave', { method: 'POST' })
}

export function abandonDungeon(): Promise<DungeonRunResponse> {
  return apiRequest<DungeonRunResponse>('/api/v1/dungeons/abandon', { method: 'POST' })
}

export function advanceDungeon(edgeCode: string): Promise<DungeonRunResponse> {
  return apiRequest<DungeonRunResponse>('/api/v1/dungeons/advance', {
    method: 'POST',
    body: JSON.stringify({ edgeCode }),
  })
}
