import { apiRequest } from './client'
import type { QuestListResponse, QuestResponse } from './types'

export function fetchQuests(): Promise<QuestListResponse> {
  return apiRequest<QuestListResponse>('/api/v1/quests')
}

export function fetchQuest(code: string): Promise<QuestResponse> {
  return apiRequest<QuestResponse>(`/api/v1/quests/${code}`)
}

export function acceptQuest(code: string): Promise<QuestResponse> {
  return apiRequest<QuestResponse>(`/api/v1/quests/${code}/accept`, { method: 'POST' })
}

export function turnInQuest(code: string): Promise<QuestResponse> {
  return apiRequest<QuestResponse>(`/api/v1/quests/${code}/turn-in`, { method: 'POST' })
}

export function trackQuest(code: string): Promise<QuestResponse> {
  return apiRequest<QuestResponse>(`/api/v1/quests/${code}/track`, { method: 'POST' })
}

export function untrackQuest(code: string): Promise<QuestResponse> {
  return apiRequest<QuestResponse>(`/api/v1/quests/${code}/track`, { method: 'DELETE' })
}
