import { apiRequest } from './client'
import type { ActivityEntryResponse } from './types'

export function fetchActivity(): Promise<ActivityEntryResponse[]> {
  return apiRequest<ActivityEntryResponse[]>('/api/v1/activity')
}
