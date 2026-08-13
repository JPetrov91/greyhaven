export function GameLayout() {
  return (
    <section className="game-layout" aria-label="Game workspace">
      <aside className="game-column game-column-left">
        <h2>Character</h2>
        <p>Stats, equipment, and quick actions will appear here.</p>
      </aside>

      <div className="game-column game-column-center">
        <h2>Greyhaven</h2>
        <p>
          Main world view, combat, and location actions will appear in this
          column.
        </p>
        <p className="muted">Bootstrap complete. Game mechanics arrive in later tasks.</p>
      </div>

      <aside className="game-column game-column-right">
        <h2>Activity</h2>
        <p>Feed and chat will appear here.</p>
      </aside>
    </section>
  )
}
