import { apiRequest } from './client'
import type { CombatAction, CombatStatusResponse } from './types'

export type ArenaDefense = {
  preferredAction: CombatAction
  preferredTechniqueCode: string | null
  healWhenHpPercentBelow: number
  defendWhenStaminaPercentBelow: number
  finisherWhenEnemyHpPercentBelow: number
  finisherTechniqueCode: string | null
}

export type ArenaProfileResponse = {
  characterId: string
  rating: number
  marks: number
  defense: ArenaDefense
  preferredActionOptions: CombatAction[]
}

export type ArenaOpponentResponse = {
  id: string
  name: string
  level: number
  rating: number
}

export type PublicCharacterResponse = {
  id: string
  name: string
  level: number
  strength: number
  agility: number
  endurance: number
  perception: number
  arenaRating: number
  weaponFamily: string | null
  weaponMasteryLevel: number | null
  techniqueLoadout: string[]
  equipment: {
    slot: string
    code: string
    displayName: string
    rarity: string
    weaponDamage: number | null
    armorValue: number | null
    affixes: { code: string; displayName: string; stat: string; magnitude: number }[]
  }[]
}

export type PvpMatchResponse = {
  id: string
  matchKind: 'ARENA' | 'DUEL'
  status: string
  roundNumber: number
  attackerName: string
  defenderName: string
  attackerId: string
  defenderId: string
  attackerHealth: number
  attackerMaxHealth: number
  attackerStamina: number
  attackerMaxStamina: number
  defenderHealth: number
  defenderMaxHealth: number
  defenderStamina: number
  defenderMaxStamina: number
  potionAvailable: boolean
  events: { roundNumber: number; sequenceNumber: number; type: string; message: string }[]
  defenderIntent: { kind: string; label: string } | null
  actionPreviews: {
    action: CombatAction
    techniqueCode: string | null
    name: string
    description: string
    staminaCost: number
    hitChancePercent: number | null
    disabledReason: string | null
  }[]
  techniques: { code: string; name: string; description: string; staminaCost: number; disabledReason: string | null }[]
  settlement: {
    attackerRatingDelta: number
    defenderRatingDelta: number
    attackerMarks: number
    defenderMarks: number
    applied: boolean
  } | null
  waitingForOpponent: boolean
  yourPendingAction: CombatAction | null
  outcomeAcknowledged: boolean
  attackerStatuses?: CombatStatusResponse[]
  defenderStatuses?: CombatStatusResponse[]
}

export type PvpHistoryPageResponse = {
  entries: {
    matchId: string
    matchKind: string
    opponentName: string
    opponentId: string
    result: string
    ratingDelta: number
    marksAwarded: number
    createdAt: string
  }[]
  page: number
  size: number
  hasMore: boolean
}

export function fetchPublicCharacter(id: string): Promise<PublicCharacterResponse> {
  return apiRequest<PublicCharacterResponse>(`/api/v1/characters/${id}/public`)
}

export function fetchArenaProfile(): Promise<ArenaProfileResponse> {
  return apiRequest<ArenaProfileResponse>('/api/v1/pvp/arena')
}

export function updateArenaDefense(defense: ArenaDefense): Promise<ArenaProfileResponse> {
  return apiRequest<ArenaProfileResponse>('/api/v1/pvp/arena/defense', {
    method: 'PUT',
    body: JSON.stringify(defense),
  })
}

export function fetchArenaOpponents(page = 0): Promise<{
  opponents: ArenaOpponentResponse[]
  page: number
  size: number
  hasMore: boolean
}> {
  return apiRequest(`/api/v1/pvp/arena/opponents?page=${page}`)
}

export function challengeArena(defenderId: string): Promise<PvpMatchResponse> {
  return apiRequest<PvpMatchResponse>('/api/v1/pvp/arena/challenges', {
    method: 'POST',
    body: JSON.stringify({ defenderId }),
  })
}

export async function fetchCurrentArenaMatch(): Promise<PvpMatchResponse | null> {
  const body = await apiRequest<PvpMatchResponse | undefined>('/api/v1/pvp/arena/matches/current')
  return body ?? null
}

export function submitArenaAction(
  matchId: string,
  action: CombatAction,
  expectedRoundNumber: number,
  techniqueCode?: string,
): Promise<PvpMatchResponse> {
  return apiRequest<PvpMatchResponse>(`/api/v1/pvp/arena/matches/${matchId}/actions`, {
    method: 'POST',
    body: JSON.stringify({ action, expectedRoundNumber, techniqueCode: techniqueCode ?? null }),
  })
}

export function acknowledgeArenaMatch(matchId: string): Promise<void> {
  return apiRequest<void>(`/api/v1/pvp/arena/matches/${matchId}/acknowledge`, { method: 'POST' })
}

export function fetchPvpHistory(page = 0): Promise<PvpHistoryPageResponse> {
  return apiRequest<PvpHistoryPageResponse>(`/api/v1/pvp/history?page=${page}`)
}

export function challengeDuel(defenderId: string): Promise<PvpMatchResponse> {
  return apiRequest<PvpMatchResponse>('/api/v1/pvp/duels', {
    method: 'POST',
    body: JSON.stringify({ defenderId }),
  })
}

export function acceptDuel(id: string): Promise<PvpMatchResponse> {
  return apiRequest<PvpMatchResponse>(`/api/v1/pvp/duels/${id}/accept`, { method: 'POST' })
}

export function declineDuel(id: string): Promise<PvpMatchResponse> {
  return apiRequest<PvpMatchResponse>(`/api/v1/pvp/duels/${id}/decline`, { method: 'POST' })
}

export async function fetchCurrentDuel(): Promise<PvpMatchResponse | null> {
  const body = await apiRequest<PvpMatchResponse | undefined>('/api/v1/pvp/duels/current')
  return body ?? null
}

export function submitDuelAction(
  matchId: string,
  action: CombatAction,
  expectedRoundNumber: number,
  techniqueCode?: string,
): Promise<PvpMatchResponse> {
  return apiRequest<PvpMatchResponse>(`/api/v1/pvp/duels/${matchId}/actions`, {
    method: 'POST',
    body: JSON.stringify({ action, expectedRoundNumber, techniqueCode: techniqueCode ?? null }),
  })
}
