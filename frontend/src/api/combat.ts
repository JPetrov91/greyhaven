import { apiRequest } from './client'
import type { CombatAction, CombatResponse } from './types'

export async function fetchCurrentCombat(): Promise<CombatResponse | null> {
  const body = await apiRequest<CombatResponse | undefined>('/api/v1/combat/current')
  return body ?? null
}

export function submitCombatAction(
  combatId: string,
  action: CombatAction,
  expectedRoundNumber: number,
): Promise<CombatResponse> {
  return apiRequest<CombatResponse>(`/api/v1/combat/${combatId}/actions`, {
    method: 'POST',
    body: JSON.stringify({ action, expectedRoundNumber }),
  })
}

export function acknowledgeCombat(combatId: string): Promise<void> {
  return apiRequest<void>(`/api/v1/combat/${combatId}/acknowledge`, {
    method: 'POST',
  })
}
