import type { NpcResponse } from '../api/types'
import { HereNowList } from '../components/HereNowList'
import { NpcStrip } from '../components/NpcStrip'
import { Button } from '../ui/Button'
import { LocationCrest, LocationIcon, locationActionArtUrl, locationArtUrl } from '../ui/locationMedia'
import { MainShellChat } from './MainShellVisualViews'
import { mainShellLocation, mainShellNearby } from './mainShellVisualFixture'

const VISUAL_NPCS: NpcResponse[] = [
  {
    code: 'EDRIC_VARN',
    name: 'Edric Varn',
    title: 'Weaponsmith',
    description: 'Honest steel.',
    greeting: 'Buy something that comes back.',
    portraitCode: 'edric-varn',
    locationCode: 'MARKET',
    merchantCode: 'WEAPONSMITH',
    interactions: ['TALK', 'SHOP'],
    questBadges: [],
  },
  {
    code: 'MARA_HELDEN',
    name: 'Mara Helden',
    title: 'Armorer',
    description: 'Leather and mail.',
    greeting: 'Leather first.',
    portraitCode: 'mara-helden',
    locationCode: 'MARKET',
    merchantCode: 'ARMORER',
    interactions: ['TALK', 'SHOP'],
    questBadges: [],
  },
]

export function LocationsVisualWorkspace() {
  const location = mainShellLocation
  const safe = location.safety === 'SAFE'

  return (
    <div className="locations-dashboard">
      <div className="locations-workspace" data-testid="location-panel" id="world">
        <div className="locations-split">
          <div className="locations-hero-well">
            <div
              className="location-hero-art"
              aria-hidden="true"
              style={{ backgroundImage: `url(${locationArtUrl(location.code)})` }}
            />
            <div className="locations-hero-overlay">
              <div className="location-hero-identity">
                <LocationCrest />
                <div>
                  <p className="location-hero-kicker">Current location</p>
                  <h2 className="location-hero-region">{location.region}</h2>
                  <p className="location-hero-place" data-testid="current-location">
                    {location.name}
                  </p>
                </div>
              </div>
              <p className="location-description">{location.description}</p>
              <p className="location-hero-pills">
                <span className={safe ? 'location-hero-pill location-hero-pill-safe' : 'location-hero-pill location-hero-pill-danger'}>
                  <LocationIcon name={safe ? 'spark' : 'danger'} />
                  {safe ? 'Safe Zone' : 'Dangerous'}
                </span>
              </p>
              <nav className="location-place-verbs" aria-label="Place actions">
                <Button type="button" variant="secondary" className="location-hero-tile" data-testid="hero-travel">
                  <span className="location-hero-tile-icon">
                    <img className="location-hero-tile-art" src={locationActionArtUrl('compass')} alt="" />
                  </span>
                  <span className="location-hero-tile-copy">
                    <strong className="type-item">Travel</strong>
                    <span className="type-meta">Change location</span>
                  </span>
                </Button>
              </nav>
            </div>
          </div>
          <aside className="locations-people">
            <NpcStrip npcs={VISUAL_NPCS} onTalk={() => undefined} />
            <HereNowList
              locationName={location.name}
              characters={mainShellNearby}
              loading={false}
              truncated={false}
              totalCount={mainShellNearby.length}
              limit={50}
              onInspect={() => undefined}
            />
          </aside>
        </div>
      </div>
      <div id="global-chat" className="locations-chat">
        <MainShellChat />
      </div>
    </div>
  )
}
