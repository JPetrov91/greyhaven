import type {
  ActivityEntryResponse,
  ActivityType,
  CharacterResponse,
  ChatMessageResponse,
  CombatResponse,
  DestinationResponse,
  InventoryItemResponse,
  InventoryResponse,
  LocationResponse,
  NearbyCharacterResponse,
} from '../api/types'
import type { ActivityRowVariant } from '../ui/ActivityRow'

export const MAIN_SHELL_VISUAL_NOW = '2026-08-16T15:45:11.000Z'
export const MAIN_SHELL_VISUAL_CLOCK = '15:42'

function item(overrides: Partial<InventoryItemResponse> & Pick<InventoryItemResponse, 'id' | 'code' | 'name'>): InventoryItemResponse {
  return {
    definitionId: `def-${overrides.id}`,
    displayName: overrides.name,
    description: overrides.description ?? overrides.name,
    type: 'ARMOR',
    rarity: 'RARE',
    quantity: 1,
    requiredLevel: 40,
    requiredStrength: 0,
    requiredAgility: 0,
    requiredEndurance: 0,
    requiredPerception: 0,
    baseValue: 400,
    equipped: true,
    canEquip: true,
    twoHanded: false,
    legacy: false,
    equipmentSlot: 'HEAD',
    weaponFamily: null,
    armorCategory: 'HEAVY',
    usable: false,
    listedQuantity: 0,
    rolledWeaponDamage: null,
    rolledArmorValue: 12,
    weaponDamage: null,
    armorValue: 12,
    healAmount: null,
    affixes: [],
    comparison: null,
    ...overrides,
  }
}

export const mainShellCharacter: CharacterResponse = {
  id: 'visual-artino',
  accountId: 'visual-account',
  name: 'Artino',
  gender: 'MALE',
  avatarCode: 'male_iron_vow',
  level: 47,
  experience: 24780,
  strength: 142,
  agility: 89,
  endurance: 131,
  perception: 67,
  currentHealth: 3850,
  maxHealth: 3850,
  currentStamina: 120,
  maxStamina: 120,
  gold: 4320,
  arenaRating: 1584,
  arenaMarks: 18650,
  unspentAttributePoints: 0,
  currentLocationId: 'visual-market',
  derivedStats: {
    physicalDamage: 186,
    accuracy: 94,
    dodge: 18,
    criticalChance: 12,
    armor: 74,
  },
  progression: {
    level: 47,
    totalExperience: 24780,
    experienceIntoCurrentLevel: 1860,
    experienceRequiredForNextLevel: 3000,
    experienceRemaining: 1140,
    progressPercent: 62,
    maxLevel: false,
  },
  createdAt: '2026-01-12T09:00:00Z',
  updatedAt: MAIN_SHELL_VISUAL_NOW,
}

export const mainShellCurrencies = {
  silver: 1_250_764,
  gold: mainShellCharacter.gold,
  marks: mainShellCharacter.arenaMarks,
  credits: 2450,
} as const

export const mainShellLocation: LocationResponse = {
  id: 'visual-market',
  code: 'MARKET',
  name: 'The Trade District',
  description: 'A crowded district where merchants, mercenaries and criminals cross paths. Everything has a price.',
  safety: 'SAFE',
  region: 'Blackstone',
  recommendedLevelMin: 40,
  recommendedLevelMax: 55,
  actions: ['INSPECT', 'MOVE', 'VIEW_NEARBY', 'BROWSE_MARKET', 'CREATE_LISTING', 'BUY_ITEM', 'TALK_NPCS', 'START_EXPEDITION', 'VIEW_CHAT'],
}

export const mainShellCombatLocation: LocationResponse = {
  id: 'visual-blackroad',
  code: 'NORTH_ROAD',
  name: 'The Blackroad',
  description: 'A rain-soaked cut through the pines. Bandits wait where the lanterns fail.',
  safety: 'DANGEROUS',
  region: 'Blackstone',
  recommendedLevelMin: 40,
  recommendedLevelMax: 50,
  actions: ['INSPECT', 'MOVE', 'SEARCH_ENCOUNTER', 'START_EXPEDITION'],
}

export const mainShellDestinations: DestinationResponse[] = [
  { id: 'visual-tavern', code: 'TAVERN', name: 'The Tavern', safety: 'SAFE', recommendedLevelMin: 1, recommendedLevelMax: 60 },
  { id: 'visual-blackroad', code: 'NORTH_ROAD', name: 'The Blackroad', safety: 'DANGEROUS', recommendedLevelMin: 40, recommendedLevelMax: 50 },
  { id: 'visual-arena', code: 'ARENA', name: 'The Arena', safety: 'DANGEROUS', recommendedLevelMin: 20, recommendedLevelMax: 60 },
  { id: 'visual-ruins', code: 'ANCIENT_RUINS', name: 'Old Watch Ruins', safety: 'DANGEROUS', recommendedLevelMin: 42, recommendedLevelMax: 55 },
]

export const mainShellNearby: NearbyCharacterResponse[] = [
  { id: 'near-mira', name: 'Mira Calden', level: 44, avatarCode: 'female_veiled' },
  { id: 'near-osric', name: 'Osric Vale', level: 51, avatarCode: 'male_unyielding' },
  { id: 'near-brann', name: 'Brann Holt', level: 39, avatarCode: 'male_iron_vow' },
]

const equippedItems: InventoryItemResponse[] = [
  item({
    id: 'eq-helm',
    code: 'IRON_HELM',
    name: 'Iron Helm',
    type: 'ARMOR',
    equipmentSlot: 'HEAD',
    rarity: 'RARE',
  }),
  item({
    id: 'eq-chest',
    code: 'IRON_PLATE',
    name: 'Iron Plate',
    type: 'ARMOR',
    equipmentSlot: 'CHEST',
    rarity: 'EPIC',
    armorValue: 28,
    rolledArmorValue: 28,
  }),
  item({
    id: 'eq-hands',
    code: 'LEATHER_GLOVES',
    name: 'Leather Gloves',
    type: 'ARMOR',
    equipmentSlot: 'HANDS',
    rarity: 'UNCOMMON',
    armorCategory: 'LIGHT',
  }),
  item({
    id: 'eq-legs',
    code: 'LEATHER_LEGGINGS',
    name: 'Leather Leggings',
    type: 'ARMOR',
    equipmentSlot: 'LEGS',
    rarity: 'UNCOMMON',
    armorCategory: 'LIGHT',
  }),
  item({
    id: 'eq-feet',
    code: 'LEATHER_BOOTS',
    name: 'Leather Boots',
    type: 'ARMOR',
    equipmentSlot: 'FEET',
    rarity: 'RARE',
    armorCategory: 'LIGHT',
  }),
  item({
    id: 'eq-weapon',
    code: 'ARMING_SWORD',
    name: "Knight's Arming Sword",
    type: 'WEAPON',
    equipmentSlot: 'MAIN_HAND',
    rarity: 'EPIC',
    weaponFamily: 'SWORD',
    armorCategory: null,
    rolledWeaponDamage: 34,
    rolledArmorValue: null,
    weaponDamage: 34,
    armorValue: null,
  }),
  item({
    id: 'eq-shield',
    code: 'WOODEN_BUCKLER',
    name: "Knight's Bulwark Shield",
    type: 'ARMOR',
    equipmentSlot: 'OFF_HAND',
    rarity: 'RARE',
  }),
  item({
    id: 'eq-amulet',
    code: 'COPPER_AMULET',
    name: 'Copper Amulet',
    type: 'ACCESSORY',
    equipmentSlot: 'AMULET',
    rarity: 'UNCOMMON',
    armorCategory: null,
  }),
  item({
    id: 'eq-ring',
    code: 'COPPER_RING',
    name: 'Copper Ring',
    type: 'ACCESSORY',
    equipmentSlot: 'RING',
    rarity: 'RARE',
    armorCategory: null,
  }),
]

const bagItems: InventoryItemResponse[] = [
  item({
    id: 'bag-sword',
    code: 'STEEL_LONGSWORD',
    name: 'Steel Longsword',
    type: 'WEAPON',
    equipmentSlot: 'MAIN_HAND',
    rarity: 'RARE',
    equipped: false,
    weaponFamily: 'SWORD',
    armorCategory: null,
    rolledWeaponDamage: 38,
    rolledArmorValue: null,
    weaponDamage: 38,
    armorValue: null,
    comparison: {
      slot: 'MAIN_HAND',
      equippedItemId: 'eq-weapon',
      verdict: 'UPGRADE',
      deltas: [
        { stat: 'Damage', equippedValue: 34, candidateValue: 38, delta: 4 },
      ],
    },
  }),
  item({
    id: 'bag-potion',
    code: 'HEALTH_POTION',
    name: 'Health Potion',
    description: 'Restores a measure of health.',
    type: 'CONSUMABLE',
    equipmentSlot: null,
    rarity: 'COMMON',
    equipped: false,
    canEquip: false,
    usable: true,
    quantity: 8,
    healAmount: 240,
    armorCategory: null,
    rolledArmorValue: null,
    armorValue: null,
    requiredLevel: 1,
    baseValue: 12,
  }),
  item({
    id: 'bag-ore',
    code: 'IRON_ORE',
    name: 'Iron Ore',
    type: 'MATERIAL',
    equipmentSlot: null,
    rarity: 'COMMON',
    equipped: false,
    canEquip: false,
    quantity: 24,
    armorCategory: null,
    rolledArmorValue: null,
    armorValue: null,
    requiredLevel: 1,
    baseValue: 3,
  }),
  item({
    id: 'bag-cloak',
    code: 'TRAVEL_CLOAK',
    name: 'Travel Cloak',
    type: 'ARMOR',
    equipmentSlot: 'CHEST',
    rarity: 'UNCOMMON',
    equipped: false,
    armorCategory: 'LIGHT',
    rolledArmorValue: 9,
    armorValue: 9,
  }),
  item({
    id: 'bag-gem',
    code: 'AMBER_SHARD',
    name: 'Amber Shard',
    type: 'MATERIAL',
    equipmentSlot: null,
    rarity: 'RARE',
    equipped: false,
    canEquip: false,
    quantity: 3,
    armorCategory: null,
    rolledArmorValue: null,
    armorValue: null,
    requiredLevel: 1,
    baseValue: 85,
  }),
]

export const mainShellInventory: InventoryResponse = {
  capacity: 40,
  usedSlots: equippedItems.length + bagItems.length,
  items: [...equippedItems, ...bagItems],
  equipment: {
    slots: {
      HEAD: 'eq-helm',
      CHEST: 'eq-chest',
      HANDS: 'eq-hands',
      LEGS: 'eq-legs',
      FEET: 'eq-feet',
      MAIN_HAND: 'eq-weapon',
      OFF_HAND: 'eq-shield',
      AMULET: 'eq-amulet',
      RING: 'eq-ring',
    },
  },
  derivedStats: mainShellCharacter.derivedStats,
}

export type MainShellExpeditionVisual = {
  id: string
  name: string
  remaining: string
  progressPercent: number
  artCode: string
  rewards: { silver: number; xp: number; marks: number }
}

export const mainShellExpeditions: MainShellExpeditionVisual[] = [
  {
    id: 'exp-north',
    name: 'The Northern Pass',
    remaining: '8h 45m remaining',
    progressPercent: 28,
    artCode: 'NORTH_ROAD',
    rewards: { silver: 8450, xp: 1250, marks: 320 },
  },
  {
    id: 'exp-ruins',
    name: 'Ruins of Vardhelm',
    remaining: '3h 12m remaining',
    progressPercent: 64,
    artCode: 'ANCIENT_RUINS',
    rewards: { silver: 6120, xp: 980, marks: 210 },
  },
]

export type MainShellObjectiveVisual = {
  id: string
  name: string
  current: number
  required: number
}

export const mainShellObjectives: MainShellObjectiveVisual[] = [
  { id: 'obj-quests', name: 'Complete 3 Quests', current: 2, required: 3 },
  { id: 'obj-pvp', name: 'Win 2 PvP Battles', current: 0, required: 2 },
  { id: 'obj-craft', name: 'Craft 1 Item', current: 1, required: 1 },
]

export type MainShellEventVisual = {
  id: string
  name: string
  timing: string
  tone: 'safe' | 'upgrade' | 'danger'
}

export const mainShellEvents: MainShellEventVisual[] = [
  { id: 'evt-loot', name: 'Double Loot Weekend', timing: '2d 8h remaining', tone: 'upgrade' },
  { id: 'evt-arena', name: 'Arena Skirmish', timing: 'Starts in 1h 17m', tone: 'safe' },
  { id: 'evt-boss', name: 'World Boss: Gorthak', timing: 'Starts in 5h 17m', tone: 'danger' },
]

export const mainShellGuild = {
  name: 'Iron Vanguard',
  level: 12,
  xp: 8650,
  xpMax: 15000,
  members: 43,
  memberCap: 50,
  power: 158400,
  territories: 3,
  crestUrl: '/icons/actions/guild.webp',
} as const

function activity(
  id: string,
  type: ActivityType,
  message: string,
  createdAt: string,
): ActivityEntryResponse {
  return { id, type, message, createdAt, readAt: null }
}

export const mainShellActivity: ActivityEntryResponse[] = [
  activity('act-quest', 'QUEST_COMPLETED', 'You completed The Lost Shipment.', '2026-08-16T15:41:00Z'),
  activity('act-silver', 'EXPEDITION_CLAIMED', 'You claimed your The Northern Pass rewards.', '2026-08-16T15:38:20Z'),
  activity('act-auction', 'MARKET_SOLD', 'You sold Iron Plate Boots for 1840 gold.', '2026-08-16T15:21:08Z'),
  activity('act-guild', 'QUEST_OBJECTIVE', 'Guild member Thorgar is now online.', '2026-08-16T15:18:44Z'),
  activity('act-duel', 'DUEL_RESULT', 'You won a duel against Bloodraven.', '2026-08-16T15:12:03Z'),
]

export type MainShellNoticeVisual = {
  id: string
  variant: ActivityRowVariant
  iconType: ActivityType | 'alert'
  primary: string
  secondary?: string
  action?: string
}

export const mainShellClaimable: MainShellNoticeVisual[] = [
  {
    id: 'claim-daily',
    variant: 'reward',
    iconType: 'QUEST_COMPLETED',
    primary: 'Daily reward available',
    action: 'Claim',
  },
  {
    id: 'claim-exp',
    variant: 'completed',
    iconType: 'EXPEDITION_COMPLETED',
    primary: 'Expedition Complete',
    secondary: 'The Northern Pass',
    action: 'Claim',
  },
]

export const mainShellNotifications: MainShellNoticeVisual[] = [
  {
    id: 'note-craft',
    variant: 'reward',
    iconType: 'CRAFTING_CLAIMED',
    primary: 'Crafting Complete',
    secondary: 'Steel Warhammer',
  },
  {
    id: 'note-daily',
    variant: 'system',
    iconType: 'QUEST_ACCEPTED',
    primary: 'Daily Quest Ready',
    secondary: '3 tasks available',
  },
  {
    id: 'note-market',
    variant: 'market',
    iconType: 'MARKET_SALE',
    primary: 'Market Alert',
    secondary: '12 new listings added',
  },
]

export const mainShellAlerts: MainShellNoticeVisual[] = [
  {
    id: 'alert-rift',
    variant: 'warning',
    iconType: 'alert',
    primary: 'Rift Invasion',
    secondary: '30m remaining',
  },
]

export const mainShellChatChannels = [
  { id: 'global', label: 'GLOBAL', art: '/icons/chat/global.webp', unread: 12, active: true },
  { id: 'trade', label: 'TRADE', art: '/icons/chat/trade.webp', unread: 3, active: false },
  { id: 'guild', label: 'GUILD', art: '/icons/chat/guild.webp', unread: 5, active: false },
  { id: 'party', label: 'PARTY', art: '/icons/chat/party.webp', unread: 1, active: false },
] as const

export const mainShellChatMessages: ChatMessageResponse[] = [
  {
    id: 'chat-1',
    characterId: 'c-aria',
    characterName: 'AriaMoon',
    body: 'Anyone up for Moonfang Expedition? Need 1 DPS and 1 Healer.',
    createdAt: '2026-08-16T15:42:18Z',
  },
  {
    id: 'chat-2',
    characterId: 'c-thor',
    characterName: 'Thorgar',
    body: 'I can DPS. lvl 47 Berserker here.',
    createdAt: '2026-08-16T15:42:31Z',
  },
  {
    id: 'chat-3',
    characterId: 'c-lyria',
    characterName: 'Lyria',
    body: 'Healer here, ready to go!',
    createdAt: '2026-08-16T15:42:58Z',
  },
  {
    id: 'chat-4',
    characterId: 'c-iron',
    characterName: 'Ironclad',
    body: "WTS [Knight's Bulwark Shield] 7.5k.",
    createdAt: '2026-08-16T15:43:10Z',
  },
  {
    id: 'chat-5',
    characterId: 'c-system',
    characterName: 'System',
    body: 'Ironclad has listed an item on the market.',
    createdAt: '2026-08-16T15:43:32Z',
  },
  {
    id: 'chat-6',
    characterId: 'c-grim',
    characterName: 'GrimTrader',
    body: 'Buying Essence of Blood 500g each.',
    createdAt: '2026-08-16T15:44:05Z',
  },
  {
    id: 'chat-7',
    characterId: 'c-artino',
    characterName: 'Artino',
    body: 'Anyone seen the Rift Warden spawn yet today?',
    createdAt: '2026-08-16T15:44:27Z',
  },
  {
    id: 'chat-8',
    characterId: 'c-shadow',
    characterName: 'ShadowStep',
    body: 'Yes, east of Blackridge Ruins.',
    createdAt: '2026-08-16T15:45:11Z',
  },
]

export const mainShellCombat: CombatResponse = {
  id: 'visual-combat',
  encounterId: 'visual-encounter',
  status: 'ACTIVE',
  rulesVersion: 2,
  roundNumber: 5,
  playerHealth: 2840,
  playerMaxHealth: 3850,
  playerStamina: 46,
  playerMaxStamina: 120,
  enemyHealth: 1620,
  enemyMaxHealth: 2400,
  enemyStamina: 38,
  enemyMaxStamina: 90,
  monster: {
    id: 'visual-marauder',
    code: 'BANDIT_VETERAN',
    name: 'Ashfang Marauder',
    level: 44,
    maxHealth: 2400,
    archetype: 'BRUTE',
    tier: 'ELITE',
  },
  potionAvailable: false,
  playerStunned: false,
  playerStatuses: [{ type: 'BATTLE_SHOUT', stacks: 1, remainingRounds: 3 }],
  enemyStatuses: [{ type: 'BLEED', stacks: 2, remainingRounds: 2 }],
  techniques: [
    {
      code: 'RENDING_CHOP',
      name: 'Rending Chop',
      description: 'A wide cut that opens a wound.',
      staminaCost: 16,
      disabledReason: null,
    },
  ],
  coreActionCosts: { quickAttack: 8, heavyAttack: 18, preciseAttack: 12 },
  events: [
    { roundNumber: 4, sequenceNumber: 1, type: 'PLAYER_HIT', message: 'You used Rending Chop. 186 damage.' },
    { roundNumber: 4, sequenceNumber: 2, type: 'ENEMY_HIT', message: 'Ashfang Marauder used Crippling Strike. 94 damage.' },
    { roundNumber: 5, sequenceNumber: 1, type: 'SYSTEM', message: 'Rain-soaked ground. Dodge is reduced.' },
  ],
  rewards: null,
  enemyIntent: { kind: 'HEAVY_ATTACK', label: 'Crippling Strike' },
  actionPreviews: [
    {
      action: 'QUICK_ATTACK',
      techniqueCode: null,
      name: 'Rending Chop',
      description: 'A reliable strike with balanced stamina cost.',
      staminaCost: 8,
      hitChancePercent: 100,
      disabledReason: null,
    },
    {
      action: 'DEFEND',
      techniqueCode: null,
      name: 'Guard Stance',
      description: 'Guard yourself and recover stamina.',
      staminaCost: 0,
      hitChancePercent: 100,
      disabledReason: null,
    },
    {
      action: 'PRECISE_ATTACK',
      techniqueCode: null,
      name: 'Feint Slash',
      description: 'Aim for a weak point. Lower damage, higher accuracy.',
      staminaCost: 12,
      hitChancePercent: 100,
      disabledReason: null,
    },
    {
      action: 'USE_POTION',
      techniqueCode: null,
      name: 'Second Wind',
      description: 'Drink a healing potion.',
      staminaCost: 0,
      hitChancePercent: null,
      disabledReason: 'NO_POTION',
    },
    {
      action: 'RETREAT',
      techniqueCode: null,
      name: 'Flee Encounter',
      description: 'Attempt to leave combat.',
      staminaCost: 0,
      hitChancePercent: null,
      disabledReason: null,
    },
  ],
  possibleLoot: [
    { itemName: 'Gold', dropChancePercent: 100 },
    { itemName: 'Iron Pick', dropChancePercent: 18 },
    { itemName: 'Iron Helm', dropChancePercent: 8 },
    { itemName: 'Copper Ring', dropChancePercent: 12 },
    { itemName: 'Amber Shard', dropChancePercent: 6 },
  ],
}
