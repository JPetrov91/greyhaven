import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { allocateAttributes, fetchCharacter, respecCharacter } from '../api/character'
import { fetchCurrentLocation } from '../api/world'
import { ApiError } from '../api/client'
import type { CharacterResponse } from '../api/types'

type AttrKey = 'strength' | 'agility' | 'endurance' | 'perception'

type Props = {
  mutationsDisabled?: boolean
}

function formatXp(value: number): string {
  return value.toLocaleString('en-US')
}

export function CharacterSummaryPanel({ mutationsDisabled = false }: Props) {
  const queryClient = useQueryClient()
  const [allocError, setAllocError] = useState<string | null>(null)
  const [allocating, setAllocating] = useState<AttrKey | null>(null)
  const [respeccing, setRespeccing] = useState(false)

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
      <aside className="game-column game-column-left">
        <h2>Character</h2>
        <p className="muted">Loading character…</p>
      </aside>
    )
  }

  if (characterQuery.error instanceof ApiError) {
    return (
      <aside className="game-column game-column-left">
        <h2>Character</h2>
        <p className="form-error">{characterQuery.error.message}</p>
      </aside>
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

  return (
    <aside id="character" className="game-column game-column-left" data-testid="character-summary">
      <h2 data-testid="character-summary-name">{character.name}</h2>
      <dl className="character-summary">
        <div>
          <dt>Level</dt>
          <dd data-testid="character-summary-level">
            {progression.maxLevel ? `Level ${character.level} — MAX` : `Lv. ${character.level}`}
          </dd>
        </div>
        <div className="character-xp-block">
          <dt>XP</dt>
          <dd>
            <XpProgress progression={progression} />
          </dd>
        </div>
        <div>
          <dt>Attribute points</dt>
          <dd data-testid="character-summary-attribute-points">{character.unspentAttributePoints}</dd>
        </div>
        <div>
          <dt>Health</dt>
          <dd>
            {character.currentHealth} / {character.maxHealth}
          </dd>
        </div>
        <div>
          <dt>Stamina</dt>
          <dd>
            {character.currentStamina} / {character.maxStamina}
          </dd>
        </div>
        <div>
          <dt>Gold</dt>
          <dd data-testid="character-summary-gold">{character.gold}</dd>
        </div>
        <div>
          <dt>Location</dt>
          <dd data-testid="character-summary-location">{locationName}</dd>
        </div>
        {(
          [
            ['Strength', 'strength', character.strength],
            ['Agility', 'agility', character.agility],
            ['Endurance', 'endurance', character.endurance],
            ['Perception', 'perception', character.perception],
          ] as const
        ).map(([label, key, value]) => (
          <div key={key}>
            <dt>{label}</dt>
            <dd>
              <span data-testid={`character-summary-${key}`}>{value}</span>
              {canAllocate ? (
                <button
                  type="button"
                  className="attr-plus"
                  data-testid={`allocate-${key}`}
                  disabled={busy}
                  onClick={() => void spendPoint(key)}
                >
                  {allocating === key ? '…' : '+'}
                </button>
              ) : null}
            </dd>
          </div>
        ))}
        <div>
          <dt>Damage</dt>
          <dd data-testid="character-summary-damage">{character.derivedStats.physicalDamage}</dd>
        </div>
        <div>
          <dt>Armor</dt>
          <dd data-testid="character-summary-armor">{character.derivedStats.armor}</dd>
        </div>
      </dl>
      <button
        type="button"
        className="travel-button"
        data-testid="character-respec"
        disabled={busy}
        onClick={() => void handleRespec()}
      >
        {respeccing ? '…' : 'Respec'}
      </button>
      {allocError ? (
        <p className="form-error" role="alert" data-testid="allocate-error">
          {allocError}
        </p>
      ) : null}
    </aside>
  )
}

function XpProgress({ progression }: { progression: CharacterResponse['progression'] }) {
  if (progression.maxLevel) {
    return (
      <div className="xp-progress" data-testid="character-summary-experience">
        <progress
          className="xp-bar"
          data-testid="xp-progress-bar"
          max={100}
          value={100}
          aria-label="MAX LEVEL"
        />
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
      <progress
        className="xp-bar"
        data-testid="xp-progress-bar"
        max={100}
        value={progression.progressPercent}
        aria-label={`${progression.progressPercent}% to next level`}
      />
      <span data-testid="xp-progress-percent">{progression.progressPercent}%</span>
      <span className="muted" data-testid="xp-remaining">
        {formatXp(remaining)} XP until Level {progression.level + 1}
      </span>
    </div>
  )
}
