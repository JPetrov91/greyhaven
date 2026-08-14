import { Link, useLocation } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { fetchCurrentExpedition } from '../api/expedition'
import { fetchCurrentLocation } from '../api/world'
import { ComingLaterButton } from '../ui/ComingLater'
import { ChromeIcon, type ChromeIconName } from '../ui/chromeIcons'
import { gameLink, isGameNavActive, type GameNavItem } from '../ui/gameNav'
import { persistUiMode, readStoredUiMode, applyUiMode, type UiMode } from '../ui/uiMode'
import { persistNavCollapsed, readStoredNavCollapsed } from '../ui/navCollapse'
import { useState } from 'react'
import { Button } from '../ui/Button'
import { classNames } from '../ui/classNames'

const LIVE_NAV: { item: GameNavItem; label: string; icon: ChromeIconName; testId: string }[] = [
  { item: 'home', label: 'Home', icon: 'home', testId: 'nav-home' },
  { item: 'world', label: 'Locations', icon: 'locations', testId: 'nav-world' },
  { item: 'character', label: 'Character', icon: 'character', testId: 'nav-character' },
  { item: 'inventory', label: 'Inventory', icon: 'pack', testId: 'nav-inventory' },
  { item: 'equipment', label: 'Equipment', icon: 'equipment', testId: 'nav-equipment' },
  { item: 'market', label: 'Market', icon: 'market', testId: 'nav-market' },
  { item: 'crafting', label: 'Crafting', icon: 'crafting', testId: 'nav-crafting' },
  { item: 'expeditions', label: 'Expeditions', icon: 'expeditions', testId: 'nav-expeditions' },
  { item: 'mastery', label: 'Mastery', icon: 'mastery', testId: 'nav-mastery' },
  { item: 'pvp', label: 'PvP', icon: 'pvp', testId: 'nav-pvp' },
]

const LATER_NAV: { label: string; icon: ChromeIconName; testId: string }[] = [
  { label: 'Guild', icon: 'guild', testId: 'nav-guild' },
  { label: 'Rankings', icon: 'rankings', testId: 'nav-rankings' },
]

export function GameLeftNav() {
  const location = useLocation()
  const [uiMode, setUiMode] = useState<UiMode>(() => readStoredUiMode())
  const [collapsed, setCollapsed] = useState(() => readStoredNavCollapsed())
  const locationQuery = useQuery({
    queryKey: ['location'],
    queryFn: fetchCurrentLocation,
    retry: false,
  })
  const expeditionQuery = useQuery({
    queryKey: ['expedition'],
    queryFn: fetchCurrentExpedition,
    retry: false,
  })

  function toggleUiMode() {
    const next: UiMode = uiMode === 'compact' ? 'normal' : 'compact'
    setUiMode(next)
    persistUiMode(next)
    applyUiMode(next)
  }

  function toggleCollapsed() {
    const next = !collapsed
    setCollapsed(next)
    persistNavCollapsed(next)
  }

  const canClaim = expeditionQuery.data?.status === 'COMPLETED'
  const atTavern = locationQuery.data?.actions.includes('START_EXPEDITION') ?? false
  const officeOn = uiMode === 'compact'

  return (
    <nav
      className={classNames('game-leftnav', collapsed && 'is-collapsed')}
      aria-label="Primary"
      data-collapsed={collapsed ? 'true' : undefined}
    >
      <ul className="game-nav-list">
        {LIVE_NAV.map((entry) => {
          const active = isGameNavActive(entry.item, location)
          return (
            <li key={entry.item}>
              <Link
                to={gameLink(entry.item)}
                data-testid={entry.testId}
                className={active ? 'active' : undefined}
                aria-current={active ? 'page' : undefined}
                title={collapsed ? entry.label : undefined}
              >
                <ChromeIcon name={entry.icon} />
                <span className="nav-label">{entry.label}</span>
              </Link>
            </li>
          )
        })}
        {LATER_NAV.map((entry) => (
          <li key={entry.testId}>
            <ComingLaterButton data-testid={entry.testId} title={collapsed ? entry.label : undefined}>
              <ChromeIcon name={entry.icon} />
              <span className="nav-label">{entry.label}</span>
            </ComingLaterButton>
          </li>
        ))}
      </ul>

      <div>
        <p className="game-nav-heading">Quick actions</p>
        <ul className="quick-actions">
          <li>
            {atTavern ? (
              <Link to={gameLink('expeditions')} data-testid="quick-tavern" title={collapsed ? 'Visit Tavern' : undefined}>
                <ChromeIcon name="tavern" />
                <span className="quick-action-copy">
                  <span>Visit Tavern</span>
                  <span className="quick-action-hint">Social & group</span>
                </span>
              </Link>
            ) : (
              <ComingLaterButton data-testid="quick-tavern" title={collapsed ? 'Visit Tavern' : undefined}>
                <ChromeIcon name="tavern" />
                <span className="quick-action-copy">
                  <span>Visit Tavern</span>
                  <span className="quick-action-hint">Social & group</span>
                </span>
              </ComingLaterButton>
            )}
          </li>
          <li>
            <ComingLaterButton data-testid="quick-daily" title={collapsed ? 'Daily Rewards' : undefined}>
              <ChromeIcon name="daily" />
              <span className="quick-action-copy">
                <span>Daily Rewards</span>
                <span className="quick-action-hint">Available later</span>
              </span>
            </ComingLaterButton>
          </li>
          {canClaim ? (
            <li>
              <Link
                to={gameLink('expeditions')}
                data-testid="quick-claim-expedition"
                title={collapsed ? 'Claim Expedition' : undefined}
              >
                <ChromeIcon name="expeditions" />
                <span className="quick-action-copy">
                  <span>Claim Expedition</span>
                  <span className="quick-action-hint">Rewards</span>
                </span>
              </Link>
            </li>
          ) : null}
          <li>
            <Link to={gameLink('market')} data-testid="quick-market" title={collapsed ? 'Market Deals' : undefined}>
              <ChromeIcon name="market" />
              <span className="quick-action-copy">
                <span>Market Deals</span>
                <span className="quick-action-hint">Open marketplace</span>
              </span>
            </Link>
          </li>
          <li>
            <Link to={gameLink('world')} data-testid="quick-travel" title={collapsed ? 'Travel' : undefined}>
              <ChromeIcon name="travel" />
              <span className="quick-action-copy">
                <span>Travel</span>
                <span className="quick-action-hint">Change location</span>
              </span>
            </Link>
          </li>
        </ul>
      </div>

      <div className="game-leftnav-foot">
        <Button
          type="button"
          variant="ghost"
          className="office-toggle"
          data-testid="ui-mode-toggle"
          aria-pressed={officeOn}
          onClick={toggleUiMode}
        >
          <span className={officeOn ? 'office-switch is-on' : 'office-switch'} aria-hidden="true">
            <span className="office-knob" />
          </span>
          <span className="office-toggle-label">Office mode</span>
        </Button>
        <div className="game-leftnav-status">
          <ComingLaterButton data-testid="nav-online" className="nav-online" aria-label="Online players">
            <span className="online-dot" aria-hidden="true" />
            <span className="nav-online-label">Online</span>
          </ComingLaterButton>
          <Button
            type="button"
            variant="ghost"
            className="btn-icon-chrome nav-collapse"
            data-testid="nav-collapse"
            aria-label={collapsed ? 'Expand navigation' : 'Collapse navigation'}
            aria-pressed={collapsed}
            onClick={toggleCollapsed}
          >
            <ChromeIcon name="collapse" />
          </Button>
        </div>
      </div>
    </nav>
  )
}
