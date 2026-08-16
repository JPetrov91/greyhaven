import { useState } from 'react'
import type { EquipmentSlot, InventoryItemResponse } from '../api/types'
import { Button } from '../ui/Button'
import { ComingLaterButton } from '../ui/ComingLater'
import { EmptyState } from '../ui/EmptyState'
import { EquipmentLayout } from '../ui/EquipmentLayout'
import { isLiveEquipmentSlot, type DesignEquipmentSlot, type FutureEquipmentSlot } from '../ui/equipmentSlots'
import { ItemDetail } from '../ui/ItemDetail'
import { ItemIcon } from '../ui/itemIcons'
import { shouldShowItemComparison } from '../ui/itemCopy'
import { Panel } from '../ui/Panel'
import { StatRow } from '../ui/StatRow'
import { mainShellCharacter, mainShellInventory } from './mainShellVisualFixture'

const RESISTS = [
  { name: 'Fire', value: '29%' },
  { name: 'Frost', value: '32%' },
  { name: 'Lightning', value: '21%' },
  { name: 'Poison', value: '27%' },
  { name: 'Shadow', value: '28%' },
  { name: 'Holy', value: '23%' },
] as const

export function EquipmentVisualWorkspace() {
  const inventory = mainShellInventory
  const [selectedSlot, setSelectedSlot] = useState<DesignEquipmentSlot>('MAIN_HAND')
  const [selectedItemId, setSelectedItemId] = useState<string | null>(null)

  const liveSlot = isLiveEquipmentSlot(selectedSlot) ? selectedSlot : null
  const equippedId = liveSlot ? inventory.equipment.slots[liveSlot] : null
  const equippedItem = equippedId ? inventory.items.find((entry) => entry.id === equippedId) : undefined
  const inspected =
    (selectedItemId ? inventory.items.find((entry) => entry.id === selectedItemId) : undefined) ?? equippedItem ?? null
  const candidates = liveSlot
    ? inventory.items.filter(
        (entry) => entry.equipmentSlot === liveSlot && entry.id !== equippedId && entry.id !== inspected?.id,
      )
    : []

  return (
    <div className="ms-screen">
      <Panel
        className="equipment-panel"
        aria-label="Equipment"
        data-testid="equipment-panel"
        title="Equipment"
        actions={
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
            </ComingLaterButton>
          </div>
        }
      >
        <div className="equipment-workspace">
          <EquipmentLayout
            includeSlotTestIds
            includeFutureSlots
            showStage
            selectedSlot={selectedSlot}
            figureGender={mainShellCharacter.gender}
            equipment={inventory.equipment}
            items={inventory.items}
            onLiveSlotClick={(slot: EquipmentSlot) => {
              setSelectedSlot(slot)
              setSelectedItemId(null)
            }}
            onFutureSlotClick={(slot: FutureEquipmentSlot) => {
              setSelectedSlot(slot)
              setSelectedItemId(null)
            }}
          />
          <EquipmentVisualInspector
            liveSlot={liveSlot}
            inspected={inspected}
            equippedName={equippedItem?.displayName ?? null}
            candidates={candidates}
            onSelectCandidate={setSelectedItemId}
          />
        </div>
        <div className="equipment-footer">
          <section className="equipment-footer-col" aria-label="Combat stats">
            <h3>Combat</h3>
            <dl className="derived-stats" data-testid="derived-stats">
              <StatRow label="Damage" testId="derived-damage" value={inventory.derivedStats.physicalDamage} />
              <StatRow label="Armor" testId="derived-armor" value={inventory.derivedStats.armor} />
              <StatRow label="Accuracy" value={inventory.derivedStats.accuracy} />
              <StatRow label="Dodge" value={inventory.derivedStats.dodge} />
              <StatRow label="Crit" value={`${inventory.derivedStats.criticalChance}%`} />
            </dl>
          </section>
          <section className="equipment-footer-col" aria-label="Resistances">
            <h3>Resistances</h3>
            <dl className="derived-stats equipment-resist-list">
              {RESISTS.map((row) => (
                <StatRow key={row.name} label={row.name} value={row.value} />
              ))}
            </dl>
          </section>
          <section className="equipment-footer-col" aria-label="Set bonuses">
            <h3>Set bonuses</h3>
            <div className="equipment-set-list">
              <p className="equipment-set-name">Iron Vanguard (4/5)</p>
              <p className="equipment-set-bonus is-active">(2) +8% Armor</p>
              <p className="equipment-set-bonus is-active">(4) +15% Block Chance</p>
            </div>
          </section>
          <section className="equipment-footer-col" aria-label="Active effects">
            <h3>Active effects</h3>
            <ul className="equipment-effect-list">
              <li>
                <span>
                  <strong>Battle Shout</strong>
                  <em>+10% Damage</em>
                  <small>29m remaining</small>
                </span>
              </li>
            </ul>
          </section>
        </div>
      </Panel>
    </div>
  )
}

function EquipmentVisualInspector({
  liveSlot,
  inspected,
  equippedName,
  candidates,
  onSelectCandidate,
}: {
  liveSlot: EquipmentSlot | null
  inspected: InventoryItemResponse | null
  equippedName: string | null
  candidates: InventoryItemResponse[]
  onSelectCandidate: (id: string) => void
}) {
  return (
    <aside className="equipment-inspector" aria-label="Selected item">
      <div className="equipment-inspector-kickers">
        <p className="equipment-inspector-kicker">Selected Item</p>
        {inspected?.equipped ? <p className="equipment-inspector-equipped">Equipped</p> : null}
      </div>
      {!liveSlot ? (
        <EmptyState testId="equipment-slot-locked">This slot is coming later.</EmptyState>
      ) : inspected ? (
        <>
          <ItemDetail
            item={inspected}
            equippedName={equippedName}
            showComparison={shouldShowItemComparison(inspected)}
            showIcon
            variant="equipment"
            showQuantity={false}
            valueLabel="Vendor value"
          />
          <div className="inventory-inspector-actions">
            <Button type="button" className="inventory-action-primary">
              {inspected.equipped ? 'Unequip' : 'Equip'}
            </Button>
            <ComingLaterButton className="inventory-later">Compare</ComingLaterButton>
            <Button type="button" variant="secondary" data-testid="equipment-open-inventory">
              Open in Inventory
            </Button>
          </div>
        </>
      ) : (
        <EmptyState>Select an equipped item to inspect its stats and effects.</EmptyState>
      )}
      {liveSlot && candidates.length > 0 ? (
        <ul className="equipment-candidates" aria-label="Items for this slot">
          {candidates.map((entry) => (
            <li key={entry.id}>
              <button
                type="button"
                className="equipment-candidate"
                data-testid={`equipment-candidate-${entry.code}`}
                onClick={() => onSelectCandidate(entry.id)}
              >
                <ItemIcon item={entry} className="item-icon item-icon-slot" />
                <span>{entry.displayName}</span>
              </button>
            </li>
          ))}
        </ul>
      ) : null}
    </aside>
  )
}
