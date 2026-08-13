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
  unspentAttributePoints: number
  currentLocationId: string | null
  derivedStats: DerivedStatsResponse
  createdAt: string
  updatedAt: string
}

export type CombatAction =
  | 'QUICK_ATTACK'
  | 'HEAVY_ATTACK'
  | 'PRECISE_ATTACK'
  | 'DEFEND'
  | 'USE_POTION'
  | 'RETREAT'

export type CombatSessionStatus = 'ACTIVE' | 'PLAYER_WON' | 'PLAYER_LOST' | 'PLAYER_ESCAPED'
export type EncounterStatus = 'AVAILABLE' | 'COMBAT_STARTED' | 'RESOLVED' | 'EXPIRED'

export type MonsterResponse = {
  id: string
  code: string
  name: string
  level: number
  maxHealth: number
}

export type EncounterSearchResponse = {
  found: boolean
  encounterId: string | null
  monster: MonsterResponse | null
}

export type EncounterResponse = {
  id: string
  status: EncounterStatus
  monster: MonsterResponse | null
}

export type CombatEventResponse = {
  roundNumber: number
  sequenceNumber: number
  type: string
  message: string
}

export type CombatRewardItemResponse = {
  itemCode: string
  itemName: string
  quantity: number
}

export type CombatRewardsResponse = {
  xp: number
  gold: number
  items: CombatRewardItemResponse[]
}

export type CombatResponse = {
  id: string
  encounterId: string
  status: CombatSessionStatus
  roundNumber: number
  playerHealth: number
  playerMaxHealth: number
  playerStamina: number
  playerMaxStamina: number
  enemyHealth: number
  enemyMaxHealth: number
  monster: MonsterResponse
  potionAvailable: boolean
  events: CombatEventResponse[]
  rewards: CombatRewardsResponse | null
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
  listedQuantity: number
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

export type ExpeditionStrategy = 'CAUTIOUS' | 'BALANCED' | 'AGGRESSIVE'
export type ExpeditionStatus = 'ACTIVE' | 'COMPLETED' | 'CLAIMED'
export type ExpeditionType = 'FOREST_PATROL'

export type ExpeditionRewardItemResponse = {
  itemCode: string
  itemName: string
  quantity: number
}

export type ExpeditionRewardsResponse = {
  xp: number
  gold: number
  injuryDamage: number
  items: ExpeditionRewardItemResponse[]
}

export type ExpeditionResponse = {
  id: string
  expeditionType: ExpeditionType
  expeditionName: string
  strategy: ExpeditionStrategy
  status: ExpeditionStatus
  startedAt: string
  completesAt: string
  claimedAt: string | null
  resultReady: boolean
  rewards: ExpeditionRewardsResponse | null
}

export type ActivityType =
  | 'COMBAT_VICTORY'
  | 'LEVEL_UP'
  | 'ITEM_FOUND'
  | 'EXPEDITION_COMPLETED'
  | 'EXPEDITION_CLAIMED'
  | 'MARKET_SOLD'
  | 'MARKET_BOUGHT'
  | 'MARKET_CANCELLED'

export type MarketListingStatus = 'ACTIVE' | 'SOLD' | 'CANCELLED'

export type MarketListingResponse = {
  id: string
  sellerCharacterId: string
  sellerName: string
  itemInstanceId: string | null
  itemCode: string
  itemName: string
  itemType: ItemType
  rarity: ItemRarity
  quantity: number
  price: number
  status: MarketListingStatus
  createdAt: string
  soldAt: string | null
  ownListing: boolean
}

export type MarketListingsResponse = {
  listings: MarketListingResponse[]
  truncated: boolean
}

export type ActivityEntryResponse = {
  id: string
  type: ActivityType
  message: string
  createdAt: string
  readAt: string | null
}
