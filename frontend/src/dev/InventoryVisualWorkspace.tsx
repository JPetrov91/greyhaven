import { useMemo, useState } from 'react'
import type { ItemRarity } from '../api/types'
import { Button } from '../ui/Button'
import { ComingLaterButton } from '../ui/ComingLater'
import { EmptyState } from '../ui/EmptyState'
import { Field } from '../ui/Field'
import { InventoryEmptySlot, InventoryItemSlot } from '../ui/InventoryItemSlot'
import { InventoryItemRow } from '../ui/InventoryItemRow'
import { ItemDetail } from '../ui/ItemDetail'
import { comparisonLabel, shouldShowItemComparison, verdictTone } from '../ui/itemCopy'
import { Panel } from '../ui/Panel'
import { StatRow } from '../ui/StatRow'
import { StatusBadge } from '../ui/StatusBadge'
import { Tabs } from '../ui/Tabs'
import { mainShellInventory } from './mainShellVisualFixture'

type Category = 'ALL' | 'EQUIPMENT' | 'CONSUMABLE' | 'MATERIAL'
type ViewMode = 'grid' | 'list'

export function InventoryVisualWorkspace() {
  const inventory = mainShellInventory
  const [category, setCategory] = useState<Category>('ALL')
  const [search, setSearch] = useState('')
  const [rarityFilter, setRarityFilter] = useState<ItemRarity | ''>('')
  const [viewMode, setViewMode] = useState<ViewMode>('grid')
  const [selectedId, setSelectedId] = useState(inventory.items.find((entry) => !entry.equipped)?.id ?? inventory.items[0]?.id)

  const visibleItems = useMemo(() => {
    return inventory.items.filter((entry) => {
      if (category === 'EQUIPMENT' && entry.type !== 'WEAPON' && entry.type !== 'ARMOR' && entry.type !== 'ACCESSORY') {
        return false
      }
      if (category === 'CONSUMABLE' && entry.type !== 'CONSUMABLE') {
        return false
      }
      if (category === 'MATERIAL' && entry.type !== 'MATERIAL') {
        return false
      }
      if (rarityFilter && entry.rarity !== rarityFilter) {
        return false
      }
      if (search && !entry.displayName.toLowerCase().includes(search.toLowerCase())) {
        return false
      }
      return true
    })
  }, [category, inventory.items, rarityFilter, search])

  const selected = inventory.items.find((entry) => entry.id === selectedId) ?? null
  const emptySlots = Math.max(0, inventory.capacity - visibleItems.length)

  return (
    <div className="ms-screen">
      <Panel className="inventory-panel" aria-label="Inventory" data-testid="inventory-panel" title="Inventory">
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
              ]}
            />
            <div className="inventory-filters">
              <Field label="Search" className="inventory-search-field">
                <input
                  type="search"
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                  data-testid="inventory-search"
                  placeholder="Search items"
                />
              </Field>
              <Field label="Rarity">
                <select
                  value={rarityFilter}
                  onChange={(event) => setRarityFilter(event.target.value as ItemRarity | '')}
                  data-testid="inventory-rarity-filter"
                >
                  <option value="">All</option>
                  <option value="COMMON">Common</option>
                  <option value="UNCOMMON">Uncommon</option>
                  <option value="RARE">Rare</option>
                  <option value="EPIC">Epic</option>
                </select>
              </Field>
              <div className="inventory-view-toggle" role="group" aria-label="Inventory view">
                <button
                  type="button"
                  className="inventory-view-btn"
                  data-testid="inventory-view-grid"
                  aria-pressed={viewMode === 'grid'}
                  onClick={() => setViewMode('grid')}
                >
                  Grid
                </button>
                <button
                  type="button"
                  className="inventory-view-btn"
                  data-testid="inventory-view-list"
                  aria-pressed={viewMode === 'list'}
                  onClick={() => setViewMode('list')}
                >
                  List
                </button>
              </div>
            </div>
            {visibleItems.length === 0 ? (
              <EmptyState testId="inventory-empty">No items match these filters.</EmptyState>
            ) : viewMode === 'grid' ? (
              <ul className="inventory-grid" data-testid="inventory-list">
                {visibleItems.map((entry) => (
                  <InventoryItemSlot
                    key={entry.id}
                    item={entry}
                    selected={selectedId === entry.id}
                    onSelect={() => setSelectedId(entry.id)}
                  />
                ))}
                {Array.from({ length: Math.min(emptySlots, 8) }, (_, index) => (
                  <InventoryEmptySlot key={`empty-${index}`} />
                ))}
              </ul>
            ) : (
              <ul className="inventory-list" data-testid="inventory-list">
                {visibleItems.map((entry) => (
                  <InventoryItemRow
                    key={entry.id}
                    item={entry}
                    selected={selectedId === entry.id}
                    onSelect={() => setSelectedId(entry.id)}
                  />
                ))}
              </ul>
            )}
            <p className="type-meta" data-testid="inventory-capacity">
              {inventory.usedSlots} / {inventory.capacity} slots
            </p>
          </div>
          <aside className="inventory-inspector" aria-label="Selected item">
            {selected ? (
              <>
                <ItemDetail item={selected} showComparison={false} showIcon valueLabel="Base value" />
                {shouldShowItemComparison(selected) && selected.comparison ? (
                  <div className="item-comparison inventory-compare" data-testid={`comparison-${selected.code}`}>
                    <p className="item-comparison-heading">
                      <span>Equipped comparison</span>
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
                  {selected.equipmentSlot ? (
                    <Button type="button" className="inventory-action-primary">
                      {selected.equipped ? 'Unequip' : 'Equip'}
                    </Button>
                  ) : null}
                  {selected.usable ? (
                    <Button type="button" className="inventory-action-primary">
                      Use
                    </Button>
                  ) : null}
                  <Button type="button" variant="secondary">
                    Sell Now
                  </Button>
                  <ComingLaterButton className="inventory-later">Move to Stash</ComingLaterButton>
                </div>
              </>
            ) : (
              <EmptyState>Select an item to inspect it.</EmptyState>
            )}
          </aside>
        </div>
      </Panel>
    </div>
  )
}
