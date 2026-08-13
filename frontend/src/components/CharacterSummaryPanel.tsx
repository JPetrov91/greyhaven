import { useQuery } from '@tanstack/react-query'
import { fetchCharacter } from '../api/character'
import { ApiError } from '../api/client'

export function CharacterSummaryPanel() {
  const characterQuery = useQuery({
    queryKey: ['character'],
    queryFn: fetchCharacter,
    retry: false,
  })

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

  return (
    <aside className="game-column game-column-left" data-testid="character-summary">
      <h2 data-testid="character-summary-name">{character.name}</h2>
      <dl className="character-summary">
        <div>
          <dt>Level</dt>
          <dd data-testid="character-summary-level">{character.level}</dd>
        </div>
        <div>
          <dt>Experience</dt>
          <dd>{character.experience}</dd>
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
          <dt>Strength</dt>
          <dd>{character.strength}</dd>
        </div>
        <div>
          <dt>Agility</dt>
          <dd>{character.agility}</dd>
        </div>
        <div>
          <dt>Endurance</dt>
          <dd>{character.endurance}</dd>
        </div>
        <div>
          <dt>Perception</dt>
          <dd>{character.perception}</dd>
        </div>
      </dl>
    </aside>
  )
}
