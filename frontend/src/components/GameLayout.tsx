import { CharacterSummaryPanel } from './CharacterSummaryPanel'

export function GameLayout() {
  return (
    <section className="game-layout" aria-label="Game workspace" data-testid="game-layout">
      <CharacterSummaryPanel />

      <div className="game-column game-column-center">
        <h2>Greyhaven</h2>
        <p>
          Main world view, combat, and location actions will appear in this
          column.
        </p>
        <p className="muted">
          Character onboarding is ready. World and combat arrive in later tasks.
        </p>
      </div>

      <aside className="game-column game-column-right">
        <h2>Activity</h2>
        <p>Feed and chat will appear here.</p>
      </aside>
    </section>
  )
}
