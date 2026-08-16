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

export function rewardChips(rewards: QuestRewardResponse[]): string[] {
  return rewards
    .map((reward) => {
      if (reward.kind === 'XP') {
        return `${reward.amount} XP`
      }
      if (reward.kind === 'GOLD') {
        return `${reward.amount} Gold`
      }
      if (reward.kind === 'ITEM') {
        return reward.itemName ?? reward.itemCode ?? 'Item'
      }
      return reward.unlockCode
    })
    .filter((value): value is string => Boolean(value))
}

export function questArtworkUrl(quest: Pick<QuestResponse, 'artworkKey' | 'objectiveLocationCode'>): string | undefined {
  const key = quest.artworkKey ?? quest.objectiveLocationCode
  if (!key) {
    return undefined
  }
  return locationArtUrl(key)
}

export function boardEntryDisabled(entry: QuestBoardEntryResponse): boolean {
  return entry.listState === 'UNAVAILABLE' || entry.listState === 'COMPLETED'
}
