import type { ItemRarity } from '../api/types'

export function formatRarity(rarity: ItemRarity): string {
  return rarity.charAt(0) + rarity.slice(1).toLowerCase()
}
