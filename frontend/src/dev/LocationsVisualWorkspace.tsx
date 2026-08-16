import { Button } from '../ui/Button'
import { GenericRow } from '../ui/GenericRow'
import { Panel } from '../ui/Panel'
import { Section } from '../ui/Section'
import { StatusBadge } from '../ui/StatusBadge'
import { DEV_UI_COMBAT_PATH } from './devUi'
import { MainShellLocationHero } from './MainShellVisualViews'
import { mainShellDestinations, mainShellNearby } from './mainShellVisualFixture'

const ACTIONS = [
  { id: 'TALK_NPCS', label: 'Talk to people', action: 'Talk' },
  { id: 'BROWSE_MARKET', label: 'Browse market listings', action: 'Open' },
  { id: 'START_EXPEDITION', label: 'Start expedition', action: 'Open' },
  { id: 'VIEW_CHAT', label: 'Global chat', action: 'Show' },
] as const

export function LocationsVisualWorkspace() {
  return (
    <div className="ms-screen">
      <MainShellLocationHero />
      <div className="ms-row ms-row-locations">
        <Panel title="Travel" data-testid="location-travel">
          <ul className="ui-row-list" data-testid="destination-list">
            {mainShellDestinations.map((destination) => (
              <GenericRow
                as="li"
                key={destination.id}
                testId={`destination-${destination.code}`}
                primary={destination.name}
                secondary={`${destination.safety === 'SAFE' ? 'Safe' : 'Dangerous'} · Lv ${destination.recommendedLevelMin}–${destination.recommendedLevelMax}`}
                action={
                  destination.code === 'NORTH_ROAD' ? (
                    <a href={DEV_UI_COMBAT_PATH} className="btn btn-primary">
                      Travel
                    </a>
                  ) : (
                    <Button type="button">Travel</Button>
                  )
                }
              />
            ))}
          </ul>
        </Panel>
        <Panel title="Available actions" data-testid="location-actions-panel">
          <ul className="ui-row-list" data-testid="location-actions">
            {ACTIONS.map((entry) => (
              <GenericRow
                as="li"
                key={entry.id}
                testId={`action-${entry.id}`}
                primary={entry.label}
                action={<Button type="button">{entry.action}</Button>}
              />
            ))}
          </ul>
        </Panel>
        <Panel title="Nearby characters" data-testid="nearby-characters-panel">
          <Section title="In this district" divider={false}>
            <ul className="ui-row-list" data-testid="nearby-characters">
              {mainShellNearby.map((character) => (
                <GenericRow
                  as="li"
                  key={character.id}
                  testId={`nearby-${character.name}`}
                  primary={character.name}
                  metadata={<StatusBadge tone="neutral">Level {character.level}</StatusBadge>}
                />
              ))}
            </ul>
          </Section>
        </Panel>
      </div>
    </div>
  )
}
