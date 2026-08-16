import { apiRequest } from './client'
import type { CombatResponse } from './types'

export type SparringBotResponse = {
  level: number
  name: string
  code: string
}

export function fetchSparringBots(): Promise<SparringBotResponse[]> {
  return apiRequest<SparringBotResponse[]>('/api/v1/sparring/bots')
}

export function startSparringDrill(botLevel: number): Promise<CombatResponse> {
  return apiRequest<CombatResponse>('/api/v1/sparring/drills', {
    method: 'POST',
    body: JSON.stringify({ botLevel }),
  })
}
