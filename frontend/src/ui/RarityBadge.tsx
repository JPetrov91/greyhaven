import type { ItemRarity } from '../api/types'
import { classNames } from './classNames'
import { formatRarity } from './formatRarity'

type Props = {
  rarity: ItemRarity
  className?: string
}

export function RarityBadge({ rarity, className }: Props) {
  return (
    <span className={classNames('rarity', `rarity-${rarity.toLowerCase()}`, className)}>
      {formatRarity(rarity)}
    </span>
  )
}
