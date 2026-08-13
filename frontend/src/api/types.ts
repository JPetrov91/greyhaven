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

export type DerivedStatsResponse = {
  physicalDamage: number
  accuracy: number
  dodge: number
  criticalChance: number
  armor: number
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
  derivedStats: DerivedStatsResponse
  createdAt: string
  updatedAt: string
}

export type ItemType = 'WEAPON' | 'ARMOR' | 'CONSUMABLE' | 'MATERIAL'
export type ItemRarity = 'COMMON' | 'UNCOMMON' | 'RARE' | 'EPIC'
export type EquipmentSlot = 'WEAPON' | 'ARMOR'

export type InventoryItemResponse = {
  id: string
  definitionId: string
  code: string
  name: string
  description: string
  type: ItemType
  rarity: ItemRarity
  quantity: number
  requiredLevel: number
  baseValue: number
  equipped: boolean
  equipmentSlot: EquipmentSlot | null
  usable: boolean
  weaponDamage: number | null
  armorValue: number | null
  healAmount: number | null
}

export type EquipmentResponse = {
  weaponItemId: string | null
  armorItemId: string | null
}

export type InventoryResponse = {
  capacity: number
  usedSlots: number
  items: InventoryItemResponse[]
  equipment: EquipmentResponse
  derivedStats: DerivedStatsResponse
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
