import type { InventoryItemResponse } from '../api/types'
import { ItemDetail } from './ItemDetail'

type Props = {
  item: InventoryItemResponse
  equippedName?: string | null
}

export { comparisonLabel, verdictTone } from './itemCopy'

export function ItemTooltip({ item, equippedName = null }: Props) {
  return (
    <div className="item-tooltip">
      <ItemDetail item={item} equippedName={equippedName} />
    </div>
  )
}
