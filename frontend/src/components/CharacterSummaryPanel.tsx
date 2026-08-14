import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { allocateAttributes, fetchCharacter, respecCharacter } from '../api/character'
import { fetchInventory } from '../api/inventory'
import { fetchCurrentLocation } from '../api/world'
import { ApiError } from '../api/client'
import type { CharacterResponse } from '../api/types'
import { Button } from '../ui/Button'
import { Dialog } from '../ui/Dialog'
import { EmptyState } from '../ui/EmptyState'
import { EquipmentLayout } from '../ui/EquipmentLayout'
import { ErrorState } from '../ui/ErrorState'
import { LoadingState } from '../ui/LoadingState'
import { Panel } from '../ui/Panel'
import { ProgressBar } from '../ui/ProgressBar'
import { StatRow } from '../ui/StatRow'
import { StatusBadge } from '../ui/StatusBadge'

type AttrKey = 'strength' | 'agility' | 'endurance' | 'perception'

type Props = {
  mutationsDisabled?: boolean
}

function formatXp(value: number): string {
  return value.toLocaleString('en-US')
}

export function CharacterSummaryPanel({ mutationsDisabled = false }: Props) {
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const [allocError, setAllocError] = useState<string | null>(null)
  const [allocating, setAllocating] = useState<AttrKey | null>(null)
  const [respeccing, setRespeccing] = useState(false)
  const [confirmRespec, setConfirmRespec] = useState(false)

  const characterQuery = useQuery({
    queryKey: ['character'],
    queryFn: fetchCharacter,
    retry: false,
  })

  const locationQuery = useQuery({
    queryKey: ['location'],
    queryFn: fetchCurrentLocation,
    retry: false,
    enabled: !!characterQuery.data,
  })

  const inventoryQuery = useQuery({
    queryKey: ['inventory'],
    queryFn: fetchInventory,
    retry: false,
    enabled: !!characterQuery.data,
  })

  async function spendPoint(attribute: AttrKey) {
    setAllocError(null)
    setAllocating(attribute)
    try {
      await allocateAttributes({
        strength: attribute === 'strength' ? 1 : 0,
        agility: attribute === 'agility' ? 1 : 0,
        endurance: attribute === 'endurance' ? 1 : 0,
        perception: attribute === 'perception' ? 1 : 0,
      })
      await queryClient.invalidateQueries({ queryKey: ['character'] })
      await queryClient.invalidateQueries({ queryKey: ['inventory'] })
    } catch (error) {
      setAllocError(error instanceof ApiError ? error.message : 'Unable to allocate attribute.')
    } finally {
      setAllocating(null)
    }
  }

  async function handleRespec() {
    setAllocError(null)
    setRespeccing(true)
    setConfirmRespec(false)
    try {
      await respecCharacter()
      await queryClient.invalidateQueries({ queryKey: ['character'] })
      await queryClient.invalidateQueries({ queryKey: ['inventory'] })
    } catch (error) {
      setAllocError(error instanceof ApiError ? error.message : 'Unable to respec.')
    } finally {
      setRespeccing(false)
    }
  }

  if (characterQuery.isLoading) {
    return (
      <Panel as="aside" className="game-column game-column-left" id="character" title="Character">
        <LoadingState>Loading character…</LoadingState>
      </Panel>
    )
  }

  if (characterQuery.error instanceof ApiError) {
    return (
      <Panel as="aside" className="game-column game-column-left" id="character" title="Character">
        <ErrorState onRetry={() => void characterQuery.refetch()}>{characterQuery.error.message}</ErrorState>
      </Panel>
    )
  }

  const character = characterQuery.data
  if (!character) {
    return null
  }

  const locationName = locationQuery.data?.name ?? '…'
  const canAllocate = character.unspentAttributePoints > 0
  const progression = character.progression
  const busy = mutationsDisabled || allocating !== null || respeccing
  const initial = character.name.trim().charAt(0).toUpperCase() || '?'

  return (
    <Panel
      as="aside"
      id="character"
      className="game-column game-column-left"
      data-testid="character-summary"
    >
      <div className="character-identity">
        <div className="portrait" aria-hidden="true">
          {initial}
        </div>
        <div>
          <h2 data-testid="character-summary-name" tabIndex={-1}>
            {character.name}
          </h2>
          {character.unspentAttributePoints > 0 ? (
            <StatusBadge tone="upgrade">{character.unspentAttributePoints} unspent</StatusBadge>
          ) : null}
        </div>
      </div>
      <dl className="character-summary">
        <StatRow
          label="Level"
          testId="character-summary-level"
          value={progression.maxLevel ? `Level ${character.level} — MAX` : character.level}
        />
        <div className="character-xp-block">
          <dt>XP</dt>
          <dd>
            <XpProgress progression={progression} />
          </dd>
        </div>
        <StatRow
          label="Attribute points"
          testId="character-summary-attribute-points"
          value={character.unspentAttributePoints}
        />
        <div className="vital-block">
          <div className="vital-block-header">
            <span>Health</span>
            <span>
              {character.currentHealth} / {character.maxHealth}
            </span>
          </div>
          <ProgressBar
            tone="health"
            max={character.maxHealth}
            value={character.currentHealth}
            label={`Health ${character.currentHealth} of ${character.maxHealth}`}
          />
        </div>
        <div className="vital-block">
          <div className="vital-block-header">
            <span>Stamina</span>
            <span>
              {character.currentStamina} / {character.maxStamina}
            </span>
          </div>
          <ProgressBar
            tone="stamina"
            max={character.maxStamina}
            value={character.currentStamina}
            label={`Stamina ${character.currentStamina} of ${character.maxStamina}`}
          />
        </div>
        <StatRow label="Gold" testId="character-summary-gold" value={character.gold} />
        <StatRow label="Location" testId="character-summary-location" value={locationName} />
        {(
          [
            ['Strength', 'strength', character.strength],
            ['Agility', 'agility', character.agility],
            ['Endurance', 'endurance', character.endurance],
            ['Perception', 'perception', character.perception],
          ] as const
        ).map(([label, key, value]) => (
          <StatRow key={key} label={label} testId={`character-summary-${key}`} value={value}>
            {canAllocate ? (
              <Button
                type="button"
                variant="secondary"
                className="btn-icon"
                data-testid={`allocate-${key}`}
                aria-label={`Allocate ${label}`}
                disabled={busy}
                onClick={() => void spendPoint(key)}
              >
                {allocating === key ? '…' : '+'}
              </Button>
            ) : null}
          </StatRow>
        ))}
        <StatRow label="Damage" testId="character-summary-damage" value={character.derivedStats.physicalDamage} />
        <StatRow label="Armor" testId="character-summary-armor" value={character.derivedStats.armor} />
      </dl>
      <details className="advanced-stats">
        <summary>Advanced statistics</summary>
        <dl className="derived-stats">
          <StatRow label="Accuracy" value={character.derivedStats.accuracy} />
          <StatRow label="Dodge" value={character.derivedStats.dodge} />
          <StatRow label="Crit" value={`${character.derivedStats.criticalChance}%`} />
        </dl>
      </details>
      {inventoryQuery.data ? (
        <div className="inventory-section">
          <h3>Equipment</h3>
          <EquipmentLayout
            compact
            testId="character-equipment"
            equipment={inventoryQuery.data.equipment}
            items={inventoryQuery.data.items}
            onSlotClick={(slot) =>
              navigate({ pathname: '/game', hash: 'inventory', search: `?slot=${slot}` })
            }
          />
        </div>
      ) : inventoryQuery.isLoading ? (
        <LoadingState>Loading equipment…</LoadingState>
      ) : null}
      <Button
        type="button"
        variant="danger"
        data-testid="character-respec"
        disabled={busy}
        onClick={() => setConfirmRespec(true)}
      >
        {respeccing ? '…' : 'Respec'}
      </Button>
      <Dialog
        open={confirmRespec}
        title="Respec character?"
        confirmLabel="Respec"
        confirmTestId="character-respec-confirm"
        danger
        onCancel={() => setConfirmRespec(false)}
        onConfirm={() => void handleRespec()}
      >
        This returns spent attribute points. Equipment that no longer meets requirements will be unequipped.
      </Dialog>
      {mutationsDisabled ? (
        <EmptyState>Character changes are unavailable during combat.</EmptyState>
      ) : null}
      {allocError ? (
        <p className="form-error" role="alert" data-testid="allocate-error">
          {allocError}
        </p>
      ) : null}
    </Panel>
  )
}

function XpProgress({ progression }: { progression: CharacterResponse['progression'] }) {
  if (progression.maxLevel) {
    return (
      <div className="xp-progress" data-testid="character-summary-experience">
        <ProgressBar className="xp-bar" testId="xp-progress-bar" max={100} value={100} label="MAX LEVEL" />
        <span data-testid="xp-progress-label">MAX LEVEL</span>
      </div>
    )
  }

  const into = progression.experienceIntoCurrentLevel
  const required = progression.experienceRequiredForNextLevel ?? 0
  const remaining = progression.experienceRemaining ?? 0

  return (
    <div className="xp-progress" data-testid="character-summary-experience">
      <span data-testid="xp-current-required">
        {formatXp(into)} / {formatXp(required)} XP
      </span>
      <ProgressBar
        className="xp-bar"
        testId="xp-progress-bar"
        max={100}
        value={progression.progressPercent}
        label={`${progression.progressPercent}% to next level`}
      />
      <span data-testid="xp-progress-percent">{progression.progressPercent}%</span>
      <span className="muted" data-testid="xp-remaining">
        {formatXp(remaining)} XP until Level {progression.level + 1}
      </span>
    </div>
  )
}
