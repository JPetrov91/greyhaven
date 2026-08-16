import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
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
import { gameLink } from '../ui/gameNav'
import { LoadingState } from '../ui/LoadingState'
import { CharacterPortrait } from '../ui/CharacterPortrait'
import { HealthBar } from '../ui/HealthBar'
import { Panel } from '../ui/Panel'
import { StaminaBar } from '../ui/StaminaBar'
import { StatRow } from '../ui/StatRow'
import { XPBar } from '../ui/XPBar'
import { StatusBadge } from '../ui/StatusBadge'

type AttrKey = 'strength' | 'agility' | 'endurance' | 'perception'

type Props = {
  mutationsDisabled?: boolean
  variant?: 'full' | 'overview'
}

function formatXp(value: number): string {
  return value.toLocaleString('en-US')
}

export function CharacterSummaryPanel({ mutationsDisabled = false, variant = 'full' }: Props) {
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const [allocError, setAllocError] = useState<string | null>(null)
  const [allocating, setAllocating] = useState<AttrKey | null>(null)
  const [respeccing, setRespeccing] = useState(false)
  const [confirmRespec, setConfirmRespec] = useState(false)
  const overview = variant === 'overview'

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
    enabled: !!characterQuery.data && !overview,
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
      <Panel as="aside" className="game-column game-column-left" id={overview ? undefined : 'character'} title="Character">
        <LoadingState>Loading character…</LoadingState>
      </Panel>
    )
  }

  if (characterQuery.error instanceof ApiError) {
    return (
      <Panel as="aside" className="game-column game-column-left" id={overview ? undefined : 'character'} title="Character">
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

  if (overview) {
    return <CharacterOverview character={character} progression={progression} />
  }

  return (
    <Panel
      as="aside"
      id="character"
      className="game-column game-column-left"
      data-testid="character-summary"
    >
      <div className="character-identity">
        <CharacterPortrait avatarCode={character.avatarCode} />
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
        <XpProgress progression={progression} />
        {overview ? null : (
          <StatRow
            label="Attribute points"
            testId="character-summary-attribute-points"
            value={character.unspentAttributePoints}
          />
        )}
        <div className="vital-block">
          <div className="vital-block-header">
            <span>Health</span>
            <span>
              {character.currentHealth} / {character.maxHealth}
            </span>
          </div>
          <HealthBar
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
          <StaminaBar
            max={character.maxStamina}
            value={character.currentStamina}
            label={`Stamina ${character.currentStamina} of ${character.maxStamina}`}
          />
        </div>
        {overview ? null : (
          <>
            <StatRow label="Gold" testId="character-summary-gold" value={character.gold} />
            <StatRow label="Location" testId="character-summary-location" value={locationName} />
          </>
        )}
        {(
          [
            ['Strength', 'strength', character.strength],
            ['Agility', 'agility', character.agility],
            ['Endurance', 'endurance', character.endurance],
            ['Perception', 'perception', character.perception],
          ] as const
        ).map(([label, key, value]) => (
          <StatRow key={key} label={label} testId={`character-summary-${key}`} value={value}>
            {!overview && canAllocate ? (
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
        {overview ? null : (
          <>
            <StatRow label="Damage" testId="character-summary-damage" value={character.derivedStats.physicalDamage} />
            <StatRow label="Armor" testId="character-summary-armor" value={character.derivedStats.armor} />
          </>
        )}
      </dl>
      {overview ? (
        <Link to={gameLink('character')} className="btn btn-secondary" data-testid="view-character">
          View Character
        </Link>
      ) : (
        <>
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
                includeFutureSlots
                testId="character-equipment"
                equipment={inventoryQuery.data.equipment}
                items={inventoryQuery.data.items}
                onLiveSlotClick={(slot) =>
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
        </>
      )}
    </Panel>
  )
}

function CharacterOverview({
  character,
  progression,
}: {
  character: CharacterResponse
  progression: CharacterResponse['progression']
}) {
  return (
    <Panel
      as="aside"
      className="character-overview-card"
      data-testid="character-summary"
      title="Character Overview"
    >
      <div className="visually-hidden">
        <h3 data-testid="character-summary-name" tabIndex={-1}>
          {character.name}
        </h3>
        <p data-testid="character-summary-level">
          {progression.maxLevel ? `Level ${character.level} — MAX` : `Level ${character.level}`}
        </p>
      </div>
      <div className="character-overview-hero">
        <div className="character-overview-portrait-wrap">
          <CharacterPortrait className="character-overview-portrait" avatarCode={character.avatarCode} />
          {character.unspentAttributePoints > 0 ? (
            <StatusBadge tone="upgrade">{character.unspentAttributePoints} unspent</StatusBadge>
          ) : null}
        </div>
        <div className="character-overview-vitals">
          <VitalMeter
            icon="health"
            label="Health"
            value={`${character.currentHealth.toLocaleString('en-US')} / ${character.maxHealth.toLocaleString('en-US')}`}
            tone="health"
            max={character.maxHealth}
            current={character.currentHealth}
            ariaLabel={`Health ${character.currentHealth} of ${character.maxHealth}`}
          />
          <VitalMeter
            icon="stamina"
            label="Stamina"
            value={`${character.currentStamina.toLocaleString('en-US')} / ${character.maxStamina.toLocaleString('en-US')}`}
            tone="stamina"
            max={character.maxStamina}
            current={character.currentStamina}
            ariaLabel={`Stamina ${character.currentStamina} of ${character.maxStamina}`}
          />
        </div>
      </div>

      <div className="character-overview-stats">
        <OverviewStat accent label="XP" testId="overview-total-xp" value={formatXp(progression.totalExperience)} />
        <OverviewStat label="STR" testId="character-summary-strength" value={character.strength} />
        <OverviewStat label="AGI" testId="character-summary-agility" value={character.agility} />
        <OverviewStat label="END" testId="character-summary-endurance" value={character.endurance} />
        <OverviewStat label="PER" testId="character-summary-perception" value={character.perception} />
      </div>

      <Link to={gameLink('character')} className="character-overview-cta" data-testid="view-character">
        <span className="character-overview-cta-label">View Character</span>
      </Link>
    </Panel>
  )
}

const VITAL_ART = {
  health: '/icons/vitals/health.webp',
  stamina: '/icons/vitals/stamina.webp',
} as const

function VitalMeter({
  icon,
  label,
  value,
  tone,
  max,
  current,
  ariaLabel,
}: {
  icon: keyof typeof VITAL_ART
  label: string
  value: string
  tone: 'health' | 'stamina'
  max: number
  current: number
  ariaLabel: string
}) {
  return (
    <div className="vital-meter">
      <div className="vital-meter-head">
        <img className="vital-icon-art" src={VITAL_ART[icon]} alt="" aria-hidden="true" />
        <div className="vital-meter-copy">
          <span className="vital-meter-label">{label}</span>
          <span className="vital-meter-value">{value}</span>
        </div>
      </div>
      {tone === 'health' ? (
        <HealthBar max={max} value={current} label={ariaLabel} />
      ) : (
        <StaminaBar max={max} value={current} label={ariaLabel} />
      )}
    </div>
  )
}

function OverviewStat({
  label,
  value,
  testId,
  accent = false,
}: {
  label: string
  value: number | string
  testId: string
  accent?: boolean
}) {
  return (
    <div className="overview-stat">
      <span className="overview-stat-label">{label}</span>
      <strong className={accent ? 'overview-stat-value overview-stat-value-xp' : 'overview-stat-value'} data-testid={testId}>
        {value}
      </strong>
    </div>
  )
}

function XpProgress({ progression }: { progression: CharacterResponse['progression'] }) {
  if (progression.maxLevel) {
    return (
      <div className="vital-block character-xp-block" data-testid="character-summary-experience">
        <div className="vital-block-header">
          <span>XP</span>
          <span data-testid="xp-progress-label">MAX LEVEL</span>
        </div>
        <XPBar testId="xp-progress-bar" max={100} value={100} label="MAX LEVEL" />
      </div>
    )
  }

  const into = progression.experienceIntoCurrentLevel
  const required = progression.experienceRequiredForNextLevel ?? 0
  const remaining = progression.experienceRemaining ?? 0

  return (
    <div className="vital-block character-xp-block" data-testid="character-summary-experience">
      <div className="vital-block-header">
        <span>XP</span>
        <span data-testid="xp-current-required">
          {formatXp(into)} / {formatXp(required)} XP
        </span>
      </div>
      <XPBar
        testId="xp-progress-bar"
        max={100}
        value={progression.progressPercent}
        label={`${progression.progressPercent}% to next level`}
      />
      <div className="vital-block-meta">
        <span data-testid="xp-progress-percent">{progression.progressPercent}%</span>
        <span className="muted" data-testid="xp-remaining">
          {formatXp(remaining)} XP until Level {progression.level + 1}
        </span>
      </div>
    </div>
  )
}
