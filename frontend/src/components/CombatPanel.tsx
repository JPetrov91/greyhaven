import { useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import { acknowledgeCombat, submitCombatAction } from '../api/combat'
import type { CombatAction, CombatResponse } from '../api/types'
import { Button } from '../ui/Button'
import { EmptyState } from '../ui/EmptyState'

const ACTIONS: { action: CombatAction; label: string }[] = [
  { action: 'QUICK_ATTACK', label: 'Quick Attack' },
  { action: 'HEAVY_ATTACK', label: 'Heavy Attack' },
  { action: 'PRECISE_ATTACK', label: 'Precise Attack' },
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
  const [pendingAction, setPendingAction] = useState<CombatAction | null>(null)

  const terminal = combat.status !== 'ACTIVE'

  async function handleAction(action: CombatAction) {
    setError(null)
    setPendingAction(action)
    try {
      const updated = await submitCombatAction(combat.id, action, combat.roundNumber)
      onCombatUpdate(updated)
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['character'] }),
        queryClient.invalidateQueries({ queryKey: ['inventory'] }),
      ])
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message)
        if (err.code === 'STALE_COMBAT_STATE') {
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
    <section className="combat-panel" data-testid="combat-panel" aria-label="Combat">
      <h2 data-testid="combat-monster-name">{combat.monster.name}</h2>
      <p className="muted">Level {combat.monster.level} · Round {combat.roundNumber}</p>

      <div className="combat-vitals" data-testid="combat-vitals">
        <div>
          <span>Your HP</span>
          <strong data-testid="combat-player-health">
            {combat.playerHealth} / {combat.playerMaxHealth}
          </strong>
        </div>
        <div>
          <span>Your Stamina</span>
          <strong data-testid="combat-player-stamina">
            {combat.playerStamina} / {combat.playerMaxStamina}
          </strong>
        </div>
        <div>
          <span>Enemy HP</span>
          <strong data-testid="combat-enemy-health">
            {combat.enemyHealth} / {combat.enemyMaxHealth}
          </strong>
        </div>
      </div>

      {!terminal ? (
        <div className="combat-actions" data-testid="combat-actions">
          {ACTIONS.map(({ action, label }) => (
            <Button
              key={action}
              type="button"
              data-testid={`combat-action-${action}`}
              disabled={pendingAction !== null || (action === 'USE_POTION' && !combat.potionAvailable)}
              onClick={() => void handleAction(action)}
            >
              {pendingAction === action ? '…' : label}
            </Button>
          ))}
        </div>
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
      ) : null}
    </section>
  )
}
