import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { allocateAttributes, fetchCharacter } from '../api/character'
import { fetchCurrentLocation } from '../api/world'
import { ApiError } from '../api/client'

type AttrKey = 'strength' | 'agility' | 'endurance' | 'perception'

type Props = {
  mutationsDisabled?: boolean
}

export function CharacterSummaryPanel({ mutationsDisabled = false }: Props) {
  const queryClient = useQueryClient()
  const [allocError, setAllocError] = useState<string | null>(null)
  const [allocating, setAllocating] = useState<AttrKey | null>(null)

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

  return (
    <aside id="character" className="game-column game-column-left" data-testid="character-summary">
      <h2 data-testid="character-summary-name">{character.name}</h2>
      <dl className="character-summary">
        <div>
          <dt>Level</dt>
          <dd data-testid="character-summary-level">{character.level}</dd>
        </div>
        <div>
          <dt>Experience</dt>
          <dd data-testid="character-summary-experience">{character.experience}</dd>
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
                  disabled={mutationsDisabled || allocating !== null}
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
      {allocError ? (
        <p className="form-error" role="alert" data-testid="allocate-error">
          {allocError}
        </p>
      ) : null}
    </aside>
  )
}
