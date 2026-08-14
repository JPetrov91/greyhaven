import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { fetchInventory } from '../api/inventory'
import type { EquipmentSlot } from '../api/types'
import { EmptyState } from '../ui/EmptyState'
import { EquipmentLayout } from '../ui/EquipmentLayout'
import { ErrorState } from '../ui/ErrorState'
import { LoadingState } from '../ui/LoadingState'
import { Panel } from '../ui/Panel'
import { StatRow } from '../ui/StatRow'
import { useUiMode } from '../ui/uiMode'

type Props = {
  mutationsDisabled?: boolean
}

export function EquipmentPanel({ mutationsDisabled = false }: Props) {
  const navigate = useNavigate()
  const uiMode = useUiMode()
  const inventoryQuery = useQuery({
    queryKey: ['inventory'],
    queryFn: fetchInventory,
    retry: false,
  })

  const panelProps = {
    className: 'game-column equipment-panel',
    'aria-label': 'Equipment',
    id: 'equipment',
    'data-testid': 'equipment-panel',
    title: 'Equipment',
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

  function openInventorySlot(slot: EquipmentSlot) {
    navigate({ pathname: '/game', hash: 'inventory', search: `?slot=${slot}` })
  }

  return (
    <Panel {...panelProps}>
      {mutationsDisabled ? <p className="muted">Equipment changes are unavailable during combat.</p> : null}
      <EquipmentLayout
        includeSlotTestIds
        compact={uiMode === 'compact'}
        equipment={inventory.equipment}
        items={inventory.items}
        onSlotClick={openInventorySlot}
      />
      <dl className="derived-stats" data-testid="derived-stats">
        <StatRow label="Damage" testId="derived-damage" value={inventory.derivedStats.physicalDamage} />
        <StatRow label="Armor" testId="derived-armor" value={inventory.derivedStats.armor} />
        <StatRow label="Accuracy" value={inventory.derivedStats.accuracy} />
        <StatRow label="Dodge" value={inventory.derivedStats.dodge} />
        <StatRow label="Crit" value={`${inventory.derivedStats.criticalChance}%`} />
      </dl>
    </Panel>
  )
}
