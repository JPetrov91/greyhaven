import { Panel } from '../ui/Panel'
import { ComingLaterButton } from '../ui/ComingLater'

export function GuildPlaceholder() {
  return (
    <Panel className="guild-placeholder" data-testid="guild-placeholder" title="My Guild">
      <div className="guild-deco location-art" aria-hidden="true" />
      <p className="muted">Guilds, roster, and guild activities are not part of the current release.</p>
      <ComingLaterButton data-testid="guild-overview">Guild Overview</ComingLaterButton>
    </Panel>
  )
}
