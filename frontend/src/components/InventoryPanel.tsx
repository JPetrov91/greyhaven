import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useSearchParams } from 'react-router-dom'
import { equipItem, fetchInventory, unequipItem, useItem } from '../api/inventory'
import { ApiError } from '../api/client'
import type { EquipmentSlot, InventoryItemResponse, ItemRarity, ItemType } from '../api/types'
import { Button } from '../ui/Button'
import { EmptyState } from '../ui/EmptyState'
import { EquipmentLayout } from '../ui/EquipmentLayout'
import { EQUIPMENT_SLOTS, SLOT_LABELS } from '../ui/equipmentSlots'
import { ErrorState } from '../ui/ErrorState'
import { Field } from '../ui/Field'
import { ItemCard } from '../ui/ItemCard'
import { LoadingState } from '../ui/LoadingState'
import { Panel } from '../ui/Panel'
import { StatRow } from '../ui/StatRow'
import { Tabs } from '../ui/Tabs'
import { useToast } from '../ui/ToastRegion'
import { useUiMode } from '../ui/uiMode'

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

function parseSlot(value: string | null): EquipmentSlot | '' {
  if (value && (EQUIPMENT_SLOTS as string[]).includes(value)) {
    return value as EquipmentSlot
  }
  return ''
}

type Props = {
  onMutated?: () => void
  mutationsDisabled?: boolean
}

export function InventoryPanel({ onMutated, mutationsDisabled = false }: Props) {
  const queryClient = useQueryClient()
  const { notify } = useToast()
  const uiMode = useUiMode()
  const [searchParams, setSearchParams] = useSearchParams()
  const [sort, setSort] = useState('name')
  const [typeFilter, setTypeFilter] = useState<ItemType | ''>('')
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const slotFilter = parseSlot(searchParams.get('slot'))

  function setSlotFilter(slot: EquipmentSlot | '') {
    setSearchParams(
      (current) => {
        const next = new URLSearchParams(current)
        if (slot) {
          next.set('slot', slot)
        } else {
          next.delete('slot')
        }
        return next
      },
      { replace: true },
    )
  }

  const inventoryQuery = useQuery({
    queryKey: ['inventory'],
    queryFn: fetchInventory,
    retry: false,
  })

  const invalidateGameplay = async (message?: string) => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['inventory'] }),
      queryClient.invalidateQueries({ queryKey: ['character'] }),
    ])
    onMutated?.()
    if (message) {
      notify(message)
    }
  }

  const equipMutation = useMutation({
    mutationFn: equipItem,
    onSuccess: () => void invalidateGameplay('Item equipped.'),
  })

  const unequipMutation = useMutation({
    mutationFn: unequipItem,
    onSuccess: () => void invalidateGameplay('Item unequipped.'),
  })

  const useMutationHook = useMutation({
    mutationFn: useItem,
    onSuccess: () => void invalidateGameplay('Item used.'),
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

  const panelProps = {
    className: 'game-column inventory-panel',
    'aria-label': 'Inventory',
    id: 'inventory',
    'data-testid': 'inventory-panel',
    title: 'Inventory',
  } as const

  if (inventoryQuery.isLoading) {
    return (
      <Panel {...panelProps}>
        <LoadingState>Loading inventory…</LoadingState>
      </Panel>
    )
  }

  if (inventoryQuery.error instanceof ApiError) {
    return (
      <Panel {...panelProps}>
        <ErrorState onRetry={() => void inventoryQuery.refetch()}>{inventoryQuery.error.message}</ErrorState>
      </Panel>
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
    <Panel {...panelProps}>
      <p className="muted" data-testid="inventory-capacity">
        {inventory.usedSlots} / {inventory.capacity} slots
      </p>
      {mutationsDisabled ? <p className="muted">Inventory changes are unavailable during combat.</p> : null}

      <div className="inventory-section">
        <h3>Equipment</h3>
        <EquipmentLayout
          includeSlotTestIds
          compact={uiMode === 'compact'}
          equipment={inventory.equipment}
          items={inventory.items}
          onSlotClick={(slot) => setSlotFilter(slotFilter === slot ? '' : slot)}
        />
        <dl className="derived-stats" data-testid="derived-stats">
          <StatRow label="Damage" value={inventory.derivedStats.physicalDamage} />
          <StatRow label="Armor" value={inventory.derivedStats.armor} />
          <StatRow label="Accuracy" value={inventory.derivedStats.accuracy} />
          <StatRow label="Dodge" value={inventory.derivedStats.dodge} />
          <StatRow label="Crit" value={`${inventory.derivedStats.criticalChance}%`} />
        </dl>
      </div>

      <div className="inventory-section">
        <h3>Items</h3>
        <Tabs<ItemType | ''>
          kind="filters"
          testId="inventory-type-filter"
          label="Item type"
          value={typeFilter}
          onChange={setTypeFilter}
          tabs={[
            { id: '', label: 'All' },
            { id: 'WEAPON', label: 'Weapon' },
            { id: 'ARMOR', label: 'Armor' },
            { id: 'ACCESSORY', label: 'Accessory' },
            { id: 'CONSUMABLE', label: 'Consumable' },
            { id: 'MATERIAL', label: 'Material' },
          ]}
        />
        <div className="inventory-filters">
          <Field label="Sort">
            <select value={sort} onChange={(event) => setSort(event.target.value)} data-testid="inventory-sort">
              <option value="name">Name</option>
              <option value="rarity">Rarity</option>
              <option value="type">Type</option>
              <option value="slot">Slot</option>
            </select>
          </Field>
          <Field label="Slot">
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
          </Field>
        </div>
        {actionError ? <p className="form-error">{actionError}</p> : null}
        {visibleItems.length === 0 ? (
          <EmptyState>Your pack is empty.</EmptyState>
        ) : (
          <ul className="inventory-list" data-testid="inventory-list">
            {visibleItems.map((item) => {
              const selected = selectedId === item.id
              const equippedId = item.comparison?.equippedItemId
              const equippedName = equippedId
                ? (inventory.items.find((entry) => entry.id === equippedId)?.displayName ?? null)
                : null
              return (
                <ItemCard
                  key={item.id}
                  item={item}
                  selected={selected}
                  equippedName={equippedName}
                  onSelect={() => setSelectedId(item.id === selectedId ? null : item.id)}
                  actions={
                    <>
                      {item.equipmentSlot !== null ? (
                        item.equipped ? (
                          <Button
                            type="button"
                            data-testid={`unequip-${item.code}`}
                            disabled={mutationsDisabled || unequipMutation.isPending}
                            onClick={() => unequipMutation.mutate(item.id)}
                          >
                            Unequip
                          </Button>
                        ) : (
                          <Button
                            type="button"
                            data-testid={`equip-${item.code}`}
                            disabled={mutationsDisabled || equipMutation.isPending || !item.canEquip}
                            onClick={() => equipMutation.mutate(item.id)}
                          >
                            Equip
                          </Button>
                        )
                      ) : null}
                      {item.usable ? (
                        <Button
                          type="button"
                          data-testid={`use-${item.code}`}
                          disabled={mutationsDisabled || useMutationHook.isPending}
                          onClick={() => useMutationHook.mutate(item.id)}
                        >
                          Use
                        </Button>
                      ) : null}
                    </>
                  }
                />
              )
            })}
          </ul>
        )}
      </div>
    </Panel>
  )
}
