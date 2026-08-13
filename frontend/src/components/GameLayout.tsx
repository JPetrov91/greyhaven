import { CharacterSummaryPanel } from './CharacterSummaryPanel'
import { LocationPanel } from './LocationPanel'

export function GameLayout() {
  return (
    <section className="game-layout" aria-label="Game workspace" data-testid="game-layout">
      <CharacterSummaryPanel />
      <LocationPanel />

      <aside className="game-column game-column-right">
        <h2>Activity</h2>
        <p>Feed and chat will appear here.</p>
      </aside>
    </section>
  )
}
