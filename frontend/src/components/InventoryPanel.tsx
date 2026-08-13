import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { equipItem, fetchInventory, unequipItem, useItem } from '../api/inventory'
import { ApiError } from '../api/client'
import type { InventoryItemResponse } from '../api/types'

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

type Props = {
  /** Called after equip/unequip/use so surrounding gameplay views can refresh. */
  onMutated?: () => void
  mutationsDisabled?: boolean
}

export function InventoryPanel({ onMutated, mutationsDisabled = false }: Props) {
  const queryClient = useQueryClient()

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

  if (inventoryQuery.isLoading) {
    return (
      <section
        className="game-column inventory-panel"
        aria-label="Inventory"
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
        data-testid="inventory-panel"
      >
        <h2>Inventory</h2>
        <p className="form-error">{inventoryQuery.error.message}</p>
      </section>
    )
  }

  const inventory = inventoryQuery.data
  if (!inventory) {
    return null
  }

  const weapon = inventory.items.find((item) => item.id === inventory.equipment.weaponItemId)
  const armor = inventory.items.find((item) => item.id === inventory.equipment.armorItemId)
  const actionError =
    (equipMutation.error instanceof ApiError && equipMutation.error.message) ||
    (unequipMutation.error instanceof ApiError && unequipMutation.error.message) ||
    (useMutationHook.error instanceof ApiError && useMutationHook.error.message) ||
    null

  return (
    <section
      className="game-column inventory-panel"
      aria-label="Inventory"
      data-testid="inventory-panel"
    >
      <h2>Inventory</h2>
      <p className="muted" data-testid="inventory-capacity">
        {inventory.usedSlots} / {inventory.capacity} slots
      </p>
      {mutationsDisabled ? <p className="muted">Inventory changes are unavailable during combat.</p> : null}

      <div className="inventory-section">
        <h3>Equipment</h3>
        <dl className="equipment-summary" data-testid="equipment-summary">
          <div>
            <dt>Weapon</dt>
            <dd data-testid="equipped-weapon">{weapon?.name ?? 'Empty'}</dd>
          </div>
          <div>
            <dt>Armor</dt>
            <dd data-testid="equipped-armor">{armor?.name ?? 'Empty'}</dd>
          </div>
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
        {actionError ? <p className="form-error">{actionError}</p> : null}
        {inventory.items.length === 0 ? (
          <p className="muted">Your pack is empty.</p>
        ) : (
          <ul className="inventory-list" data-testid="inventory-list">
            {inventory.items.map((item) => (
              <li key={item.id} data-testid={`inventory-item-${item.code}`}>
                <div className="inventory-item-main">
                  <strong>{item.name}</strong>
                  <span className={`rarity rarity-${item.rarity.toLowerCase()}`}>{item.rarity}</span>
                </div>
                <p className="inventory-item-meta">
                  {item.type} · Qty {item.quantity}
                  {item.equipped ? ' · Equipped' : ''}
                  {item.listedQuantity > 0 ? ` · Listed ${item.listedQuantity}` : ''}
                </p>
                <p className="inventory-item-stats">{itemStats(item)}</p>
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
                        disabled={mutationsDisabled || equipMutation.isPending}
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
            ))}
          </ul>
        )}
      </div>
    </section>
  )
}
