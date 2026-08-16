import { apiRequest } from './client'
import type { NpcListResponse, NpcResponse, NpcTalkResponse } from './types'

export function fetchNpcs(): Promise<NpcListResponse> {
  return apiRequest<NpcListResponse>('/api/v1/world/npcs')
}

export function fetchNpc(code: string): Promise<NpcResponse> {
  return apiRequest<NpcResponse>(`/api/v1/world/npcs/${code}`)
}

export function talkToNpc(
  code: string,
  questCode?: string,
  action?: string,
  kitFamily?: string,
): Promise<NpcTalkResponse> {
  return apiRequest<NpcTalkResponse>(`/api/v1/world/npcs/${code}/talk`, {
    method: 'POST',
    body: JSON.stringify({
      questCode: questCode ?? null,
      action: action ?? null,
      kitFamily: kitFamily ?? null,
    }),
  })
}
