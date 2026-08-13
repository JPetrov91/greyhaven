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

export type LocationSafety = 'SAFE' | 'DANGEROUS'

export type LocationAction =
  | 'INSPECT'
  | 'MOVE'
  | 'VIEW_NEARBY'
  | 'VIEW_CHAT'
  | 'START_EXPEDITION'
  | 'INSPECT_EXPEDITIONS'
  | 'BROWSE_MARKET'
  | 'CREATE_LISTING'
  | 'BUY_ITEM'
  | 'CANCEL_LISTING'
  | 'SEARCH_ENCOUNTER'

export type LocationResponse = {
  id: string
  code: string
  name: string
  description: string
  safety: LocationSafety
  region: string
  actions: LocationAction[]
}

export type DestinationResponse = {
  id: string
  code: string
  name: string
  safety: LocationSafety
}

export type DestinationsResponse = {
  destinations: DestinationResponse[]
}

export type NearbyCharacterResponse = {
  id: string
  name: string
  level: number
}

export type NearbyCharactersResponse = {
  characters: NearbyCharacterResponse[]
  truncated: boolean
}
