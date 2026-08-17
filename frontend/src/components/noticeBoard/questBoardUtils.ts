import type { QuestBoardEntryResponse, QuestResponse, QuestRewardResponse } from '../../api/types'
import { locationArtUrl } from '../../ui/locationMedia'

export function formatQuestType(value: string | null | undefined): string {
  if (!value) {
    return 'Quest'
  }
  return value
    .toLowerCase()
    .split(/[_\s]+/)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

export function formatDifficulty(value: string | null | undefined): string {
  if (!value) {
    return 'Normal'
  }
  return value.charAt(0) + value.slice(1).toLowerCase()
}

export type RewardChip = {
  key: string
  kind: string
  label: string
  iconUrl?: string
}

export function rewardChips(rewards: QuestRewardResponse[]): RewardChip[] {
  return rewards.flatMap((reward, index) => {
    if (reward.kind === 'XP') {
      return [{
        key: `xp-${index}`,
        kind: 'XP',
        label: String(reward.amount ?? 0),
        iconUrl: '/quests/rewards/xp.png',
      }]
    }
    if (reward.kind === 'GOLD') {
      return [{
        key: `gold-${index}`,
        kind: 'GOLD',
        label: String(reward.amount ?? 0),
        iconUrl: '/quests/rewards/gold.png',
      }]
    }
    const text = reward.kind === 'ITEM' ? reward.itemName ?? reward.itemCode ?? 'Item' : reward.unlockCode
    if (!text) {
      return []
    }
    return [{ key: `other-${index}`, kind: reward.kind, label: text }]
  })
}

const QUEST_ART: Record<string, string> = {
  NORTH_ROAD: '/quests/abandoned_caravan.png',
  abandoned_caravan: '/quests/abandoned_caravan.png',
}

const QUEST_TYPE_ART: Record<string, string> = {
  INVESTIGATION: '/quests/types/investigation.png',
  EXTERMINATION: '/quests/types/extermination.png',
  MAIN: '/quests/types/main.png',
  SIDE: '/quests/types/side.png',
}

export function questTypeArtUrl(questType: string | null | undefined): string {
  if (!questType) {
    return '/quests/types/generic.png'
  }
  return QUEST_TYPE_ART[questType.toUpperCase()] ?? '/quests/types/generic.png'
}

export function questTypeTone(questType: string | null | undefined): string {
  const key = questType?.toUpperCase()
  if (key && key in QUEST_TYPE_ART) {
    return key.toLowerCase()
  }
  return 'generic'
}

export function questArtworkUrl(quest: Pick<QuestResponse, 'artworkKey' | 'objectiveLocationCode'>): string | undefined {
  const key = quest.artworkKey ?? quest.objectiveLocationCode
  if (!key) {
    return undefined
  }
  return QUEST_ART[key] ?? locationArtUrl(key)
}

export function boardEntryDisabled(entry: QuestBoardEntryResponse): boolean {
  return entry.listState === 'UNAVAILABLE' || entry.listState === 'COMPLETED'
}
