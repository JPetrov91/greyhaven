import type { ReactNode } from 'react'
import type { CombatResponse, CombatStatusResponse } from '../api/types'
import { CombatStatusIcon } from '../ui/combatStatusIcons'
import { monsterCombatArtUrl, PLAYER_COMBAT_AVATAR_URL } from '../ui/combatMedia'
import { ProgressBar } from '../ui/ProgressBar'
import { StatusBadge } from '../ui/StatusBadge'
import { classNames } from '../ui/classNames'

type Props = {
  combat: CombatResponse
  playerName: string
  artUrl?: string
  legacy: boolean
  terminal: boolean
  turnLabel: string
  children?: ReactNode
}

export function CombatStage({ combat, playerName, artUrl, legacy, terminal, turnLabel, children }: Props) {
  return (
    <div className="combat-hud-viewport" data-testid="combat-stage">
      <div
        className="combat-stage-backdrop"
        style={artUrl ? { backgroundImage: `url(${artUrl})` } : undefined}
      />
      <div className="combat-stage-veil" />
      <div className="combat-stage-fighters">
        <CombatFighter
          side="player"
          name={playerName}
          imageSrc={PLAYER_COMBAT_AVATAR_URL}
          health={combat.playerHealth}
          maxHealth={combat.playerMaxHealth}
          stamina={combat.playerStamina}
          maxStamina={combat.playerMaxStamina}
          healthTestId="combat-player-health"
          staminaTestId="combat-player-stamina"
          statuses={legacy ? [] : combat.playerStatuses ?? []}
          statusTestId="combat-player-statuses"
          showStamina
        />
        <CombatFighter
          side="enemy"
          name={combat.monster.name}
          subtitle={`Level ${combat.monster.level}${combat.monster.tier && combat.monster.tier !== 'NORMAL' ? ` · ${formatArchetype(combat.monster.tier)}` : ''}${combat.monster.archetype ? ` · ${formatArchetype(combat.monster.archetype)}` : ''}`}
          nameTestId="combat-monster-name"
          imageSrc={monsterCombatArtUrl(combat.monster.code)}
          health={combat.enemyHealth}
          maxHealth={combat.enemyMaxHealth}
          stamina={combat.enemyStamina}
          maxStamina={combat.enemyMaxStamina}
          healthTestId="combat-enemy-health"
          staminaTestId="combat-enemy-stamina"
          statuses={legacy ? [] : combat.enemyStatuses ?? []}
          statusTestId="combat-enemy-statuses"
          showStamina={!legacy}
          intent={!terminal ? combat.enemyIntent?.label : undefined}
        />
      </div>
      <div className="combat-round-badge">
        <strong>ROUND {combat.roundNumber}</strong>
        <span>{turnLabel}</span>
      </div>
      {children}
    </div>
  )
}

function CombatFighter({
  side,
  name,
  subtitle,
  nameTestId,
  imageSrc,
  health,
  maxHealth,
  stamina,
  maxStamina,
  healthTestId,
  staminaTestId,
  statuses,
  statusTestId,
  showStamina,
  intent,
}: {
  side: 'player' | 'enemy'
  name: string
  subtitle?: string
  nameTestId?: string
  imageSrc: string
  health: number
  maxHealth: number
  stamina: number
  maxStamina: number
  healthTestId: string
  staminaTestId: string
  statuses: CombatStatusResponse[]
  statusTestId: string
  showStamina: boolean
  intent?: string
}) {
  return (
    <div className={classNames('combat-fighter', `combat-fighter-${side}`)} data-testid={`combat-fighter-${side}`}>
      <div className="combat-fighter-figure">
        <img src={imageSrc} alt="" />
      </div>
      <div className="combat-fighter-hud" data-testid={side === 'player' ? 'combat-vitals' : undefined}>
        <div className="combat-fighter-identity">
          <strong data-testid={nameTestId}>{name}</strong>
          {subtitle ? <span className="muted">{subtitle}</span> : null}
        </div>
        <StageVital label="Health" testId={healthTestId} value={health} max={maxHealth} tone="health" />
        {showStamina ? (
          <StageVital label="Stamina" testId={staminaTestId} value={stamina} max={maxStamina} tone="stamina" />
        ) : null}
        {intent ? (
          <section className="combat-intent-panel" data-testid="combat-enemy-intent">
            <h3>Enemy intent</h3>
            <p>{intent}</p>
          </section>
        ) : null}
        <StatusPanel statuses={statuses} testId={statusTestId} />
      </div>
    </div>
  )
}

function StageVital({
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
    <div className="combat-stage-vital">
      <span>{label}</span>
      <strong data-testid={testId}>
        {value} / {max}
      </strong>
      <ProgressBar value={value} max={Math.max(1, max)} label={label} tone={tone} />
    </div>
  )
}

function StatusPanel({ statuses, testId }: { statuses: CombatStatusResponse[]; testId: string }) {
  return (
    <section className="combat-status-panel" data-testid={testId} aria-label="Status effects">
      <h3>Status effects</h3>
      {statuses.length === 0 ? (
        <p className="muted">No statuses</p>
      ) : (
        <ul>
          {statuses.map((status) => (
            <li key={`${status.type}-${status.remainingRounds}`}>
              <span className="combat-status-art">
                <CombatStatusIcon type={status.type} />
              </span>
              <div>
                <StatusBadge tone={statusTone(status.type)}>{formatStatusName(status.type)}</StatusBadge>
                <p className="muted">
                  {status.stacks > 1 ? `×${status.stacks} · ` : ''}
                  {status.remainingRounds} turns
                </p>
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
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

function formatStatusName(type: string): string {
  return type.replaceAll('_', ' ')
}

function formatArchetype(archetype: string): string {
  return archetype.charAt(0) + archetype.slice(1).toLowerCase()
}
