import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { equipItem, fetchInventory, unequipItem, useItem } from '../api/inventory'
import { ApiError } from '../api/client'
import type {
  EquipmentSlot,
  InventoryItemResponse,
  ItemComparisonResponse,
  ItemRarity,
  ItemType,
} from '../api/types'

const EQUIPMENT_SLOTS: EquipmentSlot[] = [
  'HEAD',
  'CHEST',
  'HANDS',
  'LEGS',
  'FEET',
  'MAIN_HAND',
  'OFF_HAND',
  'AMULET',
  'RING',
]

const SLOT_LABELS: Record<EquipmentSlot, string> = {
  HEAD: 'Head',
  CHEST: 'Chest',
  HANDS: 'Hands',
  LEGS: 'Legs',
  FEET: 'Feet',
  MAIN_HAND: 'Main Hand',
  OFF_HAND: 'Off Hand',
  AMULET: 'Amulet',
  RING: 'Ring',
}

function slotTestId(slot: EquipmentSlot): string {
  if (slot === 'MAIN_HAND') {
    return 'equipped-weapon'
  }
  if (slot === 'CHEST') {
    return 'equipped-armor'
  }
  return `equipped-${slot}`
}

function itemStats(item: InventoryItemResponse): string {
  const parts: string[] = []
  if (item.weaponDamage != null) {
    parts.push(`Damage ${item.weaponDamage}`)
  }
  if (item.armorValue != null) {
    parts.push(`Armor ${item.armorValue}`)
  }
  if (item.healAmount != null) {
    parts.push(`Heal ${item.healAmount}`)
  }
  if (parts.length === 0) {
    parts.push('No combat stats')
  }
  return parts.join(' · ')
}

function comparisonLabel(verdict: ItemComparisonResponse['verdict'] | undefined): string {
  if (verdict == null) {
    return 'same'
  }
  return verdict.toLowerCase()
}

function sortItems(items: InventoryItemResponse[], sort: string): InventoryItemResponse[] {
  const copy = [...items]
  copy.sort((left, right) => {
    if (sort === 'rarity') {
      const order: ItemRarity[] = ['EPIC', 'RARE', 'UNCOMMON', 'COMMON']
      return order.indexOf(left.rarity) - order.indexOf(right.rarity)
    }
    if (sort === 'type') {
      return left.type.localeCompare(right.type)
    }
    if (sort === 'slot') {
      return (left.equipmentSlot ?? '').localeCompare(right.equipmentSlot ?? '')
    }
    return left.displayName.localeCompare(right.displayName)
  })
  return copy
}

type Props = {
  /** Called after equip/unequip/use so surrounding gameplay views can refresh. */
  onMutated?: () => void
  mutationsDisabled?: boolean
}

export function InventoryPanel({ onMutated, mutationsDisabled = false }: Props) {
  const queryClient = useQueryClient()
  const [sort, setSort] = useState('name')
  const [typeFilter, setTypeFilter] = useState<ItemType | ''>('')
  const [slotFilter, setSlotFilter] = useState<EquipmentSlot | ''>('')
  const [selectedId, setSelectedId] = useState<string | null>(null)

  const inventoryQuery = useQuery({
    queryKey: ['inventory'],
    queryFn: fetchInventory,
    retry: false,
  })

  const invalidateGameplay = async () => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['inventory'] }),
      queryClient.invalidateQueries({ queryKey: ['character'] }),
    ])
    onMutated?.()
  }

  const equipMutation = useMutation({
    mutationFn: equipItem,
    onSuccess: invalidateGameplay,
  })

  const unequipMutation = useMutation({
    mutationFn: unequipItem,
    onSuccess: invalidateGameplay,
  })

  const useMutationHook = useMutation({
    mutationFn: useItem,
    onSuccess: invalidateGameplay,
  })

  const visibleItems = useMemo(() => {
    const items = inventoryQuery.data?.items ?? []
    return sortItems(
      items.filter((item) => {
        if (typeFilter && item.type !== typeFilter) {
          return false
        }
        if (slotFilter && item.equipmentSlot !== slotFilter) {
          return false
        }
        return true
      }),
      sort,
    )
  }, [inventoryQuery.data?.items, sort, typeFilter, slotFilter])

  if (inventoryQuery.isLoading) {
    return (
      <section
        className="game-column inventory-panel"
        aria-label="Inventory"
        id="inventory"
        data-testid="inventory-panel"
      >
        <h2>Inventory</h2>
        <p className="muted">Loading inventory…</p>
      </section>
    )
  }

  if (inventoryQuery.error instanceof ApiError) {
    return (
      <section
        className="game-column inventory-panel"
        aria-label="Inventory"
        id="inventory"
        data-testid="inventory-panel"
      >
        <h2>Inventory</h2>
        <p className="form-error" role="alert">
          {inventoryQuery.error.message}
        </p>
        <button type="button" className="travel-button" onClick={() => void inventoryQuery.refetch()}>
          Retry
        </button>
      </section>
    )
  }

  const inventory = inventoryQuery.data
  if (!inventory) {
    return null
  }

  const actionError =
    (equipMutation.error instanceof ApiError && equipMutation.error.message) ||
    (unequipMutation.error instanceof ApiError && unequipMutation.error.message) ||
    (useMutationHook.error instanceof ApiError && useMutationHook.error.message) ||
    null

  return (
    <section
      className="game-column inventory-panel"
      aria-label="Inventory"
      id="inventory"
      data-testid="inventory-panel"
    >
      <h2>Inventory</h2>
      <p className="muted" data-testid="inventory-capacity">
        {inventory.usedSlots} / {inventory.capacity} slots
      </p>
      {mutationsDisabled ? <p className="muted">Inventory changes are unavailable during combat.</p> : null}

      <div className="inventory-section">
        <h3>Equipment</h3>
        <dl className="equipment-grid" data-testid="equipment-summary">
          {EQUIPMENT_SLOTS.map((slot) => {
            const equippedId = inventory.equipment.slots[slot]
            const equippedItem = inventory.items.find((item) => item.id === equippedId)
            return (
              <div key={slot}>
                <dt>{SLOT_LABELS[slot]}</dt>
                <dd
                  data-testid={slotTestId(slot)}
                  className={equippedItem ? `rarity rarity-${equippedItem.rarity.toLowerCase()}` : undefined}
                >
                  {equippedItem?.displayName ?? 'Empty'}
                </dd>
              </div>
            )
          })}
        </dl>
        <dl className="derived-stats" data-testid="derived-stats">
          <div>
            <dt>Damage</dt>
            <dd>{inventory.derivedStats.physicalDamage}</dd>
          </div>
          <div>
            <dt>Armor</dt>
            <dd>{inventory.derivedStats.armor}</dd>
          </div>
          <div>
            <dt>Accuracy</dt>
            <dd>{inventory.derivedStats.accuracy}</dd>
          </div>
          <div>
            <dt>Dodge</dt>
            <dd>{inventory.derivedStats.dodge}</dd>
          </div>
          <div>
            <dt>Crit</dt>
            <dd>{inventory.derivedStats.criticalChance}%</dd>
          </div>
        </dl>
      </div>

      <div className="inventory-section">
        <h3>Items</h3>
        <div className="inventory-filters">
          <label>
            Sort
            <select value={sort} onChange={(event) => setSort(event.target.value)} data-testid="inventory-sort">
              <option value="name">Name</option>
              <option value="rarity">Rarity</option>
              <option value="type">Type</option>
              <option value="slot">Slot</option>
            </select>
          </label>
          <label>
            Type
            <select
              value={typeFilter}
              onChange={(event) => setTypeFilter(event.target.value as ItemType | '')}
              data-testid="inventory-type-filter"
            >
              <option value="">All</option>
              <option value="WEAPON">Weapon</option>
              <option value="ARMOR">Armor</option>
              <option value="ACCESSORY">Accessory</option>
              <option value="CONSUMABLE">Consumable</option>
              <option value="MATERIAL">Material</option>
            </select>
          </label>
          <label>
            Slot
            <select
              value={slotFilter}
              onChange={(event) => setSlotFilter(event.target.value as EquipmentSlot | '')}
              data-testid="inventory-slot-filter"
            >
              <option value="">All</option>
              {EQUIPMENT_SLOTS.map((slot) => (
                <option key={slot} value={slot}>
                  {SLOT_LABELS[slot]}
                </option>
              ))}
            </select>
          </label>
        </div>
        {actionError ? <p className="form-error">{actionError}</p> : null}
        {visibleItems.length === 0 ? (
          <p className="muted">Your pack is empty.</p>
        ) : (
          <ul className="inventory-list" data-testid="inventory-list">
            {visibleItems.map((item) => {
              const selected = selectedId === item.id
              const comparisonClass =
                item.comparison == null ? '' : `comparison-${comparisonLabel(item.comparison.verdict)}`
              return (
                <li
                  key={item.id}
                  data-testid={`inventory-item-${item.code}`}
                  className={[
                    item.equipped ? 'inventory-item-equipped' : '',
                    item.equipmentSlot && !item.canEquip ? 'inventory-item-unusable' : '',
                    comparisonClass,
                  ]
                    .filter(Boolean)
                    .join(' ')}
                >
                  <button
                    type="button"
                    className="inventory-item-select"
                    onClick={() => setSelectedId(item.id === selectedId ? null : item.id)}
                  >
                    <div className="inventory-item-main">
                      <strong>{item.displayName}</strong>
                      <span className={`rarity rarity-${item.rarity.toLowerCase()}`}>{item.rarity}</span>
                    </div>
                  </button>
                  <p className="inventory-item-meta">
                    {item.type}
                    {item.weaponFamily ? ` · ${item.weaponFamily}` : ''}
                    {item.armorCategory ? ` · ${item.armorCategory}` : ''}
                    {item.twoHanded ? ' · Two-handed' : ''}
                    {` · Qty ${item.quantity}`}
                    {item.equipped ? ' · Equipped' : ''}
                    {item.listedQuantity > 0 ? ` · Listed ${item.listedQuantity}` : ''}
                    {item.legacy ? ' · Legacy' : ''}
                    {item.equipmentSlot && !item.canEquip ? ' · Unusable' : ''}
                  </p>
                  <p className="inventory-item-stats">{itemStats(item)}</p>
                  <p className="inventory-item-meta">
                    Req L{item.requiredLevel} STR {item.requiredStrength} AGI {item.requiredAgility} END{' '}
                    {item.requiredEndurance} PER {item.requiredPerception}
                  </p>
                  {item.affixes.length > 0 ? (
                    <ul className="affix-list">
                      {item.affixes.map((affix) => (
                        <li key={`${affix.kind}-${affix.code}-${affix.magnitude}`}>
                          {affix.displayName} ({affix.stat} {affix.magnitude})
                        </li>
                      ))}
                    </ul>
                  ) : null}
                  {selected && item.comparison ? (
                    <div className="item-comparison" data-testid={`comparison-${item.code}`}>
                      <p>
                        vs {SLOT_LABELS[item.comparison.slot]} ({comparisonLabel(item.comparison.verdict)})
                      </p>
                      <ul>
                        {item.comparison.deltas.map((delta) => (
                          <li key={delta.stat}>
                            {delta.stat} {delta.delta > 0 ? '+' : ''}
                            {delta.delta}
                          </li>
                        ))}
                      </ul>
                    </div>
                  ) : null}
                  <div className="inventory-item-actions">
                    {item.equipmentSlot !== null ? (
                      item.equipped ? (
                        <button
                          type="button"
                          className="travel-button"
                          data-testid={`unequip-${item.code}`}
                          disabled={mutationsDisabled || unequipMutation.isPending}
                          onClick={() => unequipMutation.mutate(item.id)}
                        >
                          Unequip
                        </button>
                      ) : (
                        <button
                          type="button"
                          className="travel-button"
                          data-testid={`equip-${item.code}`}
                          disabled={mutationsDisabled || equipMutation.isPending || !item.canEquip}
                          onClick={() => equipMutation.mutate(item.id)}
                        >
                          Equip
                        </button>
                      )
                    ) : null}
                    {item.usable ? (
                      <button
                        type="button"
                        className="travel-button"
                        data-testid={`use-${item.code}`}
                        disabled={mutationsDisabled || useMutationHook.isPending}
                        onClick={() => useMutationHook.mutate(item.id)}
                      >
                        Use
                      </button>
                    ) : null}
                  </div>
                </li>
              )
            })}
          </ul>
        )}
      </div>
    </section>
  )
}
