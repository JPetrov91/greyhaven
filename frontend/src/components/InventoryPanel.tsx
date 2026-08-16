import { useEffect, useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { fetchCharacter } from '../api/character'
import { salvageItem } from '../api/crafting'
import { fetchCurrentLocation } from '../api/world'
import { equipItem, fetchInventory, unequipItem, useItem } from '../api/inventory'
import { sellToMerchant } from '../api/market'
import { ApiError } from '../api/client'
import type { EquipmentSlot, InventoryItemResponse, ItemRarity, ItemType, LocationAction } from '../api/types'
import { Button } from '../ui/Button'
import { ComingLaterButton, ComingLaterChip } from '../ui/ComingLater'
import { EmptyState } from '../ui/EmptyState'
import { EQUIPMENT_SLOTS, SLOT_LABELS } from '../ui/equipmentSlots'
import { ErrorState } from '../ui/ErrorState'
import { Field } from '../ui/Field'
import { SearchInput } from '../ui/SearchInput'
import { Select } from '../ui/Select'
import { TextInput } from '../ui/TextInput'
import { InventoryEmptySlot, InventoryItemSlot } from '../ui/InventoryItemSlot'
import { InventoryItemRow } from '../ui/InventoryItemRow'
import { ItemDetail } from '../ui/ItemDetail'
import { comparisonLabel, shouldShowItemComparison, verdictTone } from '../ui/itemCopy'
import { LoadingState } from '../ui/LoadingState'
import { Panel } from '../ui/Panel'
import { StatRow } from '../ui/StatRow'
import { StatusBadge } from '../ui/StatusBadge'
import { Tabs } from '../ui/Tabs'
import { useToast } from '../ui/ToastRegion'

type Category = 'ALL' | 'EQUIPMENT' | 'CONSUMABLE' | 'MATERIAL' | 'QUEST' | 'JUNK'
type ViewMode = 'grid' | 'list'
type UsableFilter = '' | 'usable' | 'unusable'

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

function matchesCategory(item: InventoryItemResponse, category: Category): boolean {
  if (category === 'ALL') {
    return true
  }
  if (category === 'EQUIPMENT') {
    return item.type === 'WEAPON' || item.type === 'ARMOR' || item.type === 'ACCESSORY'
  }
  if (category === 'CONSUMABLE') {
    return item.type === 'CONSUMABLE'
  }
  if (category === 'MATERIAL') {
    return item.type === 'MATERIAL'
  }
  return false
}

type Props = {
  onMutated?: () => void
  mutationsDisabled?: boolean
}

export function InventoryPanel({ onMutated, mutationsDisabled = false }: Props) {
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const { notify } = useToast()
  const [searchParams, setSearchParams] = useSearchParams()
  const [sort, setSort] = useState('name')
  const [category, setCategory] = useState<Category>('ALL')
  const [typeFilter, setTypeFilter] = useState<ItemType | ''>('')
  const [rarityFilter, setRarityFilter] = useState<ItemRarity | ''>('')
  const [usableFilter, setUsableFilter] = useState<UsableFilter>('')
  const [search, setSearch] = useState('')
  const [viewMode, setViewMode] = useState<ViewMode>('grid')
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const [merchantSellQuantity, setMerchantSellQuantity] = useState(1)
  const [confirmSalvage, setConfirmSalvage] = useState(false)
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

  const characterQuery = useQuery({
    queryKey: ['character'],
    queryFn: fetchCharacter,
    retry: false,
  })

  const locationQuery = useQuery({
    queryKey: ['location'],
    queryFn: fetchCurrentLocation,
    retry: false,
  })

  const invalidateGameplay = async (message?: string) => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['inventory'] }),
      queryClient.invalidateQueries({ queryKey: ['character'] }),
      queryClient.invalidateQueries({ queryKey: ['masteries'] }),
      queryClient.invalidateQueries({ queryKey: ['techniques'] }),
      queryClient.invalidateQueries({ queryKey: ['activity'] }),
      queryClient.invalidateQueries({ queryKey: ['location'] }),
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

  const merchantSellMutation = useMutation({
    mutationFn: ({ itemId, quantity }: { itemId: string; quantity: number }) => sellToMerchant(itemId, quantity),
    onSuccess: (result) =>
      void invalidateGameplay(`Sold ${result.itemName} for ${result.goldAwarded} gold.`),
  })

  const salvageMutation = useMutation({
    mutationFn: salvageItem,
    onSuccess: (result) => {
      setConfirmSalvage(false)
      const yields = result.results.map((line) => `${line.itemName} × ${line.quantity}`).join(', ')
      void invalidateGameplay(`Salvaged ${result.sourceItemName}${yields ? `: ${yields}` : '.'}`)
    },
  })

  const items = inventoryQuery.data?.items ?? []

  const visibleItems = useMemo(() => {
    return sortItems(
      items.filter((item) => {
        if (!matchesCategory(item, category)) {
          return false
        }
        if (typeFilter && item.type !== typeFilter) {
          return false
        }
        if (rarityFilter && item.rarity !== rarityFilter) {
          return false
        }
        if (slotFilter && item.equipmentSlot !== slotFilter) {
          return false
        }
        if (usableFilter === 'usable' && !item.usable) {
          return false
        }
        if (usableFilter === 'unusable' && item.usable) {
          return false
        }
        if (search && !item.displayName.toLowerCase().includes(search.trim().toLowerCase())) {
          return false
        }
        return true
      }),
      sort,
    )
  }, [items, category, typeFilter, rarityFilter, slotFilter, usableFilter, search, sort])

  useEffect(() => {
    if (selectedId && visibleItems.some((item) => item.id === selectedId)) {
      return
    }
    setSelectedId(visibleItems[0]?.id ?? null)
  }, [visibleItems, selectedId])

  useEffect(() => {
    setMerchantSellQuantity(1)
    setConfirmSalvage(false)
  }, [selectedId])

  const panelProps = {
    className: 'game-column inventory-panel',
    'aria-label': 'Inventory',
    id: 'inventory',
    'data-testid': 'inventory-panel',
  } as const
  const panelTitle = (
    <span className="inventory-heading">
      <InventoryGlyph />
      Inventory
    </span>
  )

  if (inventoryQuery.isLoading) {
    return (
      <Panel {...panelProps} title={panelTitle}>
        <LoadingState>Loading inventory…</LoadingState>
      </Panel>
    )
  }

  if (inventoryQuery.error instanceof ApiError) {
    return (
      <Panel {...panelProps} title={panelTitle}>
        <ErrorState onRetry={() => void inventoryQuery.refetch()}>{inventoryQuery.error.message}</ErrorState>
      </Panel>
    )
  }

  const inventory = inventoryQuery.data
  if (!inventory) {
    return null
  }

  const selected = items.find((item) => item.id === selectedId) ?? null
  const equippedPeer =
    selected?.comparison?.equippedItemId != null
      ? (items.find((item) => item.id === selected.comparison?.equippedItemId) ?? null)
      : null
  const showCompare = selected != null && shouldShowItemComparison(selected)
  const atMarket = (locationQuery.data?.actions ?? []).includes('CREATE_LISTING' satisfies LocationAction)
  const atWard = (locationQuery.data?.actions ?? []).includes('SALVAGE' satisfies LocationAction)
  const unreserved = selected ? selected.quantity - selected.listedQuantity : 0
  const canSell = selected != null && !selected.equipped && unreserved > 0
  const merchantOffer = selected?.merchantBuyPrice
  const sellHint =
    selected && !canSell
      ? selected.equipped
        ? 'Unequip this item before selling it.'
        : selected.listedQuantity > 0
          ? 'This stack is already listed.'
          : null
      : !atMarket && canSell
        ? 'Travel to the Market to sell to a merchant or list an item.'
        : null
  const emptySlots =
    visibleItems.length > 0 ? Math.max(0, inventory.capacity - visibleItems.length) : 0

  const actionError =
    (equipMutation.error instanceof ApiError && equipMutation.error.message) ||
    (unequipMutation.error instanceof ApiError && unequipMutation.error.message) ||
    (useMutationHook.error instanceof ApiError && useMutationHook.error.message) ||
    (merchantSellMutation.error instanceof ApiError && merchantSellMutation.error.message) ||
    (salvageMutation.error instanceof ApiError && salvageMutation.error.message) ||
    null

  const emptyMessage =
    category === 'QUEST'
      ? 'No quest items.'
      : category === 'JUNK'
        ? 'No junk items.'
        : visibleItems.length === 0 && items.length > 0
          ? 'No items match these filters.'
          : 'Your pack is empty.'

  return (
    <Panel {...panelProps} title={panelTitle}>
      {mutationsDisabled ? <p className="muted">Inventory changes are unavailable during combat.</p> : null}
      <div className="inventory-workspace">
        <div className="inventory-bag">
          <Tabs<Category>
            kind="filters"
            testId="inventory-type-filter"
            label="Item type"
            value={category}
            onChange={setCategory}
            tabs={[
              { id: 'ALL', label: 'All Items' },
              { id: 'EQUIPMENT', label: 'Equipment' },
              { id: 'CONSUMABLE', label: 'Consumables' },
              { id: 'MATERIAL', label: 'Materials' },
              { id: 'QUEST', label: 'Quest' },
              { id: 'JUNK', label: 'Junk' },
            ]}
          />
          <div className="inventory-filters">
            <Field label="Search" className="inventory-search-field">
              <SearchInput
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                data-testid="inventory-search"
                placeholder="Search items"
              />
            </Field>
            <Field label="Rarity">
              <Select
                value={rarityFilter}
                onChange={(event) => setRarityFilter(event.target.value as ItemRarity | '')}
                data-testid="inventory-rarity-filter"
              >
                <option value="">All</option>
                <option value="COMMON">Common</option>
                <option value="UNCOMMON">Uncommon</option>
                <option value="RARE">Rare</option>
                <option value="EPIC">Epic</option>
              </Select>
            </Field>
            <Field label="Type">
              <Select
                value={typeFilter}
                onChange={(event) => setTypeFilter(event.target.value as ItemType | '')}
                data-testid="inventory-type-select"
              >
                <option value="">All</option>
                <option value="WEAPON">Weapon</option>
                <option value="ARMOR">Armor</option>
                <option value="ACCESSORY">Accessory</option>
                <option value="CONSUMABLE">Consumable</option>
                <option value="MATERIAL">Material</option>
              </Select>
            </Field>
            <Field label="Slot">
              <Select
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
              </Select>
            </Field>
            <Field label="Usable">
              <Select
                value={usableFilter}
                onChange={(event) => setUsableFilter(event.target.value as UsableFilter)}
                data-testid="inventory-usable-filter"
              >
                <option value="">All</option>
                <option value="usable">Usable</option>
                <option value="unusable">Not usable</option>
              </Select>
            </Field>
            <Field label="Sort">
              <Select value={sort} onChange={(event) => setSort(event.target.value)} data-testid="inventory-sort">
                <option value="name">Name</option>
                <option value="rarity">Rarity</option>
                <option value="type">Type</option>
                <option value="slot">Slot</option>
                <option value="recent" disabled>
                  Recent
                </option>
              </Select>
            </Field>
            <div className="inventory-view-toggle" role="group" aria-label="Inventory view">
              <button
                type="button"
                className="inventory-view-btn"
                data-testid="inventory-view-grid"
                aria-label="Grid view"
                aria-pressed={viewMode === 'grid'}
                onClick={() => setViewMode('grid')}
              >
                <GridGlyph />
              </button>
              <button
                type="button"
                className="inventory-view-btn"
                data-testid="inventory-view-list"
                aria-label="List view"
                aria-pressed={viewMode === 'list'}
                onClick={() => setViewMode('list')}
              >
                <ListGlyph />
              </button>
            </div>
          </div>
          {actionError ? <p className="form-error">{actionError}</p> : null}
          {visibleItems.length === 0 ? (
            <EmptyState testId="inventory-empty">{emptyMessage}</EmptyState>
          ) : viewMode === 'grid' ? (
            <ul className="inventory-grid" data-testid="inventory-list">
              {visibleItems.map((item) => (
                <InventoryItemSlot
                  key={item.id}
                  item={item}
                  selected={selectedId === item.id}
                  onSelect={() => setSelectedId(item.id)}
                />
              ))}
              {Array.from({ length: emptySlots }, (_, index) => (
                <InventoryEmptySlot key={`empty-${index}`} />
              ))}
            </ul>
          ) : (
            <ul className="inventory-list" data-testid="inventory-list">
              {visibleItems.map((item) => (
                <InventoryItemRow
                  key={item.id}
                  item={item}
                  selected={selectedId === item.id}
                  onSelect={() => setSelectedId(item.id)}
                />
              ))}
            </ul>
          )}
          <div className="inventory-capacity-row">
            <p className="muted" data-testid="inventory-capacity">
              {inventory.usedSlots} / {inventory.capacity} slots
            </p>
            <ComingLaterButton data-testid="inventory-expand-slots" className="btn btn-secondary" aria-label="Expand capacity">
              +
            </ComingLaterButton>
          </div>
        </div>

        <aside className="inventory-inspector" aria-label="Selected item">
          {selected ? (
            <>
              <ItemDetail item={selected} showComparison={false} showIcon valueLabel="Base value" />
              {merchantOffer != null ? (
                <p className="muted" data-testid={`merchant-offer-${selected.code}`}>
                  Merchant offer: {merchantOffer} Gold
                  {unreserved > 1 ? ` each (${merchantOffer * Math.min(merchantSellQuantity, unreserved)} for qty)` : ''}
                </p>
              ) : null}
              {showCompare && selected.comparison ? (
                <div className="item-comparison inventory-compare" data-testid={`comparison-${selected.code}`}>
                  <p className="item-comparison-heading">
                    <span>
                      Equipped: {equippedPeer?.displayName ?? (selected.comparison.equippedItemId ? 'Equipped' : 'Empty')}
                    </span>
                    <StatusBadge tone={verdictTone(selected.comparison.verdict)}>
                      {comparisonLabel(selected.comparison.verdict)}
                    </StatusBadge>
                  </p>
                  <dl className="stat-list">
                    {selected.comparison.deltas.map((delta) => (
                      <StatRow
                        key={delta.stat}
                        label={delta.stat}
                        value={`${delta.equippedValue} → ${delta.candidateValue}`}
                        delta={delta.delta}
                      />
                    ))}
                  </dl>
                </div>
              ) : null}
              <div className="inventory-inspector-actions">
                {selected.equipmentSlot !== null ? (
                  selected.equipped ? (
                    <Button
                      type="button"
                      className="inventory-action-primary"
                      data-testid={`unequip-${selected.code}`}
                      disabled={mutationsDisabled || unequipMutation.isPending}
                      onClick={() => unequipMutation.mutate(selected.id)}
                    >
                      Unequip
                    </Button>
                  ) : (
                    <Button
                      type="button"
                      className="inventory-action-primary"
                      data-testid={`equip-${selected.code}`}
                      disabled={mutationsDisabled || equipMutation.isPending || !selected.canEquip}
                      onClick={() => equipMutation.mutate(selected.id)}
                    >
                      Equip
                    </Button>
                  )
                ) : null}
                {selected.usable ? (
                  <Button
                    type="button"
                    className="inventory-action-primary"
                    data-testid={`use-${selected.code}`}
                    disabled={mutationsDisabled || useMutationHook.isPending}
                    onClick={() => useMutationHook.mutate(selected.id)}
                  >
                    Use
                  </Button>
                ) : null}
                {unreserved > 1 ? (
                  <Field label="Sell quantity">
                    <TextInput
                      data-testid={`merchant-sell-qty-${selected.code}`}
                      type="number"
                      min={1}
                      max={unreserved}
                      value={merchantSellQuantity}
                      onChange={(event) => setMerchantSellQuantity(Number(event.target.value))}
                    />
                  </Field>
                ) : null}
                <Button
                  type="button"
                  className="inventory-action-primary"
                  data-testid={`merchant-sell-${selected.code}`}
                  disabled={!canSell || mutationsDisabled || merchantSellMutation.isPending || !atMarket}
                  title={!atMarket ? 'Travel to the Market to sell to a merchant.' : undefined}
                  onClick={() =>
                    merchantSellMutation.mutate({
                      itemId: selected.id,
                      quantity: Math.min(Math.max(1, merchantSellQuantity), unreserved),
                    })
                  }
                >
                  Sell Now
                </Button>
                <Button
                  type="button"
                  variant="secondary"
                  data-testid={`sell-${selected.code}`}
                  disabled={!canSell}
                  onClick={() => navigate({ pathname: '/game', search: `?panel=market&listItem=${selected.id}` })}
                >
                  Create Market Listing
                </Button>
                {sellHint ? <p className="muted inventory-sell-hint">{sellHint}</p> : null}
                <div className="inventory-later-row">
                  <ComingLaterButton className="inventory-later" data-testid="inventory-move-stash">
                    Move to Stash
                  </ComingLaterButton>
                  {confirmSalvage ? (
                    <>
                      <p className="muted" data-testid="salvage-confirm">
                        Destroy this item for materials?
                      </p>
                      <Button
                        type="button"
                        data-testid="inventory-salvage"
                        disabled={salvageMutation.isPending || mutationsDisabled}
                        onClick={() => salvageMutation.mutate(selected.id)}
                      >
                        {salvageMutation.isPending ? 'Salvaging…' : 'Confirm salvage'}
                      </Button>
                      <Button type="button" variant="secondary" onClick={() => setConfirmSalvage(false)}>
                        Cancel
                      </Button>
                    </>
                  ) : (
                    <Button
                      type="button"
                      variant="secondary"
                      className="inventory-later"
                      data-testid="inventory-salvage"
                      disabled={
                        mutationsDisabled ||
                        !atWard ||
                        selected.equipped ||
                        selected.listedQuantity > 0 ||
                        (selected.type !== 'WEAPON' && selected.type !== 'ARMOR' && selected.type !== 'ACCESSORY')
                      }
                      title={
                        selected.equipped
                          ? 'Unequip this item before salvaging it.'
                          : selected.listedQuantity > 0
                            ? 'Cancel the market listing before salvaging.'
                            : !atWard
                              ? 'Travel to the Craftsmen Ward to salvage.'
                              : selected.type !== 'WEAPON' && selected.type !== 'ARMOR' && selected.type !== 'ACCESSORY'
                                ? 'Only equipment can be salvaged.'
                                : undefined
                      }
                      onClick={() => setConfirmSalvage(true)}
                    >
                      Salvage
                    </Button>
                  )}
                  <ComingLaterButton className="inventory-later" data-testid="inventory-link-chat">
                    Link in Chat
                  </ComingLaterButton>
                </div>
              </div>
            </>
          ) : (
            <EmptyState>Select an item to inspect it.</EmptyState>
          )}

          <div className="inventory-bag-overview">
            <p className="muted">
              {inventory.usedSlots} / {inventory.capacity} slots
            </p>
            <p className="currency-row">
              <span className="currency-chip" data-testid="inventory-gold">
                Gold <strong>{characterQuery.data?.gold.toLocaleString('en-US') ?? '—'}</strong>
              </span>
              <ComingLaterChip testId="inventory-silver">Silver</ComingLaterChip>
            </p>
            <div className="inventory-later-row">
              <Button type="button" variant="secondary" data-testid="inventory-auto-sort" onClick={() => setSort('rarity')}>
                Auto-Sort
              </Button>
              <ComingLaterButton className="inventory-later" data-testid="inventory-stack-all">
                Stack All
              </ComingLaterButton>
              <ComingLaterButton className="inventory-later" data-testid="inventory-open-stash">
                Open Stash
              </ComingLaterButton>
              <ComingLaterButton className="inventory-later" data-testid="inventory-loadouts">
                Loadouts
              </ComingLaterButton>
            </div>
          </div>
        </aside>
      </div>
    </Panel>
  )
}

function GridGlyph() {
  return (
    <svg viewBox="0 0 16 16" width="14" height="14" aria-hidden="true" focusable="false">
      <rect x="1" y="1" width="6" height="6" fill="none" stroke="currentColor" strokeWidth="1.4" />
      <rect x="9" y="1" width="6" height="6" fill="none" stroke="currentColor" strokeWidth="1.4" />
      <rect x="1" y="9" width="6" height="6" fill="none" stroke="currentColor" strokeWidth="1.4" />
      <rect x="9" y="9" width="6" height="6" fill="none" stroke="currentColor" strokeWidth="1.4" />
    </svg>
  )
}

function ListGlyph() {
  return (
    <svg viewBox="0 0 16 16" width="14" height="14" aria-hidden="true" focusable="false">
      <path d="M1 3h14M1 8h14M1 13h14" fill="none" stroke="currentColor" strokeWidth="1.6" />
    </svg>
  )
}

function InventoryGlyph() {
  return (
    <svg className="inventory-heading-icon" viewBox="0 0 32 32" aria-hidden="true" focusable="false">
      <path
        d="M8.2 12.4h15.6v13.2H8.2Z"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.7"
        strokeLinejoin="round"
      />
      <path
        d="M11.2 12.4V9.6c0-2.8 2.2-4.6 4.8-4.6s4.8 1.8 4.8 4.6v2.8"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.7"
      />
    </svg>
  )
}
