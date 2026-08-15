import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import { fetchCharacter } from '../api/character'
import { acknowledgeCombat, submitCombatAction } from '../api/combat'
import type { CombatAction, CombatActionPreviewResponse, CombatResponse } from '../api/types'
import { fetchCurrentLocation } from '../api/world'
import { CombatStage } from './CombatStage'
import { ChatPanel } from './ChatPanel'
import { Button } from '../ui/Button'
import { CharacterPortrait } from '../ui/CharacterPortrait'
import { EmptyState } from '../ui/EmptyState'
import { LocationIcon, locationArtUrl, locationWeather } from '../ui/locationMedia'
import { StatRow } from '../ui/StatRow'
import { Tabs } from '../ui/Tabs'
import { classNames } from '../ui/classNames'

const CORE_BAR_ACTIONS: CombatAction[] = [
  'QUICK_ATTACK',
  'HEAVY_ATTACK',
  'PRECISE_ATTACK',
  'DEFEND',
  'USE_POTION',
]

const FALLBACK_CORE: {
  action: CombatAction
  name: string
  description: string
  costKey?: keyof CombatResponse['coreActionCosts']
}[] = [
  {
    action: 'QUICK_ATTACK',
    name: 'Quick Attack',
    description: 'A reliable strike with balanced stamina cost.',
    costKey: 'quickAttack',
  },
  {
    action: 'HEAVY_ATTACK',
    name: 'Heavy Attack',
    description: 'A slower blow that hits harder and is easier to dodge.',
    costKey: 'heavyAttack',
  },
  {
    action: 'PRECISE_ATTACK',
    name: 'Precise Attack',
    description: 'Aim for a weak point. Lower damage, higher accuracy.',
    costKey: 'preciseAttack',
  },
  { action: 'DEFEND', name: 'Defend', description: 'Guard yourself and recover stamina.' },
  { action: 'USE_POTION', name: 'Use Potion', description: 'Drink a healing potion.' },
  { action: 'RETREAT', name: 'Flee Encounter', description: 'Attempt to leave combat.' },
]

type LogTab = 'ALL' | 'PLAYER' | 'ENEMY' | 'SYSTEM'

type Props = {
  combat: CombatResponse
  onCombatUpdate: (combat: CombatResponse | null) => void
}

export function CombatPanel({ combat, onCombatUpdate }: Props) {
  const queryClient = useQueryClient()
  const [error, setError] = useState<string | null>(null)
  const [pendingAction, setPendingAction] = useState<string | null>(null)
  const [logTab, setLogTab] = useState<LogTab>('ALL')

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

  const terminal = combat.status !== 'ACTIVE'
  const awaitingRewards = combat.status === 'PLAYER_WON' && !combat.rewards
  const legacy = combat.rulesVersion === 1
  const character = characterQuery.data
  const location = locationQuery.data
  const weather = location ? locationWeather(location.code) : null
  const previews = combat.actionPreviews?.length ? combat.actionPreviews : fallbackPreviews(combat)
  const barPreviews = previews.filter(
    (preview) =>
      preview.action !== 'RETREAT' &&
      (preview.action !== 'USE_TECHNIQUE' || !legacy) &&
      (CORE_BAR_ACTIONS.includes(preview.action) || preview.action === 'USE_TECHNIQUE'),
  )
  const retreat = previews.find((preview) => preview.action === 'RETREAT')
  const events = combat.events.filter((event) => matchesLogTab(event.type, logTab))

  async function handleAction(action: CombatAction, techniqueCode?: string) {
    setError(null)
    setPendingAction(techniqueCode ?? action)
    try {
      const updated = await submitCombatAction(combat.id, action, combat.roundNumber, techniqueCode)
      onCombatUpdate(updated)
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['character'] }),
        queryClient.invalidateQueries({ queryKey: ['inventory'] }),
        queryClient.invalidateQueries({ queryKey: ['masteries'] }),
        queryClient.invalidateQueries({ queryKey: ['techniques'] }),
      ])
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message)
        if (err.code === 'STALE_COMBAT_STATE' || err.code === 'INVENTORY_FULL') {
          await queryClient.invalidateQueries({ queryKey: ['combat'] })
        }
      } else {
        setError('Unable to perform that combat action.')
      }
    } finally {
      setPendingAction(null)
    }
  }

  async function dismissOutcome() {
    setError(null)
    try {
      await acknowledgeCombat(combat.id)
      onCombatUpdate(null)
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['character'] }),
        queryClient.invalidateQueries({ queryKey: ['inventory'] }),
        queryClient.invalidateQueries({ queryKey: ['masteries'] }),
        queryClient.invalidateQueries({ queryKey: ['techniques'] }),
      ])
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message)
      } else {
        setError('Unable to continue.')
      }
    }
  }

  const turnLabel = terminal
    ? combat.status === 'PLAYER_WON'
      ? 'Victory'
      : combat.status === 'PLAYER_LOST'
        ? 'Defeat'
        : combat.status === 'PLAYER_ESCAPED'
          ? 'Escaped'
          : 'Combat ended'
    : combat.playerStunned
      ? 'Stunned'
      : 'Your Turn'

  return (
    <section className="combat-panel combat-hud" data-testid="combat-panel" aria-label="Combat">
      <aside className="combat-hud-player">
        <div className="combat-player-identity">
          <CharacterPortrait className="combat-player-portrait" avatarCode={character?.avatarCode} />
          <div>
            <p className="combat-player-name">{character?.name ?? 'You'}</p>
            <p className="muted">Level {character?.level ?? '—'}</p>
          </div>
        </div>
        {character ? (
          <dl className="stat-list combat-stat-list">
            <StatRow label="Damage" value={character.derivedStats.physicalDamage} />
            <StatRow label="Armor" value={character.derivedStats.armor} />
            <StatRow label="Accuracy" value={character.derivedStats.accuracy} />
            <StatRow label="Dodge" value={character.derivedStats.dodge} />
            <StatRow label="Crit" value={`${character.derivedStats.criticalChance}%`} />
          </dl>
        ) : null}
        <section className="combat-hud-block">
          <h3>Encounter</h3>
          <p>
            Defeat {combat.monster.name}
            {combat.monster.tier && combat.monster.tier !== 'NORMAL'
              ? ` (${combat.monster.tier.charAt(0) + combat.monster.tier.slice(1).toLowerCase().replaceAll('_', ' ')})`
              : ''}
            .
          </p>
        </section>
        {location && weather ? (
          <section className="combat-hud-block">
            <h3>Environment</h3>
            <p className="combat-environment">
              <LocationIcon name={weather.icon} />
              <span>
                {location.name}. {weather.label}, {weather.temperature}.
              </span>
            </p>
          </section>
        ) : null}
      </aside>

      <CombatStage
        combat={combat}
        playerName={character?.name ?? 'You'}
        artUrl={location ? locationArtUrl(location.code) : undefined}
        legacy={legacy}
        terminal={terminal}
        turnLabel={turnLabel}
      >
        {terminal ? <div className="combat-outcome-overlay">{outcomeBlock()}</div> : null}
      </CombatStage>

      <aside className="combat-hud-log">
        <section className="combat-log" aria-labelledby="combat-log-heading">
          <div className="combat-log-header">
            <h3 id="combat-log-heading">Battle log</h3>
            <Tabs<LogTab>
              label="Combat log filters"
              testId="combat-log-tabs"
              value={logTab}
              onChange={setLogTab}
              tabs={[
                { id: 'ALL', label: 'All' },
                { id: 'PLAYER', label: 'Player' },
                { id: 'ENEMY', label: 'Enemy' },
                { id: 'SYSTEM', label: 'System' },
              ]}
            />
          </div>
          <ul data-testid="combat-log">
            {events.length === 0 ? (
              <li className="muted">The fight begins.</li>
            ) : (
              events.map((event) => (
                <li key={`${event.roundNumber}-${event.sequenceNumber}`} data-testid="combat-log-entry">
                  <span className="muted">R{event.roundNumber}</span> {event.message}
                </li>
              ))
            )}
          </ul>
        </section>
        {(combat.possibleLoot?.length ?? 0) > 0 ? (
          <section className="combat-loot-preview" data-testid="combat-loot-preview">
            <h3>Potential rewards</h3>
            <ul>
              {combat.possibleLoot?.map((item) => (
                <li key={`${item.itemName}-${item.dropChancePercent}`}>
                  {item.itemName} {item.dropChancePercent}%
                </li>
              ))}
            </ul>
          </section>
        ) : null}
      </aside>

      <div className="combat-hud-chat">
        <ChatPanel />
      </div>

      <div className="combat-hud-actions">
        {!terminal ? (
          <>
            <p className="combat-actions-kicker">Choose your action</p>
            <div className="combat-actions" data-testid="combat-actions">
              {barPreviews
                .filter((preview) => preview.action !== 'USE_TECHNIQUE')
                .map((preview) => (
                  <ActionCard
                    key={preview.action}
                    preview={preview}
                    testId={`combat-action-${preview.action}`}
                    pending={pendingAction === preview.action}
                    busy={pendingAction !== null}
                    onClick={() => void handleAction(preview.action)}
                  />
                ))}
            </div>
            {legacy ? null : (
              <div className="combat-techniques" data-testid="combat-techniques">
                {barPreviews.filter((preview) => preview.action === 'USE_TECHNIQUE').length === 0 ? (
                  <p className="muted">No techniques equipped for this weapon.</p>
                ) : (
                  barPreviews
                    .filter((preview) => preview.action === 'USE_TECHNIQUE')
                    .map((preview) => (
                      <ActionCard
                        key={preview.techniqueCode}
                        preview={preview}
                        testId={`combat-technique-${preview.techniqueCode}`}
                        pending={pendingAction === preview.techniqueCode}
                        busy={pendingAction !== null}
                        onClick={() => void handleAction('USE_TECHNIQUE', preview.techniqueCode ?? undefined)}
                      />
                    ))
                )}
              </div>
            )}
            {combat.playerStunned ? (
              <div className="combat-stun-skip">
                <p className="muted">You are stunned and cannot act.</p>
                <Button
                  type="button"
                  data-testid="combat-skip-stun"
                  disabled={pendingAction !== null}
                  onClick={() => void handleAction('DEFEND')}
                >
                  {pendingAction === 'DEFEND' ? '…' : 'Skip turn'}
                </Button>
              </div>
            ) : null}
          </>
        ) : (
          <p className="muted">Combat has ended.</p>
        )}
        {error ? (
          <p className="form-error" role="alert" data-testid="combat-error">
            {error}
          </p>
        ) : null}
      </div>

      <div className="combat-hud-flee">
        {!terminal && retreat ? (
          <ActionCard
            preview={retreat}
            testId="combat-action-RETREAT"
            className="combat-flee-card"
            pending={pendingAction === 'RETREAT'}
            busy={pendingAction !== null}
            onClick={() => void handleAction('RETREAT')}
          />
        ) : null}
      </div>
    </section>
  )

  function outcomeBlock() {
    if (combat.rewards) {
      return (
        <section className="combat-rewards" data-testid="combat-rewards" aria-labelledby="rewards-heading">
          <h3 id="rewards-heading">Victory</h3>
          <p data-testid="combat-reward-xp">+{combat.rewards.xp} XP</p>
          <p data-testid="combat-reward-gold">+{combat.rewards.gold} gold</p>
          {combat.rewards.newLevel > combat.rewards.previousLevel ? (
            <div className="combat-level-up" data-testid="combat-level-up">
              <p>LEVEL UP</p>
              <p>
                Level {combat.rewards.previousLevel} → {combat.rewards.newLevel}
              </p>
              <p>+{combat.rewards.attributePointsGained} Attribute Points</p>
            </div>
          ) : null}
          {combat.rewards.items.length > 0 ? (
            <ul data-testid="combat-reward-items">
              {combat.rewards.items.map((item) => (
                <li key={`${item.itemCode}-${item.quantity}`}>
                  {item.itemName} ×{item.quantity}
                </li>
              ))}
            </ul>
          ) : (
            <EmptyState>No items dropped.</EmptyState>
          )}
          <Button type="button" data-testid="combat-dismiss" onClick={() => void dismissOutcome()}>
            Continue
          </Button>
        </section>
      )
    }
    if (awaitingRewards) {
      return (
        <section className="combat-rewards" data-testid="combat-claim-rewards">
          <h3>Victory</h3>
          <p>Your inventory is full. Make room, then claim these rewards.</p>
          <Button
            type="button"
            data-testid="combat-claim-rewards-button"
            disabled={pendingAction !== null}
            onClick={() => void handleAction('DEFEND')}
          >
            {pendingAction !== null ? '…' : 'Claim rewards'}
          </Button>
        </section>
      )
    }
    return (
      <section className="combat-rewards" data-testid="combat-ended">
        <h3>
          {combat.status === 'PLAYER_LOST'
            ? 'Defeat'
            : combat.status === 'PLAYER_ESCAPED'
              ? 'Escaped'
              : 'Combat ended'}
        </h3>
        <Button type="button" data-testid="combat-dismiss" onClick={() => void dismissOutcome()}>
          Continue
        </Button>
      </section>
    )
  }
}

function fallbackPreviews(combat: CombatResponse): CombatActionPreviewResponse[] {
  const costs = combat.coreActionCosts ?? { quickAttack: 8, heavyAttack: 18, preciseAttack: 12 }
  const core = FALLBACK_CORE.map((entry) => {
    const staminaCost = entry.costKey ? costs[entry.costKey] : 0
    let disabledReason: string | null = null
    if (combat.playerStunned) {
      disabledReason = 'STUNNED'
    } else if (entry.action === 'USE_POTION' && !combat.potionAvailable) {
      disabledReason = 'NO_POTION'
    } else if (staminaCost > 0 && combat.playerStamina < staminaCost) {
      disabledReason = 'INSUFFICIENT_STAMINA'
    }
    return {
      action: entry.action,
      techniqueCode: null,
      name: entry.name,
      description: entry.description,
      staminaCost,
      hitChancePercent: null,
      disabledReason,
    }
  })
  const techniques = (combat.techniques ?? []).map((technique) => ({
    action: 'USE_TECHNIQUE' as const,
    techniqueCode: technique.code,
    name: technique.name,
    description: technique.description,
    staminaCost: technique.staminaCost,
    hitChancePercent: null,
    disabledReason: technique.disabledReason,
  }))
  return [...core, ...techniques]
}

function ActionCard({
  preview,
  testId,
  pending,
  busy,
  onClick,
  className,
}: {
  preview: CombatActionPreviewResponse
  testId: string
  pending: boolean
  busy: boolean
  onClick: () => void
  className?: string
}) {
  const disabled = busy || Boolean(preview.disabledReason)
  const costLabel = preview.staminaCost > 0 ? `(${preview.staminaCost})` : ''
  return (
    <button
      type="button"
      className={classNames('combat-action-card', className, disabled && 'combat-action-card-disabled')}
      data-testid={testId}
      disabled={disabled}
      onClick={onClick}
    >
      <strong>
        {pending ? '…' : `${preview.name}${costLabel ? ` ${costLabel}` : ''}`}
      </strong>
      {preview.staminaCost > 0 ? <span className="muted">{preview.staminaCost} Stamina</span> : null}
      <p>{preview.description}</p>
      {preview.hitChancePercent != null ? (
        <span className="combat-action-hit">{preview.hitChancePercent}% hit</span>
      ) : null}
      {preview.disabledReason ? (
        <span className="combat-action-blocked">{formatDisabled(preview.disabledReason)}</span>
      ) : null}
    </button>
  )
}

function formatDisabled(reason: string): string {
  switch (reason) {
    case 'INSUFFICIENT_STAMINA':
      return 'Not enough Stamina'
    case 'NO_POTION':
      return 'No potion'
    case 'STUNNED':
      return 'Stunned'
    default:
      return reason.replaceAll('_', ' ')
  }
}

function matchesLogTab(type: string, tab: LogTab): boolean {
  if (tab === 'ALL') {
    return true
  }
  if (tab === 'PLAYER') {
    return type.startsWith('PLAYER_')
  }
  if (tab === 'ENEMY') {
    return type.startsWith('ENEMY_')
  }
  return !type.startsWith('PLAYER_') && !type.startsWith('ENEMY_')
}
