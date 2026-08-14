import { useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import { acknowledgeCombat, submitCombatAction } from '../api/combat'
import type { CombatAction, CombatResponse, CombatStatusResponse } from '../api/types'
import { Button } from '../ui/Button'
import { EmptyState } from '../ui/EmptyState'
import { Panel } from '../ui/Panel'
import { ProgressBar } from '../ui/ProgressBar'
import { StatusBadge } from '../ui/StatusBadge'
import { Tooltip } from '../ui/Tooltip'

const CORE_ACTIONS: { action: CombatAction; label: string; costKey?: keyof CombatResponse['coreActionCosts'] }[] = [
  { action: 'QUICK_ATTACK', label: 'Quick Attack', costKey: 'quickAttack' },
  { action: 'HEAVY_ATTACK', label: 'Heavy Attack', costKey: 'heavyAttack' },
  { action: 'PRECISE_ATTACK', label: 'Precise Attack', costKey: 'preciseAttack' },
  { action: 'DEFEND', label: 'Defend' },
  { action: 'USE_POTION', label: 'Use Potion' },
  { action: 'RETREAT', label: 'Retreat' },
]

type Props = {
  combat: CombatResponse
  onCombatUpdate: (combat: CombatResponse | null) => void
}

export function CombatPanel({ combat, onCombatUpdate }: Props) {
  const queryClient = useQueryClient()
  const [error, setError] = useState<string | null>(null)
  const [pendingAction, setPendingAction] = useState<string | null>(null)
  const [hoveredTechnique, setHoveredTechnique] = useState<string | null>(null)

      const terminal = combat.status !== 'ACTIVE'
      const awaitingRewards = combat.status === 'PLAYER_WON' && !combat.rewards
      const legacy = combat.rulesVersion === 1
  const costs = combat.coreActionCosts ?? { quickAttack: 8, heavyAttack: 18, preciseAttack: 12 }

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

  return (
    <Panel className="combat-panel" data-testid="combat-panel" aria-label="Combat">
      <h2>
        <span data-testid="combat-monster-name">{combat.monster.name}</span>
        <span className="muted">
          {' '}
          Level {combat.monster.level}
          {combat.monster.archetype ? ` · ${formatArchetype(combat.monster.archetype)}` : ''}
          {' · Round '}
          {combat.roundNumber}
        </span>
      </h2>
      <div className="combat-vitals" data-testid="combat-vitals">
        <Vital
          label="Your HP"
          testId="combat-player-health"
          value={combat.playerHealth}
          max={combat.playerMaxHealth}
          tone="health"
        />
        <Vital
          label="Your Stamina"
          testId="combat-player-stamina"
          value={combat.playerStamina}
          max={combat.playerMaxStamina}
          tone="stamina"
        />
        <Vital
          label="Enemy HP"
          testId="combat-enemy-health"
          value={combat.enemyHealth}
          max={combat.enemyMaxHealth}
          tone="health"
        />
        {legacy ? null : (
          <Vital
            label="Enemy Stamina"
            testId="combat-enemy-stamina"
            value={combat.enemyStamina}
            max={combat.enemyMaxStamina}
            tone="stamina"
          />
        )}
      </div>

      {legacy ? null : (
        <div className="combat-status-rows">
          <StatusRow label="You" statuses={combat.playerStatuses ?? []} testId="combat-player-statuses" />
          <StatusRow label="Enemy" statuses={combat.enemyStatuses ?? []} testId="combat-enemy-statuses" />
        </div>
      )}

      {!terminal ? (
        <>
          <div className="combat-actions" data-testid="combat-actions">
            {CORE_ACTIONS.map(({ action, label, costKey }) => {
              const cost = costKey ? costs[costKey] : 0
              const disabled =
                pendingAction !== null
                || combat.playerStunned
                || (action === 'USE_POTION' && !combat.potionAvailable)
                || (cost > 0 && combat.playerStamina < cost)
              return (
                <Button
                  key={action}
                  type="button"
                  data-testid={`combat-action-${action}`}
                  disabled={disabled}
                  onClick={() => void handleAction(action)}
                >
                  {pendingAction === action ? '…' : cost > 0 ? `${label} (${cost})` : label}
                </Button>
              )
            })}
          </div>
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
          {legacy ? null : (
            <div className="combat-techniques" data-testid="combat-techniques">
              {(combat.techniques ?? []).length === 0 ? (
                <p className="muted">No techniques equipped for this weapon.</p>
              ) : (
                combat.techniques.map((technique) => {
                  const disabled =
                    pendingAction !== null
                    || combat.playerStunned
                    || Boolean(technique.disabledReason)
                  return (
                    <Tooltip
                      key={technique.code}
                      open={hoveredTechnique === technique.code}
                      content={
                        <div>
                          <strong>{technique.name}</strong>
                          <p>{technique.description}</p>
                          <p>Stamina {technique.staminaCost}</p>
                        </div>
                      }
                    >
                      <Button
                        type="button"
                        data-testid={`combat-technique-${technique.code}`}
                        disabled={disabled}
                        onMouseEnter={() => setHoveredTechnique(technique.code)}
                        onMouseLeave={() => setHoveredTechnique(null)}
                        onClick={() => void handleAction('USE_TECHNIQUE', technique.code)}
                      >
                        {pendingAction === technique.code ? '…' : `${technique.name} (${technique.staminaCost})`}
                      </Button>
                    </Tooltip>
                  )
                })
              )}
            </div>
          )}
        </>
      ) : null}

      {error ? (
        <p className="form-error" role="alert" data-testid="combat-error">
          {error}
        </p>
      ) : null}

      <section className="combat-log" aria-labelledby="combat-log-heading">
        <h3 id="combat-log-heading">Combat log</h3>
        <ul data-testid="combat-log">
          {combat.events.length === 0 ? (
            <li className="muted">The fight begins.</li>
          ) : (
            combat.events.map((event) => (
              <li key={`${event.roundNumber}-${event.sequenceNumber}`} data-testid="combat-log-entry">
                <span className="muted">R{event.roundNumber}</span> {event.message}
              </li>
            ))
          )}
        </ul>
      </section>

      {terminal && combat.rewards ? (
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
      ) : null}

      {terminal && !combat.rewards ? (
        awaitingRewards ? (
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
        ) : (
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
      ) : null}
    </Panel>
  )
}

function Vital({
  label,
  testId,
  value,
  max,
  tone,
}: {
  label: string
  testId: string
  value: number
  max: number
  tone: 'health' | 'stamina'
}) {
  return (
    <div>
      <span>{label}</span>
      <strong data-testid={testId}>
        {value} / {max}
      </strong>
      <ProgressBar value={value} max={Math.max(1, max)} label={label} tone={tone} />
    </div>
  )
}

function StatusRow({
  label,
  statuses,
  testId,
}: {
  label: string
  statuses: CombatStatusResponse[]
  testId: string
}) {
  return (
    <div className="combat-status-row" data-testid={testId}>
      <span className="muted">{label}</span>
      {statuses.length === 0 ? (
        <span className="muted">No statuses</span>
      ) : (
        statuses.map((status) => (
          <StatusBadge key={`${status.type}-${status.remainingRounds}`} tone={statusTone(status.type)}>
            {formatStatus(status)}
          </StatusBadge>
        ))
      )}
    </div>
  )
}

function statusTone(type: string): 'safe' | 'danger' | 'neutral' | 'upgrade' | 'downgrade' | 'mixed' {
  switch (type) {
    case 'BLEED':
    case 'POISON':
      return 'danger'
    case 'STUN':
      return 'mixed'
    case 'GUARDED':
    case 'STUN_IMMUNITY':
      return 'safe'
    case 'ARMOR_BREAK':
    case 'OFF_BALANCE':
      return 'downgrade'
    default:
      return 'neutral'
  }
}

function formatStatus(status: CombatStatusResponse): string {
  const name = status.type.replaceAll('_', ' ')
  return status.stacks > 1
    ? `${name} ×${status.stacks} (${status.remainingRounds}r)`
    : `${name} (${status.remainingRounds}r)`
}

function formatArchetype(archetype: string): string {
  return archetype.charAt(0) + archetype.slice(1).toLowerCase()
}
