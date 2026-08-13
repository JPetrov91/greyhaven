export type ApiErrorBody = {
  code: string
  message: string
  timestamp: string
}

export type MeResponse = {
  accountId: string
  email: string
  hasCharacter: boolean
}

export type CharacterResponse = {
  id: string
  accountId: string
  name: string
  level: number
  experience: number
  strength: number
  agility: number
  endurance: number
  perception: number
  currentHealth: number
  maxHealth: number
  currentStamina: number
  maxStamina: number
  gold: number
  currentLocationId: string | null
  createdAt: string
  updatedAt: string
}
