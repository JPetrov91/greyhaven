import { Link, useLocation } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { fetchCurrentExpedition } from '../api/expedition'
import { fetchCurrentLocation } from '../api/world'
import { ComingLaterButton } from '../ui/ComingLater'
import { gameLink, isGameNavActive, type GameNavItem } from '../ui/gameNav'
import { persistUiMode, readStoredUiMode, applyUiMode, type UiMode } from '../ui/uiMode'
import { useState } from 'react'
import { Button } from '../ui/Button'

const LIVE_NAV: { item: GameNavItem; label: string; testId: string }[] = [
  { item: 'home', label: 'Home', testId: 'nav-home' },
  { item: 'world', label: 'Locations', testId: 'nav-world' },
  { item: 'character', label: 'Character', testId: 'nav-character' },
  { item: 'inventory', label: 'Inventory', testId: 'nav-inventory' },
  { item: 'equipment', label: 'Equipment', testId: 'nav-equipment' },
  { item: 'market', label: 'Market', testId: 'nav-market' },
  { item: 'expeditions', label: 'Expeditions', testId: 'nav-expeditions' },
  { item: 'mastery', label: 'Mastery', testId: 'nav-mastery' },
]

export function GameLeftNav() {
  const location = useLocation()
  const [uiMode, setUiMode] = useState<UiMode>(() => readStoredUiMode())
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

  const canClaim = expeditionQuery.data?.status === 'COMPLETED'
  const atTavern = locationQuery.data?.actions.includes('START_EXPEDITION') ?? false

  return (
    <nav className="game-leftnav" aria-label="Primary">
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
              >
                {entry.label}
              </Link>
            </li>
          )
        })}
        <li>
          <ComingLaterButton data-testid="nav-crafting">Crafting</ComingLaterButton>
        </li>
        <li>
          <ComingLaterButton data-testid="nav-pvp">PvP</ComingLaterButton>
        </li>
        <li>
          <ComingLaterButton data-testid="nav-guild">Guild</ComingLaterButton>
        </li>
        <li>
          <ComingLaterButton data-testid="nav-rankings">Rankings</ComingLaterButton>
        </li>
      </ul>

      <div>
        <p className="game-nav-heading">Quick actions</p>
        <ul className="quick-actions">
          <li>
            <Link to={gameLink('world')} data-testid="quick-travel">
              Travel
            </Link>
          </li>
          <li>
            {atTavern ? (
              <Link to={gameLink('expeditions')} data-testid="quick-tavern">
                Visit Tavern
              </Link>
            ) : (
              <ComingLaterButton data-testid="quick-tavern">Visit Tavern</ComingLaterButton>
            )}
          </li>
          {canClaim ? (
            <li>
              <Link to={gameLink('expeditions')} data-testid="quick-claim-expedition">
                Claim Expedition
              </Link>
            </li>
          ) : null}
          <li>
            <Link to={gameLink('market')} data-testid="quick-market">
              Market Deals
            </Link>
          </li>
          <li>
            <ComingLaterButton data-testid="quick-daily">Daily Rewards</ComingLaterButton>
          </li>
        </ul>
      </div>

      <div className="game-leftnav-foot">
        <Button
          type="button"
          variant="ghost"
          data-testid="ui-mode-toggle"
          aria-pressed={uiMode === 'compact'}
          onClick={toggleUiMode}
        >
          {uiMode === 'compact' ? 'Office mode on' : 'Office mode'}
        </Button>
      </div>
    </nav>
  )
}
