export type ApiErrorBody = {
  code: string
  message: string
  timestamp: string
}

export type MeResponse = {
  accountId: string
  email: string
  characterCount: number
  activeCharacterId: string | null
}

export type CharacterEquippedSlotResponse = {
  slot: string
  displayName: string
  rarity: string
}

export type CharacterSlotResponse = {
  slotIndex: number
  empty: boolean
  characterId: string | null
  name: string | null
  gender: 'MALE' | 'FEMALE' | null
  avatarCode: string | null
  level: number
  gold: number
  currentLocationId: string | null
  locationName: string | null
  strength: number
  agility: number
  endurance: number
  perception: number
  currentHealth: number
  maxHealth: number
  currentStamina: number
  maxStamina: number
  physicalDamage: number
  accuracy: number
  dodge: number
  criticalChance: number
  armor: number
  healingPotions: number
  equipped: CharacterEquippedSlotResponse[]
}

export type CharacterRosterResponse = {
  slots: CharacterSlotResponse[]
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
  gender?: 'MALE' | 'FEMALE'
  avatarCode?: string
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
  arenaRating: number
  arenaMarks: number
  unspentAttributePoints: number
  currentLocationId: string | null
  derivedStats: DerivedStatsResponse
  progression: ProgressionResponse
  createdAt: string
  updatedAt: string
  unlocks?: string[]
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
  tier?: string | null
}

export type EncounterSearchResponse = {
  found: boolean
  encounterId: string | null
  monster: MonsterResponse | null
  flavour?: string | null
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

export type CombatIntentResponse = {
  kind: string
  label: string
}

export type CombatActionPreviewResponse = {
  action: CombatAction
  techniqueCode: string | null
  name: string
  description: string
  staminaCost: number
  hitChancePercent: number | null
  disabledReason: string | null
}

export type CombatLootPreviewResponse = {
  itemName: string
  dropChancePercent: number
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
  enemyIntent?: CombatIntentResponse | null
  actionPreviews?: CombatActionPreviewResponse[]
  possibleLoot?: CombatLootPreviewResponse[]
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
  weaponDamageMin?: number | null
  weaponDamageMax?: number | null
  blockSoakMin?: number | null
  blockSoakMax?: number | null
  armorValue: number | null
  healAmount: number | null
  accuracy?: number
  criticalChance?: number
  dodge?: number
  strength?: number
  agility?: number
  endurance?: number
  perception?: number
  staminaCostReduction?: number
  affixes: ItemAffixResponse[]
  comparison: ItemComparisonResponse | null
  merchantBuyPrice?: number
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
  | 'ENTER_DUNGEON'
  | 'ENTER_ARENA'
  | 'CHALLENGE_DUEL'
  | 'START_SPARRING_DRILL'
  | 'CRAFT'
  | 'CLAIM_CRAFT'
  | 'SALVAGE'
  | 'CREATE_BUY_ORDER'
  | 'FULFILL_BUY_ORDER'
  | 'TALK_NPCS'
  | 'NOTICE_BOARD'

export type LocationResponse = {
  id: string
  code: string
  name: string
  description: string
  safety: LocationSafety
  region: string
  recommendedLevelMin?: number | null
  recommendedLevelMax?: number | null
  actions: LocationAction[]
}

export type DestinationResponse = {
  id: string
  code: string
  name: string
  safety: LocationSafety
  recommendedLevelMin?: number | null
  recommendedLevelMax?: number | null
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
  | 'ARENA_VICTORY'
  | 'ARENA_DEFEAT'
  | 'DUEL_RESULT'
  | 'CRAFTING_STARTED'
  | 'CRAFTING_CLAIMED'
  | 'PROFESSION_RANK_UP'
  | 'ITEM_SALVAGED'
  | 'MARKET_LISTING_FEE'
  | 'MARKET_SALE'
  | 'BUY_ORDER_CREATED'
  | 'BUY_ORDER_FILLED'
  | 'BUY_ORDER_CANCELLED'
  | 'QUEST_ACCEPTED'
  | 'QUEST_OBJECTIVE'
  | 'QUEST_COMPLETED'

export type MarketListingStatus = 'ACTIVE' | 'SOLD' | 'CANCELLED'

export type MarketListingResponse = {
  id: string
  sellerCharacterId: string
  sellerName: string
  itemInstanceId: string | null
  itemDefinitionId?: string | null
  itemCode: string
  itemName: string
  displayName?: string
  itemType: ItemType
  rarity: ItemRarity
  weaponFamily?: WeaponFamily | null
  requiredLevel?: number
  quantity: number
  price: number
  listingFeePaid?: number
  saleFeePaid?: number | null
  status: MarketListingStatus
  createdAt: string
  soldAt: string | null
  ownListing: boolean
  affixes?: ItemAffixResponse[]
}

export type MarketListingsResponse = {
  listings: MarketListingResponse[]
  truncated: boolean
  page?: number
  size?: number
  total?: number
  listingFeePercent?: number
  saleFeePercent?: number
}

export type MarketBuyOrderStatus = 'ACTIVE' | 'FILLED' | 'CANCELLED'

export type MarketBuyOrderResponse = {
  id: string
  buyerCharacterId: string
  buyerName: string
  itemDefinitionId: string
  itemCode: string
  itemName: string
  itemType: ItemType
  remainingQuantity: number
  originalQuantity: number
  maxUnitPrice: number
  reservedGold: number
  postingFeePaid?: number
  status: MarketBuyOrderStatus
  createdAt: string
  ownOrder: boolean
}

export type MarketBuyOrdersResponse = {
  orders: MarketBuyOrderResponse[]
  truncated: boolean
  page: number
  size: number
  total: number
}

export type Profession = 'BLACKSMITH' | 'ALCHEMIST' | 'HUNTER'
export type CraftingJobStatus = 'ACTIVE' | 'COMPLETED' | 'CLAIMED'

export type ProfessionResponse = {
  profession: Profession
  rank: number
  xp: number
  xpToNextRank: number
  maxRank: boolean
}

export type RecipeInputResponse = {
  itemCode: string
  itemName: string
  quantity: number
  availableQuantity: number
}

export type RecipeResponse = {
  code: string
  name: string
  profession: Profession
  requiredProfessionRank: number
  requiredCharacterLevel: number
  goldCost: number
  durationSeconds: number
  outputItemCode: string
  outputItemName: string
  outputQuantity: number
  minRarity: ItemRarity | null
  maxRarity: ItemRarity | null
  professionXp: number
  available: boolean
  unavailableReason: string | null
  inputs: RecipeInputResponse[]
}

export type CraftingJobResponse = {
  id: string
  profession: Profession
  recipeCode: string
  recipeName: string
  status: CraftingJobStatus
  startedAt: string
  completesAt: string
  claimedAt: string | null
  resultReady: boolean
  outputItemCode: string
  outputItemName: string
  outputQuantity: number
  rarity: ItemRarity | null
  professionXp: number
}

export type SalvageResultResponse = {
  itemCode: string
  itemName: string
  quantity: number
}

export type SalvageResponse = {
  sourceItemCode: string
  sourceItemName: string
  results: SalvageResultResponse[]
}

export type MerchantType = 'WEAPONSMITH' | 'ARMORER' | 'APOTHECARY' | 'GENERAL'

export type MerchantStockItemResponse = {
  itemDefinitionId: string
  itemCode: string
  itemName: string
  description: string
  itemType: ItemType
  rarity: ItemRarity
  sellPrice: number
  availabilityType: 'UNLIMITED'
  requiredLevel: number
  weaponDamage: number | null
  armorValue: number | null
  healAmount: number | null
  twoHanded: boolean
  equipmentSlot: EquipmentSlot | null
  weaponFamily: string | null
  armorCategory: string | null
  requiredStrength: number
  requiredAgility: number
  requiredEndurance: number
  requiredPerception: number
  accuracy?: number
  criticalChance?: number
  dodge?: number
  strength?: number
  agility?: number
  endurance?: number
  perception?: number
  staminaCostReduction?: number
}

export type MerchantResponse = {
  id: string
  code: string
  name: string
  title: string
  description: string
  merchantType: MerchantType
  portraitCode: string
  stock: MerchantStockItemResponse[]
}

export type MerchantListResponse = {
  merchants: MerchantResponse[]
}

export type MerchantPurchaseResponse = {
  merchantId: string
  itemDefinitionId: string
  itemCode: string
  itemName: string
  quantity: number
  pricePaid: number
  goldRemaining: number
}

export type MerchantSaleResponse = {
  itemInstanceId: string
  itemCode: string
  itemName: string
  quantity: number
  goldAwarded: number
  goldRemaining: number
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

export type DungeonRunStatus = 'ACTIVE' | 'COMPLETED' | 'ABANDONED'
export type DungeonRoomKind = 'ENTRANCE' | 'COMBAT' | 'CHOICE' | 'OPTIONAL' | 'BOSS'
export type DungeonRoomState = 'LOCKED' | 'AVAILABLE' | 'CLEARED' | 'SKIPPED'

export type DungeonRoomResponse = {
  code: string
  name: string
  kind: DungeonRoomKind
  state: DungeonRoomState
}

export type DungeonChoiceResponse = {
  edgeCode: string
  roomCode: string
  roomName: string
  optional: boolean
}

export type DungeonRunResponse = {
  runId: string
  dungeonCode: string
  dungeonName: string
  status: DungeonRunStatus
  paused: boolean
  currentRoomCode: string
  currentRoomName: string
  currentRoomDescription: string
  currentRoomKind: DungeonRoomKind
  chosenBranch: string | null
  uniqueRewardGranted: boolean
  rooms: DungeonRoomResponse[]
  choices: DungeonChoiceResponse[]
  encounter: EncounterSearchResponse | null
}

export type QuestObjectiveResponse = {
  type: string
  targetCode: string
  requiredAmount: number
  currentAmount: number
  completed: boolean
  displayText: string
  consumeOnTurnIn: boolean
  actionHint?: string | null
}

export type QuestRewardResponse = {
  kind: string
  amount: number
  itemCode: string | null
  itemName: string | null
  unlockCode: string | null
}

export type QuestResponse = {
  code: string
  name: string
  description: string
  category: string
  status: 'AVAILABLE' | 'ACTIVE' | 'READY_TO_TURN_IN' | 'COMPLETED'
  recommendedLevel: number
  startNpcCode: string | null
  startNpcName: string | null
  turnInNpcCode: string | null
  turnInNpcName: string | null
  nextQuestCode: string | null
  nextQuestName: string | null
  repeatable: boolean
  tracked: boolean
  objectives: QuestObjectiveResponse[]
  rewards: QuestRewardResponse[]
  unlocks: string[]
  kitFamily?: string | null
  lastSearchOutcome?: string | null
  completeText?: string | null
  shortDescription?: string | null
  questType?: string | null
  difficulty?: string | null
  artworkKey?: string | null
  boardLocationCode?: string | null
  objectiveLocationCode?: string | null
  locationName?: string | null
  regionName?: string | null
  actionHint?: string | null
  actionTargetCode?: string | null
  actionLocationCode?: string | null
}

export type QuestBoardListState = 'AVAILABLE' | 'UNAVAILABLE' | 'ACTIVE' | 'READY_TO_TURN_IN' | 'COMPLETED'

export type QuestBoardEntryResponse = {
  code: string
  name: string
  shortDescription: string
  questType: string
  listState: QuestBoardListState
  recommendedLevel: number
  difficulty: string
  rewards: QuestRewardResponse[]
}

export type QuestBoardResponse = {
  locationCode: string
  quests: QuestBoardEntryResponse[]
}

export type QuestListResponse = {
  quests: QuestResponse[]
}

export type NpcResponse = {
  code: string
  name: string
  title: string
  description: string
  greeting: string
  portraitCode: string
  locationCode: string
  merchantCode: string | null
  interactions: string[]
  questBadges: string[]
}

export type NpcListResponse = {
  npcs: NpcResponse[]
}

export type NpcTalkActionResponse = {
  type: 'ACCEPT' | 'TURN_IN' | 'SHOP' | 'CLOSE' | string
  questCode: string | null
  merchantCode: string | null
  label: string
  hint?: string | null
  action?: string | null
}

export type NpcTalkResponse = {
  code: string
  name: string
  title: string
  portraitCode: string
  text: string
  merchantCode: string | null
  actions: NpcTalkActionResponse[]
}
