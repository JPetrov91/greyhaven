import type { NearbyCharacterResponse } from '../api/types'
import { CharacterPortrait } from '../ui/CharacterPortrait'
import { EmptyState } from '../ui/EmptyState'
import { LoadingState } from '../ui/LoadingState'

type Props = {
  locationName: string
  characters: NearbyCharacterResponse[]
  loading: boolean
  truncated: boolean
  totalCount: number
  limit: number
  onInspect: (id: string) => void
}

export function hereNowEmptyCopy(locationName: string): string {
  if (locationName.toLowerCase().includes('square')) {
    return 'The square is quiet.'
  }
  return `${locationName} is quiet.`
}

export function HereNowList({
  locationName,
  characters,
  loading,
  truncated,
  totalCount,
  limit,
  onInspect,
}: Props) {
  return (
    <section className="here-now" aria-labelledby="here-now-heading">
      <h3 id="here-now-heading" className="here-now-heading">
        Here now
        <span className="here-now-count" data-testid="here-now-count">
          {totalCount}
        </span>
      </h3>
      {loading ? (
        <LoadingState>Looking around…</LoadingState>
      ) : characters.length === 0 ? (
        <EmptyState testId="nearby-empty">{hereNowEmptyCopy(locationName)}</EmptyState>
      ) : (
        <>
          <ul className="here-now-list" data-testid="nearby-characters">
            {characters.map((character) => (
              <li key={character.id} data-testid={`nearby-${character.name}`}>
                <button type="button" className="here-now-row" onClick={() => onInspect(character.id)}>
                  <CharacterPortrait className="here-now-portrait" avatarCode={character.avatarCode} />
                  <span className="here-now-copy">
                    <strong>{character.name}</strong>
                    <span className="muted">Lv. {character.level}</span>
                  </span>
                </button>
              </li>
            ))}
          </ul>
          {truncated ? (
            <p className="muted" data-testid="nearby-truncated">
              Showing the first {limit} characters here.
            </p>
          ) : null}
        </>
      )}
    </section>
  )
}
