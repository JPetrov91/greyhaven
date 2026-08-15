import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { fetchCharacter } from '../api/character'
import { equipItem, fetchInventory, unequipItem } from '../api/inventory'
import type { EquipmentSlot, InventoryItemResponse } from '../api/types'
import { Button } from '../ui/Button'
import { ComingLaterButton } from '../ui/ComingLater'
import { EmptyState } from '../ui/EmptyState'
import { EquipmentLayout } from '../ui/EquipmentLayout'
import { ErrorState } from '../ui/ErrorState'
import { isLiveEquipmentSlot, type DesignEquipmentSlot, type FutureEquipmentSlot } from '../ui/equipmentSlots'
import { ItemDetail } from '../ui/ItemDetail'
import { ItemIcon } from '../ui/itemIcons'
import { shouldShowItemComparison } from '../ui/itemCopy'
import { LoadingState } from '../ui/LoadingState'
import { Panel } from '../ui/Panel'
import { StatRow } from '../ui/StatRow'
import { useToast } from '../ui/ToastRegion'
import { useUiMode } from '../ui/uiMode'

type Props = {
  mutationsDisabled?: boolean
}

const PLACEHOLDER_RESISTS = [
  { name: 'Fire', icon: '🔥', value: '29%' },
  { name: 'Frost', icon: '❄', value: '32%' },
  { name: 'Lightning', icon: '⚡', value: '21%' },
  { name: 'Poison', icon: '☠', value: '27%' },
  { name: 'Shadow', icon: '◈', value: '28%' },
  { name: 'Holy', icon: '✦', value: '23%' },
] as const

export function EquipmentPanel({ mutationsDisabled = false }: Props) {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { notify } = useToast()
  const uiMode = useUiMode()
  const compact = uiMode === 'compact'
  const [selectedSlot, setSelectedSlot] = useState<DesignEquipmentSlot>('MAIN_HAND')
  const [selectedItemId, setSelectedItemId] = useState<string | null>(null)

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

  const invalidateGameplay = async (message?: string) => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['inventory'] }),
      queryClient.invalidateQueries({ queryKey: ['character'] }),
    ])
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

  const panelProps = {
    className: 'game-column equipment-panel',
    'aria-label': 'Equipment',
    id: 'equipment',
    'data-testid': 'equipment-panel',
    title: 'Equipment',
    actions: (
      <div className="equipment-header-controls">
        <div className="equipment-subtabs" role="tablist" aria-label="Equipment views">
          <button type="button" className="tab tab-active" role="tab" aria-selected="true">
            Equipment
          </button>
          <ComingLaterButton className="tab" role="tab" aria-selected={false}>
            Stats
          </ComingLaterButton>
          <ComingLaterButton className="tab" role="tab" aria-selected={false}>
            Loadouts
          </ComingLaterButton>
          <ComingLaterButton className="tab" role="tab" aria-selected={false}>
            Appearance
          </ComingLaterButton>
        </div>
        <ComingLaterButton className="equipment-preset-btn" data-testid="equipment-loadout">
          Preset: Iron Vanguard
          <span className="equipment-preset-edit" aria-hidden="true">
            ✎
          </span>
        </ComingLaterButton>
      </div>
    ),
  } as const

  if (inventoryQuery.isLoading) {
    return (
      <Panel {...panelProps}>
        <LoadingState>Loading equipment…</LoadingState>
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
    return (
      <Panel {...panelProps}>
        <EmptyState>No equipment loaded.</EmptyState>
      </Panel>
    )
  }

  const liveSlot = isLiveEquipmentSlot(selectedSlot) ? selectedSlot : null
  const equippedId = liveSlot ? inventory.equipment.slots[liveSlot] : null
  const equippedItem = equippedId ? inventory.items.find((item) => item.id === equippedId) : undefined
  const inspected =
    (selectedItemId ? inventory.items.find((item) => item.id === selectedItemId) : undefined) ?? equippedItem ?? null

  const candidates = liveSlot
    ? inventory.items.filter(
        (item) => item.equipmentSlot === liveSlot && item.id !== equippedId && item.id !== inspected?.id,
      )
    : []

  function selectLiveSlot(slot: EquipmentSlot) {
    setSelectedSlot(slot)
    setSelectedItemId(null)
  }

  function selectFutureSlot(slot: FutureEquipmentSlot) {
    setSelectedSlot(slot)
    setSelectedItemId(null)
  }

  const actionError =
    (equipMutation.error instanceof ApiError && equipMutation.error.message) ||
    (unequipMutation.error instanceof ApiError && unequipMutation.error.message) ||
    null

  return (
    <Panel {...panelProps}>
      {mutationsDisabled ? <p className="muted">Equipment changes are unavailable during combat.</p> : null}
      <div className="equipment-workspace">
        <EquipmentLayout
          includeSlotTestIds
          includeFutureSlots
          showStage={!compact}
          compact={compact}
          selectedSlot={selectedSlot}
          figureGender={characterQuery.data?.gender}
          equipment={inventory.equipment}
          items={inventory.items}
          onLiveSlotClick={selectLiveSlot}
          onFutureSlotClick={selectFutureSlot}
        />
        <EquipmentInspector
          liveSlot={liveSlot}
          inspected={inspected}
          equippedName={equippedItem?.displayName ?? null}
          candidates={candidates}
          mutationsDisabled={mutationsDisabled}
          equipPending={equipMutation.isPending}
          unequipPending={unequipMutation.isPending}
          actionError={actionError}
          onSelectCandidate={setSelectedItemId}
          onEquip={(id) => equipMutation.mutate(id)}
          onUnequip={(id) => unequipMutation.mutate(id)}
          onOpenInventory={() => {
            if (liveSlot) {
              navigate({ pathname: '/game', hash: 'inventory', search: `?slot=${liveSlot}` })
            }
          }}
        />
      </div>
      <EquipmentFooter
        damage={inventory.derivedStats.physicalDamage}
        armor={inventory.derivedStats.armor}
        accuracy={inventory.derivedStats.accuracy}
        dodge={inventory.derivedStats.dodge}
        crit={inventory.derivedStats.criticalChance}
      />
    </Panel>
  )
}

type InspectorProps = {
  liveSlot: EquipmentSlot | null
  inspected: InventoryItemResponse | null
  equippedName: string | null
  candidates: InventoryItemResponse[]
  mutationsDisabled: boolean
  equipPending: boolean
  unequipPending: boolean
  actionError: string | null
  onSelectCandidate: (id: string) => void
  onEquip: (id: string) => void
  onUnequip: (id: string) => void
  onOpenInventory: () => void
}

function EquipmentInspector({
  liveSlot,
  inspected,
  equippedName,
  candidates,
  mutationsDisabled,
  equipPending,
  unequipPending,
  actionError,
  onSelectCandidate,
  onEquip,
  onUnequip,
  onOpenInventory,
}: InspectorProps) {
  const showCompare = inspected != null && shouldShowItemComparison(inspected)

  return (
    <aside className="equipment-inspector" aria-label="Selected item">
      <div className="equipment-inspector-kickers">
        <p className="equipment-inspector-kicker">Selected Item</p>
        {inspected?.equipped ? <p className="equipment-inspector-equipped">Equipped</p> : null}
      </div>
      {!liveSlot ? (
        <div className="equipment-inspector-empty">
          <EmptyState testId="equipment-slot-locked">This slot is coming later.</EmptyState>
        </div>
      ) : inspected ? (
        <>
          <ItemDetail
            item={inspected}
            equippedName={equippedName}
            showComparison={showCompare}
            showIcon
            variant="equipment"
            showQuantity={false}
            valueLabel="Vendor value"
          />
          {inspected.equipped ? <p className="equipment-currently-equipped">Currently Equipped</p> : null}
          <div className="inventory-inspector-actions">
            {inspected.equipped ? (
              <Button
                type="button"
                className="inventory-action-primary"
                data-testid={`unequip-${inspected.code}`}
                disabled={mutationsDisabled || unequipPending}
                onClick={() => onUnequip(inspected.id)}
              >
                Unequip
              </Button>
            ) : (
              <Button
                type="button"
                className="inventory-action-primary"
                data-testid={`equip-${inspected.code}`}
                disabled={mutationsDisabled || equipPending || !inspected.canEquip}
                onClick={() => onEquip(inspected.id)}
              >
                Equip
              </Button>
            )}
            <ComingLaterButton className="inventory-later" data-testid="equipment-compare">
              Compare
            </ComingLaterButton>
            <div className="equipment-inspector-secondary">
              <ComingLaterButton className="inventory-later" data-testid="equipment-open-stash">
                Open Stash
              </ComingLaterButton>
              <ComingLaterButton className="inventory-later" data-testid="equipment-favorite">
                Favorite
              </ComingLaterButton>
            </div>
            <Button
              type="button"
              variant="secondary"
              className="equipment-open-inventory"
              data-testid="equipment-open-inventory"
              onClick={onOpenInventory}
            >
              Open in Inventory
            </Button>
          </div>
        </>
      ) : (
        <div className="equipment-inspector-empty">
          <EmptyState>Select an equipped item to inspect its stats and effects.</EmptyState>
        </div>
      )}
      {actionError ? <p className="form-error">{actionError}</p> : null}
      {liveSlot && candidates.length > 0 ? (
        <ul className="equipment-candidates" aria-label="Items for this slot">
          {candidates.map((item) => (
            <li key={item.id}>
              <button
                type="button"
                className="equipment-candidate"
                data-testid={`equipment-candidate-${item.code}`}
                onClick={() => onSelectCandidate(item.id)}
              >
                <ItemIcon item={item} className="item-icon item-icon-slot" />
                <span>{item.displayName}</span>
              </button>
            </li>
          ))}
        </ul>
      ) : null}
    </aside>
  )
}

function EquipmentFooter({
  damage,
  armor,
  accuracy,
  dodge,
  crit,
}: {
  damage: number
  armor: number
  accuracy: number
  dodge: number
  crit: number
}) {
  return (
    <div className="equipment-footer">
      <section className="equipment-footer-col" aria-label="Combat stats">
        <h3>Combat</h3>
        <dl className="derived-stats" data-testid="derived-stats">
          <StatRow label="Damage" testId="derived-damage" value={damage} />
          <StatRow label="Armor" testId="derived-armor" value={armor} />
          <StatRow label="Accuracy" value={accuracy} />
          <StatRow label="Dodge" value={dodge} />
          <StatRow label="Crit" value={`${crit}%`} />
        </dl>
      </section>
      <section className="equipment-footer-col equipment-footer-locked" title="Coming later" aria-label="Resistances">
        <h3>
          Resistances <span className="visually-hidden">Coming later</span>
        </h3>
        <dl className="derived-stats equipment-resist-list">
          {PLACEHOLDER_RESISTS.map((row) => (
            <StatRow key={row.name} label={`${row.icon} ${row.name}`} value={row.value} />
          ))}
        </dl>
      </section>
      <section className="equipment-footer-col equipment-footer-locked" title="Coming later" aria-label="Set bonuses">
        <h3>
          Set bonuses <span className="visually-hidden">Coming later</span>
        </h3>
        <div className="equipment-set-list">
          <p className="equipment-set-name">Iron Vanguard (4/5)</p>
          <p className="equipment-set-bonus is-active">(2) +8% Armor</p>
          <p className="equipment-set-bonus is-active">(4) +15% Block Chance</p>
          <p className="equipment-set-name">Guardian's Resolve (2/2)</p>
          <p className="equipment-set-bonus is-active">(2) +10% Stamina</p>
          <p className="equipment-set-bonus is-incomplete">(5) +20% Block Value</p>
        </div>
      </section>
      <section className="equipment-footer-col equipment-footer-locked" title="Coming later" aria-label="Active effects">
        <h3>
          Active effects <span className="visually-hidden">Coming later</span>
        </h3>
        <ul className="equipment-effect-list">
          <li>
            <span className="equipment-effect-icon" aria-hidden="true">
              ⚔
            </span>
            <span>
              <strong>Battle Shout</strong>
              <em>+10% Damage</em>
              <small>29m remaining</small>
            </span>
          </li>
          <li>
            <span className="equipment-effect-icon" aria-hidden="true">
              🛡
            </span>
            <span>
              <strong>Fortified</strong>
              <em>+15% Armor</em>
              <small>18m remaining</small>
            </span>
          </li>
        </ul>
      </section>
    </div>
  )
}
