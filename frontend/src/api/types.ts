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

export type ProgressionResponse = {
  level: number
  totalExperience: number
  experienceIntoCurrentLevel: number
  experienceRequiredForNextLevel: number | null
  experienceRemaining: number | null
  progressPercent: number
  maxLevel: boolean
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
  progression: ProgressionResponse
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
  | 'USE_TECHNIQUE'

export type CombatSessionStatus = 'ACTIVE' | 'PLAYER_WON' | 'PLAYER_LOST' | 'PLAYER_ESCAPED'
export type EncounterStatus = 'AVAILABLE' | 'COMBAT_STARTED' | 'RESOLVED' | 'EXPIRED'

export type CombatStatusResponse = {
  type: string
  stacks: number
  remainingRounds: number
}

export type CombatTechniqueOptionResponse = {
  code: string
  name: string
  description: string
  staminaCost: number
  disabledReason: string | null
}

export type CoreActionCostsResponse = {
  quickAttack: number
  heavyAttack: number
  preciseAttack: number
}

export type MonsterResponse = {
  id: string
  code: string
  name: string
  level: number
  maxHealth: number
  archetype?: string | null
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
  previousLevel: number
  newLevel: number
  attributePointsGained: number
  items: CombatRewardItemResponse[]
}

export type CombatResponse = {
  id: string
  encounterId: string
  status: CombatSessionStatus
  rulesVersion: number
  roundNumber: number
  playerHealth: number
  playerMaxHealth: number
  playerStamina: number
  playerMaxStamina: number
  enemyHealth: number
  enemyMaxHealth: number
  enemyStamina: number
  enemyMaxStamina: number
  monster: MonsterResponse
  potionAvailable: boolean
  playerStunned: boolean
  playerStatuses: CombatStatusResponse[]
  enemyStatuses: CombatStatusResponse[]
  techniques: CombatTechniqueOptionResponse[]
  coreActionCosts: CoreActionCostsResponse
  events: CombatEventResponse[]
  rewards: CombatRewardsResponse | null
}

export type ItemType = 'WEAPON' | 'ARMOR' | 'CONSUMABLE' | 'MATERIAL' | 'ACCESSORY'
export type ItemRarity = 'COMMON' | 'UNCOMMON' | 'RARE' | 'EPIC'
export type EquipmentSlot =
  | 'HEAD'
  | 'CHEST'
  | 'HANDS'
  | 'LEGS'
  | 'FEET'
  | 'MAIN_HAND'
  | 'OFF_HAND'
  | 'AMULET'
  | 'RING'

export type ItemAffixResponse = {
  code: string
  kind: 'PREFIX' | 'SUFFIX'
  displayName: string
  stat: string
  magnitude: number
}

export type StatDeltaResponse = {
  stat: string
  equippedValue: number
  candidateValue: number
  delta: number
}

export type ItemComparisonResponse = {
  slot: EquipmentSlot
  equippedItemId: string | null
  verdict: 'UPGRADE' | 'DOWNGRADE' | 'MIXED' | 'SAME'
  deltas: StatDeltaResponse[]
}

export type InventoryItemResponse = {
  id: string
  definitionId: string
  code: string
  name: string
  displayName: string
  description: string
  type: ItemType
  rarity: ItemRarity
  quantity: number
  requiredLevel: number
  requiredStrength: number
  requiredAgility: number
  requiredEndurance: number
  requiredPerception: number
  baseValue: number
  equipped: boolean
  canEquip: boolean
  twoHanded: boolean
  legacy: boolean
  equipmentSlot: EquipmentSlot | null
  weaponFamily: string | null
  armorCategory: string | null
  usable: boolean
  listedQuantity: number
  rolledWeaponDamage: number | null
  rolledArmorValue: number | null
  weaponDamage: number | null
  armorValue: number | null
  healAmount: number | null
  affixes: ItemAffixResponse[]
  comparison: ItemComparisonResponse | null
}

export type EquipmentResponse = {
  slots: Record<EquipmentSlot, string | null>
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
  | 'MASTERY_UNLOCK'
  | 'TECHNIQUE_UNLOCK'

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

export type ChatMessageResponse = {
  id: string
  characterId: string
  characterName: string
  body: string
  createdAt: string
}

export type WeaponFamily = 'SWORD' | 'AXE' | 'MACE' | 'DAGGER' | 'BOW'
export type TechniqueKind = 'ACTIVE' | 'PASSIVE'

export type MasteryProgressResponse = {
  level: number
  totalExperience: number
  experienceIntoCurrentLevel: number
  experienceRequiredForNextLevel: number | null
  experienceRemaining: number | null
  progressPercent: number
  maxLevel: boolean
}

export type WeaponMasteryResponse = {
  weaponFamily: WeaponFamily
  level: number
  totalExperience: number
  progress: MasteryProgressResponse
  nextUnlockCodes: string[]
}

export type MasteriesResponse = {
  equippedWeaponFamily: WeaponFamily | null
  masteries: WeaponMasteryResponse[]
}

export type TechniqueDefinitionResponse = {
  code: string
  displayName: string
  description: string
  weaponFamily: WeaponFamily
  unlockMasteryLevel: number
  kind: TechniqueKind
  unlocked: boolean
  staminaCost: number
  accuracyModifier: number
  damagePercentModifier: number
  appliesStatus: string | null
  tags: string
}

export type TechniqueLoadoutResponse = {
  slots: Array<string | null>
  loadoutFamily: WeaponFamily | null
  compatibleWithEquippedWeapon: boolean
}

export type TechniquesResponse = {
  equippedWeaponFamily: WeaponFamily | null
  techniques: TechniqueDefinitionResponse[]
  loadout: TechniqueLoadoutResponse
}
