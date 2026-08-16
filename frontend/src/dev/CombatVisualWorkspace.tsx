import { useState } from 'react'
import type { CombatActionPreviewResponse } from '../api/types'
import { CombatStage } from '../components/CombatStage'
import { Button } from '../ui/Button'
import { CharacterPortrait } from '../ui/CharacterPortrait'
import { classNames } from '../ui/classNames'
import { LocationIcon, locationArtUrl, locationWeather } from '../ui/locationMedia'
import { StatRow } from '../ui/StatRow'
import { Tabs } from '../ui/Tabs'
import { MainShellChat } from './MainShellVisualViews'
import { mainShellCharacter, mainShellCombat, mainShellCombatLocation } from './mainShellVisualFixture'

type LogTab = 'ALL' | 'PLAYER' | 'ENEMY' | 'SYSTEM'

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

function formatDisabled(reason: string): string {
  if (reason === 'INSUFFICIENT_STAMINA') {
    return 'Not enough Stamina'
  }
  if (reason === 'NO_POTION') {
    return 'No potion'
  }
  return reason.replaceAll('_', ' ')
}

function ActionCard({
  preview,
  testId,
  className,
}: {
  preview: CombatActionPreviewResponse
  testId: string
  className?: string
}) {
  const disabled = Boolean(preview.disabledReason)
  return (
    <button
      type="button"
      className={classNames('combat-action-card', className, disabled && 'combat-action-card-disabled')}
      data-testid={testId}
      disabled={disabled}
    >
      <strong>
        {preview.name}
        {preview.staminaCost > 0 ? ` (${preview.staminaCost})` : ''}
      </strong>
      {preview.staminaCost > 0 ? <span className="type-meta">{preview.staminaCost} Stamina</span> : null}
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

export function CombatVisualWorkspace() {
  const combat = mainShellCombat
  const character = mainShellCharacter
  const location = mainShellCombatLocation
  const weather = locationWeather(location.code)
  const [logTab, setLogTab] = useState<LogTab>('ALL')
  const previews = combat.actionPreviews ?? []
  const retreat = previews.find((preview) => preview.action === 'RETREAT')
  const bar = previews.filter((preview) => preview.action !== 'RETREAT')
  const events = combat.events.filter((event) => matchesLogTab(event.type, logTab))

  return (
    <section className="combat-panel combat-hud" data-testid="combat-panel" aria-label="Combat">
      <aside className="combat-hud-player">
        <div className="combat-player-identity">
          <CharacterPortrait className="combat-player-portrait" avatarCode={character.avatarCode} />
          <div>
            <p className="combat-player-name">{character.name}</p>
            <p className="type-meta">Level {character.level}</p>
          </div>
        </div>
        <dl className="stat-list combat-stat-list">
          <StatRow label="Damage" value={character.derivedStats.physicalDamage} />
          <StatRow label="Armor" value={character.derivedStats.armor} />
          <StatRow label="Accuracy" value={character.derivedStats.accuracy} />
          <StatRow label="Dodge" value={character.derivedStats.dodge} />
          <StatRow label="Crit" value={`${character.derivedStats.criticalChance}%`} />
        </dl>
        <section className="combat-hud-block">
          <h3>Encounter</h3>
          <p>Defeat {combat.monster.name} (Elite).</p>
        </section>
        <section className="combat-hud-block">
          <h3>Environment</h3>
          <p className="combat-environment">
            <LocationIcon name={weather.icon} />
            <span>
              {location.name}. {weather.label}, {weather.temperature}.
            </span>
          </p>
        </section>
      </aside>

      <CombatStage
        combat={combat}
        playerName={character.name}
        artUrl={locationArtUrl(location.code)}
        legacy={false}
        terminal={false}
        turnLabel="Your Turn"
      />

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
            {events.map((event) => (
              <li key={`${event.roundNumber}-${event.sequenceNumber}`} data-testid="combat-log-entry">
                <span className="type-meta">R{event.roundNumber}</span> {event.message}
              </li>
            ))}
          </ul>
        </section>
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
      </aside>

      <div className="combat-hud-chat">
        <MainShellChat />
      </div>

      <div className="combat-hud-actions">
        <p className="combat-actions-kicker">Choose your action</p>
        <div className="combat-actions" data-testid="combat-actions">
          {bar.map((preview) => (
            <ActionCard key={preview.action} preview={preview} testId={`combat-action-${preview.action}`} />
          ))}
        </div>
        <div className="combat-techniques" data-testid="combat-techniques">
          <p className="type-meta">No techniques equipped for this weapon.</p>
        </div>
      </div>

      <div className="combat-hud-flee">
        {retreat ? (
          <ActionCard preview={retreat} testId="combat-action-RETREAT" className="combat-flee-card" />
        ) : (
          <Button type="button" variant="danger">
            Flee Encounter
          </Button>
        )}
      </div>
    </section>
  )
}
