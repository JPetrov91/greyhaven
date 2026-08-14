import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { fetchInventory } from '../api/inventory'
import { EmptyState } from '../ui/EmptyState'
import { EquipmentLayout } from '../ui/EquipmentLayout'
import { ErrorState } from '../ui/ErrorState'
import { gameLink } from '../ui/gameNav'
import { LoadingState } from '../ui/LoadingState'
import { Panel } from '../ui/Panel'

export function EquipmentOverviewCard() {
  const inventoryQuery = useQuery({
    queryKey: ['inventory'],
    queryFn: fetchInventory,
    retry: false,
  })

  return (
    <Panel title="Equipment" data-testid="equipment-overview">
      {inventoryQuery.isLoading ? (
        <LoadingState>Loading equipment…</LoadingState>
      ) : inventoryQuery.error ? (
        <ErrorState onRetry={() => void inventoryQuery.refetch()}>Unable to load equipment.</ErrorState>
      ) : inventoryQuery.data ? (
        <>
          <EquipmentLayout
            testId="home-equipment"
            equipment={inventoryQuery.data.equipment}
            items={inventoryQuery.data.items}
          />
          <Link to={gameLink('inventory')} className="btn btn-secondary" data-testid="view-full-equipment">
            View Full Equipment
          </Link>
        </>
      ) : (
        <EmptyState>No equipment loaded.</EmptyState>
      )}
    </Panel>
  )
}
